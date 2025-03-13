// src/main/java/com/pokerapp/service/impl/GameServiceImpl.java
package com.pokerapp.service.impl;

import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.api.dto.response.PlayerStateDto;
import com.pokerapp.api.dto.response.CardDto;
import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Hand;
import com.pokerapp.domain.game.*;
import com.pokerapp.domain.poker.HandEvaluator;
import com.pokerapp.domain.replay.GameAction;
import com.pokerapp.domain.replay.Replay;
import com.pokerapp.domain.statistics.GameResult;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import com.pokerapp.domain.user.User;
import com.pokerapp.exception.InvalidMoveException;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.*;
import com.pokerapp.service.GameService;
import com.pokerapp.service.ReplayService;
import com.pokerapp.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private final GameRepository gameRepository;

    @Autowired
    private final TableRepository tableRepository;

    @Autowired
    private final PlayerRepository playerRepository;

    @Autowired
    private final ReplayRepository replayRepository;

    @Autowired
    private final ReplayService replayService;

    @Autowired
    private final StatisticsRepository statisticsRepository;

    @Autowired
    private final StatisticsService statisticsService;

    @Autowired
    private final HandEvaluator handEvaluator;

    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public GameServiceImpl(
            GameRepository gameRepository,
            TableRepository tableRepository,
            PlayerRepository playerRepository,
            ReplayRepository replayRepository,
            ReplayService replayService, StatisticsRepository statisticsRepository, StatisticsService statisticsService,
            HandEvaluator handEvaluator,
            SimpMessagingTemplate messagingTemplate) {
        this.gameRepository = gameRepository;
        this.tableRepository = tableRepository;
        this.playerRepository = playerRepository;
        this.replayRepository = replayRepository;
        this.replayService = replayService;
        this.statisticsRepository = statisticsRepository;
        this.statisticsService = statisticsService;
        this.handEvaluator = handEvaluator;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public Game createGame(Long tableId) {
        PokerTable pokerTable = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Table not found"));

        Game game = new Game();
        game.setPokerTable(pokerTable);
        game.setSmallBlind(pokerTable.getMinBuyIn() / 100);
        game.setBigBlind(pokerTable.getMinBuyIn() / 50);
        game.setStatus(GameStatus.WAITING);

        game = gameRepository.save(game);

        // Create and associate replay system
        replayService.createReplay(game);

        return game;
    }

    @Override
    @Transactional
    public Game startGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        if (game.getStatus() != GameStatus.WAITING) {
            throw new IllegalStateException("Game is not in WAITING state");
        }

        if (game.getPokerTable().getPlayers().size() < 2) {
            throw new IllegalStateException("Need at least 2 players to start a game");
        }

        game.start();
        Game savedGame = gameRepository.save(game);

        // Notify all players
        messagingTemplate.convertAndSend("/topic/games/" + gameId, getGameState(gameId));

        return savedGame;
    }

    @Override
    public GameStateDto getGameState(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        GameStateDto gameStateDto = new GameStateDto();
        gameStateDto.setGameId(game.getId());
        gameStateDto.setStatus(game.getStatus().toString());

        if (game.getCurrentRound() != null) {
            gameStateDto.setPot(game.getCurrentRound().getPot());

            // Only show community cards if they've been dealt
            List<CardDto> communityCards = game.getCurrentRound().getCommunityCards().stream()
                    .filter(Card::isShowing)
                    .map(this::convertToCardDto)
                    .collect(Collectors.toList());
            gameStateDto.setCommunityCards(communityCards);

            BettingRound bettingRound = game.getCurrentRound().getCurrentBettingRound();
            if (bettingRound != null) {
                gameStateDto.setCurrentBet(bettingRound.getCurrentBet());
                gameStateDto.setStage(bettingRound.getStage().toString());

                // Set current player
                Player nextPlayer = bettingRound.getNextPlayer();
                if (nextPlayer != null) {
                    gameStateDto.setCurrentPlayerId(nextPlayer.getId());

                    // Add possible actions for current player
                    List<String> possibleActions = getPossibleActions(game, nextPlayer);
                    gameStateDto.setPossibleActions(possibleActions);
                }
            }
        }

        // Add player states (with proper card visibility)
        List<PlayerStateDto> playerStates = game.getPokerTable().getPlayers().stream()
                .map(player -> convertToPlayerStateDto(player, game))
                .collect(Collectors.toList());
        gameStateDto.setPlayers(playerStates);

        return gameStateDto;
    }

    @Override
    @Transactional
    public GameStateDto makeMove(Long gameId, Long playerId, MoveDto moveDto) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }

        GameRound round = game.getCurrentRound();
        BettingRound bettingRound = round.getCurrentBettingRound();

        // Check if it's this player's turn
        if (!isPlayerTurn(game, player)) {
            throw new InvalidMoveException("Not your turn");
        }

        // Create and process move
        Move move = new Move();
        move.setType(MoveType.valueOf(moveDto.getType()));
        move.setAmount(moveDto.getAmount());
        move.setPlayer(player);

        bettingRound.processMove(player, move);

        // Record for replay
        Replay replay = replayRepository.findByGameId(gameId)
                .orElseThrow(() -> new NotFoundException("Replay not found"));
        replay.recordAction(GameAction.fromMove(move, replay.getActionCounter() + 1));
        replayRepository.save(replay);

        // Handle player status based on move
        updatePlayerStatus(player, move);
        playerRepository.save(player);

        // Check if betting round is complete
        if (isBettingRoundComplete(bettingRound)) {
            advanceGame(game);
        }

        gameRepository.save(game);

        // Notify all players
        GameStateDto gameState = getGameState(gameId);
        messagingTemplate.convertAndSend("/topic/games/" + gameId, gameState);

        return gameState;
    }

    private void updatePlayerStatus(Player player, Move move) {
        switch (move.getType()) {
            case FOLD:
                player.setStatus(PlayerStatus.FOLDED);
                break;
            case ALL_IN:
                player.setStatus(PlayerStatus.ALL_IN);
                break;
            default:
                player.setStatus(PlayerStatus.ACTIVE);
        }
    }

    @Override
    @Transactional
    public Game endGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        List<Player> winners = game.determineWinner();

        // Distribute pot to winners and record for statistics
        Map<User, Double> winnings = new HashMap<>();

        if (!winners.isEmpty()) {
            GameRound round = game.getCurrentRound();
            double potPerWinner = round.getPot() / winners.size();

            for (Player winner : winners) {
                winner.setChips(winner.getChips() + potPerWinner);
                playerRepository.save(winner);
                winnings.put(winner, potPerWinner);
            }
        }

        // Record all participants with zero winnings if they didn't win
        for (Player player : game.getPokerTable().getPlayers()) {
            if (!winners.contains(player)) {
                winnings.put(player, 0.0);
            }
        }

        // Record game results for statistics
        GameResult gameResult = statisticsService.recordGameResult(game, winnings);
        statisticsService.updateUserStatistics(gameResult);

        game.setStatus(GameStatus.FINISHED);
        Game savedGame = gameRepository.save(game);

        // Complete the replay
        replayService.completeReplay(game.getId());

        return savedGame;
    }


    private boolean isPlayerTurn(Game game, Player player) {
        GameRound round = game.getCurrentRound();
        if (round == null) return false;

        BettingRound bettingRound = round.getCurrentBettingRound();
        if (bettingRound == null) return false;

        Player nextPlayer = bettingRound.getNextPlayer();
        return nextPlayer != null && nextPlayer.getId().equals(player.getId());
    }

    private boolean isBettingRoundComplete(BettingRound bettingRound) {
        // Implementation to check if betting round is complete
        // This would check if all active players have made a move and bets are equal
        return bettingRound.getNextPlayer() == null;
    }

    private void advanceGame(Game game) {
        GameRound round = game.getCurrentRound();
        BettingRound bettingRound = round.getCurrentBettingRound();

        // If this was the river, determine winners and end the round
        if (bettingRound.getStage() == BettingStage.RIVER) {
            List<Player> winners = game.determineWinner();

            // Distribute pot
            if (!winners.isEmpty()) {
                double potPerWinner = round.getPot() / winners.size();
                for (Player winner : winners) {
                    winner.setChips(winner.getChips() + potPerWinner);
                    playerRepository.save(winner);
                }
            }

            // Start new round or end game
            if (shouldStartNewRound(game)) {
                startNewRound(game);
            } else {
                game.setStatus(GameStatus.FINISHED);
                replayService.completeReplay(game.getId());
            }
        } else {
            // Advance to next betting stage
            round.advanceToNextBettingRound();
        }
    }

    private boolean shouldStartNewRound(Game game) {
        // Check if there are at least 2 players with chips
        int playersWithChips = 0;
        for (Player player : game.getPokerTable().getPlayers()) {
            if (player.getChips() > 0) {
                playersWithChips++;
            }
        }
        return playersWithChips >= 2;
    }

    private void startNewRound(Game game) {
        // Reset all players who still have chips
        for (Player player : game.getPokerTable().getPlayers()) {
            if (player.getChips() > 0) {
                player.setStatus(PlayerStatus.ACTIVE);
            }
        }

        // Create new round
        GameRound newRound = new GameRound();
        newRound.setRoundNumber(game.getGameRounds().size() + 1);
        newRound.setGame(game);
        game.getGameRounds().add(newRound);
        game.setCurrentRound(newRound);

        // Advance dealer position
        game.setDealerPosition((game.getDealerPosition() + 1) % game.getPokerTable().getPlayers().size());

        // Deal cards
        dealPlayerCards(game);

        // Start with preflop betting
        newRound.advanceToNextBettingRound();
    }

    private void dealPlayerCards(Game game) {
        for (Player player : game.getPokerTable().getPlayers()) {
            if (player.getChips() > 0) {
                if (player.getHand() == null) {
                    player.setHand(new Hand());
                } else {
                    player.getHand().clear();
                }

                // Each player gets 2 cards
                player.getHand().addCard(game.getDeck().drawCard());
                player.getHand().addCard(game.getDeck().drawCard());
                playerRepository.save(player);
            }
        }
    }

    private List<String> getPossibleActions(Game game, Player player) {
        List<String> actions = new ArrayList<>();
        GameRound round = game.getCurrentRound();
        BettingRound bettingRound = round.getCurrentBettingRound();

        Double currentBet = bettingRound.getCurrentBet();

        // Player can always fold
        actions.add("FOLD");

        // Check is possible when there's no bet to call
        if (currentBet == 0) {
            actions.add("CHECK");
        } else {
            // Call is possible when there's a bet to call
            actions.add("CALL");
        }

        // Raise is possible if player has enough chips
        if (player.getChips() > currentBet) {
            actions.add("RAISE");
        }

        // All-in is always an option
        actions.add("ALL_IN");

        return actions;
    }

    private CardDto convertToCardDto(Card card) {
        CardDto dto = new CardDto();
        if (card.isShowing()) {
            dto.setSuit(card.getSuit().toString());
            dto.setRank(card.getRank().toString());
        } else {
            dto.setHidden(true);
        }
        return dto;
    }

    private PlayerStateDto convertToPlayerStateDto(Player player, Game game) {
        PlayerStateDto dto = new PlayerStateDto();
        dto.setId(player.getId());
        dto.setUsername(player.getUsername());
        dto.setChips(player.getChips());
        dto.setStatus(player.getStatus().toString());

        // Only show cards at showdown or to the player themselves
        boolean showCards = game.getStatus() == GameStatus.FINISHED ||
                (game.getCurrentRound() != null &&
                        game.getCurrentRound().getCurrentBettingRound() != null &&
                        game.getCurrentRound().getCurrentBettingRound().getStage() == BettingStage.RIVER);

        if (player.getHand() != null && (showCards || player.getId().equals(getCurrentUserId()))) {
            List<CardDto> cards = player.getHand().getCards().stream()
                    .map(this::convertToCardDto)
                    .collect(Collectors.toList());
            dto.setCards(cards);
        }

        return dto;
    }

    // In a real application, this would come from the security context
    private Long getCurrentUserId() {
        return 1L; // Placeholder
    }
}
