package com.pokerapp.service.impl;

import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.response.CardDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.api.dto.response.MessageDto;
import com.pokerapp.api.dto.response.PlayerStateDto;
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
import com.pokerapp.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final TableRepository tableRepository;
    private final PlayerRepository playerRepository;
    private final ReplayRepository replayRepository;
    private final ReplayService replayService;
    private final StatisticsRepository statisticsRepository;
    private final StatisticsService statisticsService;
    private final HandEvaluator handEvaluator;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @Autowired
    public GameServiceImpl(
            GameRepository gameRepository,
            TableRepository tableRepository,
            PlayerRepository playerRepository,
            ReplayRepository replayRepository,
            ReplayService replayService,
            StatisticsRepository statisticsRepository,
            StatisticsService statisticsService,
            HandEvaluator handEvaluator,
            SimpMessagingTemplate messagingTemplate,
            UserService userService) {
        this.gameRepository = gameRepository;
        this.tableRepository = tableRepository;
        this.playerRepository = playerRepository;
        this.replayRepository = replayRepository;
        this.replayService = replayService;
        this.statisticsRepository = statisticsRepository;
        this.statisticsService = statisticsService;
        this.handEvaluator = handEvaluator;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Game createGame(Long tableId) {
        PokerTable pokerTable = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Table not found with ID: " + tableId));

        // Check if the table has enough players
        if (pokerTable.getPlayers().size() < 2) {
            throw new IllegalStateException("Cannot create a game with fewer than 2 players");
        }

        Game game = new Game();
        game.setPokerTable(pokerTable);
        game.setSmallBlind(pokerTable.getMinBuyIn() / 100);
        game.setBigBlind(pokerTable.getMinBuyIn() / 50);
        game.setStatus(GameStatus.WAITING);

        game = gameRepository.save(game);

        // Update the table's current game
        pokerTable.setCurrentGame(game);
        tableRepository.save(pokerTable);

        // Create and associate replay system
        replayService.createReplay(game);

        // Log game creation in system messages
        broadcastSystemMessage(game.getId(),
                "Game created at table: " + pokerTable.getName());

        return game;
    }

    @Override
    @Transactional
    public Game startGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        if (game.getStatus() != GameStatus.WAITING) {
            throw new IllegalStateException("Game is not in WAITING state");
        }

        PokerTable table = game.getPokerTable();
        if (table.getPlayers().size() < 2) {
            throw new IllegalStateException("Need at least 2 players to start a game");
        }

        // Initialize the deck
        game.getDeck().initialize();
        game.getDeck().shuffle();

        // Deal cards to players
        dealPlayerCards(game);

        // Create first round
        GameRound firstRound = new GameRound();
        firstRound.setRoundNumber(1);
        firstRound.setGame(game);
        firstRound.setPot(0.0);
        game.getGameRounds().add(firstRound);
        game.setCurrentRound(firstRound);

        // Collect blinds from players
        collectBlinds(game);

        // Start with preflop betting
        firstRound.advanceToNextBettingRound();

        // Mark game as in progress
        game.setStatus(GameStatus.IN_PROGRESS);
        Game savedGame = gameRepository.save(game);

        // Notify all players
        broadcastSystemMessage(gameId, "Game started. Blinds: $" +
                game.getSmallBlind() + "/$" + game.getBigBlind());
        messagingTemplate.convertAndSend("/topic/games/" + gameId, getGameState(gameId));

        return savedGame;
    }

    /**
     * Collect the small and big blinds from the appropriate players
     */
    private void collectBlinds(Game game) {
        List<Player> activePlayers = new ArrayList<>(game.getPokerTable().getPlayers());

        // Order players starting with the one after the dealer
        Collections.rotate(activePlayers, -(game.getDealerPosition() + 1));

        if (activePlayers.size() >= 2) {
            // Small blind is posted by the first player after the dealer
            Player smallBlindPlayer = activePlayers.get(0);
            double smallBlindAmount = Math.min(game.getSmallBlind(), smallBlindPlayer.getChips());
            smallBlindPlayer.setChips(smallBlindPlayer.getChips() - smallBlindAmount);

            // Big blind is posted by the second player after the dealer
            Player bigBlindPlayer = activePlayers.get(1);
            double bigBlindAmount = Math.min(game.getBigBlind(), bigBlindPlayer.getChips());
            bigBlindPlayer.setChips(bigBlindPlayer.getChips() - bigBlindAmount);

            // Add blinds to the pot
            double totalBlinds = smallBlindAmount + bigBlindAmount;
            game.getCurrentRound().setPot(totalBlinds);

            // Save updated player balances
            playerRepository.save(smallBlindPlayer);
            playerRepository.save(bigBlindPlayer);

            // Set up betting round with current bet equal to big blind
            BettingRound bettingRound = game.getCurrentRound().getCurrentBettingRound();
            if (bettingRound != null) {
                bettingRound.setCurrentBet(bigBlindAmount);
            }

            // Record blinds in the game log
            broadcastSystemMessage(game.getId(),
                    smallBlindPlayer.getUsername() + " posts small blind: $" + smallBlindAmount);
            broadcastSystemMessage(game.getId(),
                    bigBlindPlayer.getUsername() + " posts big blind: $" + bigBlindAmount);
        }
    }

    @Override
    @Transactional // Add @Transactional to ensure the session is open when accessing lazy collections
    public GameStateDto getGameState(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        GameStateDto gameStateDto = new GameStateDto();
        gameStateDto.setGameId(game.getId());
        gameStateDto.setStatus(game.getStatus().toString());

        // Set initial empty lists to avoid null references
        gameStateDto.setCommunityCards(new ArrayList<>());
        gameStateDto.setPossibleActions(new ArrayList<>());
        gameStateDto.setPlayers(new ArrayList<>());
        gameStateDto.setMessages(new ArrayList<>());

        if (game.getCurrentRound() != null) {
            GameRound currentRound = game.getCurrentRound();
            gameStateDto.setPot(currentRound.getPot());

            // Only show community cards if they've been dealt
            List<CardDto> communityCards = currentRound.getCommunityCards().stream()
                    .filter(Card::isShowing)
                    .map(this::convertToCardDto)
                    .collect(Collectors.toList());
            gameStateDto.setCommunityCards(communityCards);

            BettingRound bettingRound = currentRound.getCurrentBettingRound();
            if (bettingRound != null) {
                gameStateDto.setCurrentBet(bettingRound.getCurrentBet());
                gameStateDto.setStage(bettingRound.getStage().toString());

                // Find the current player to act
                Player currentPlayer = determineCurrentPlayer(game);
                if (currentPlayer != null) {
                    gameStateDto.setCurrentPlayerId(currentPlayer.getId());

                    // Add possible actions for current player
                    gameStateDto.setPossibleActions(getPossibleActions(game, currentPlayer));
                }
            }
        }

        // Add player states (with proper card visibility)
        List<PlayerStateDto> playerStates = game.getPokerTable().getPlayers().stream()
                .map(player -> convertToPlayerStateDto(player, game))
                .collect(Collectors.toList());
        gameStateDto.setPlayers(playerStates);

        // Add some system messages
        List<MessageDto> messages = new ArrayList<>();
        MessageDto statusMsg = new MessageDto();
        statusMsg.setType("INFO");
        statusMsg.setContent("Game status: " + game.getStatus());
        statusMsg.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        messages.add(statusMsg);

        gameStateDto.setMessages(messages);

        return gameStateDto;
    }

    /**
     * Determine which player's turn it is in the current betting round
     */
    private Player determineCurrentPlayer(Game game) {
        GameRound round = game.getCurrentRound();
        if (round == null) return null;

        BettingRound bettingRound = round.getCurrentBettingRound();
        if (bettingRound == null) return null;

        // Get the list of active players in order
        List<Player> orderedPlayers = getOrderedPlayers(game);

        // Filter to only active players who haven't folded or gone all-in
        List<Player> activePlayers = orderedPlayers.stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .collect(Collectors.toList());

        if (activePlayers.isEmpty()) return null;

        // The current player is determined by the currentPlayerIndex in the betting round
        int currentIndex = bettingRound.getCurrentPlayerIndex();
        if (currentIndex >= activePlayers.size()) {
            currentIndex = 0;
        }

        return activePlayers.get(currentIndex);
    }

    /**
     * Get the players ordered correctly for the current betting round
     */
    private List<Player> getOrderedPlayers(Game game) {
        List<Player> players = new ArrayList<>(game.getPokerTable().getPlayers());

        // In preflop, action starts with player after big blind (3rd position from dealer)
        // In later rounds, action starts with player after dealer
        int startPosition = game.getCurrentRound().getCurrentBettingRound().getStage() == BettingStage.PREFLOP ? 3 : 1;

        // Rotate the list so the first player to act is at the beginning
        Collections.rotate(players, -(game.getDealerPosition() + startPosition) % players.size());

        return players;
    }

    @Override
    @Transactional
    public GameStateDto makeMove(Long gameId, Long userId, MoveDto moveDto) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        // Find player by user ID
        Player player = playerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Player not found for user ID: " + userId));

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
        move.setTimestamp(System.currentTimeMillis());

        // Validate the move based on type
        validateMove(move, player, bettingRound);

        // Process the move's effect on chips and pot
        processMove(move, player, round);

        // Add the move to the betting round
        bettingRound.processMove(player, move);

        // Record for replay
        Replay replay = replayRepository.findByGameId(gameId)
                .orElseThrow(() -> new NotFoundException("Replay not found for game ID: " + gameId));
        replay.recordAction(GameAction.fromMove(move, replay.getActionCounter() + 1));
        replayRepository.save(replay);

        // Update player status based on move
        updatePlayerStatus(player, move);
        playerRepository.save(player);

        // Advance to the next player in turn
        advanceToNextPlayer(bettingRound, game);

        // Check if betting round is complete
        if (isBettingRoundComplete(bettingRound, game)) {
            advanceGame(game);
        }

        gameRepository.save(game);

        // Broadcast the move to all players
        broadcastPlayerMove(game.getId(), player, move);

        // Notify all players of the updated game state
        GameStateDto gameState = getGameState(gameId);
        messagingTemplate.convertAndSend("/topic/games/" + gameId, gameState);

        return gameState;
    }

    /**
     * Broadcast a player's move to all players at the table
     */
    private void broadcastPlayerMove(Long gameId, Player player, Move move) {
        String message;
        switch (move.getType()) {
            case CHECK:
                message = player.getUsername() + " checks";
                break;
            case CALL:
                message = player.getUsername() + " calls $" + move.getAmount();
                break;
            case RAISE:
                message = player.getUsername() + " raises to $" + move.getAmount();
                break;
            case FOLD:
                message = player.getUsername() + " folds";
                break;
            case ALL_IN:
                message = player.getUsername() + " goes all-in with $" + move.getAmount();
                break;
            default:
                message = player.getUsername() + " makes a move: " + move.getType();
        }

        broadcastSystemMessage(gameId, message);
    }

    /**
     * Broadcast a system message to all players
     */
    private void broadcastSystemMessage(Long gameId, String content) {
        MessageDto message = new MessageDto();
        message.setType("INFO");
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        messagingTemplate.convertAndSend("/topic/games/" + gameId + "/messages", message);
    }

    /**
     * Validate a player's move based on game rules
     */
    private void validateMove(Move move, Player player, BettingRound bettingRound) {
        Double currentBet = bettingRound.getCurrentBet();

        switch (move.getType()) {
            case CHECK:
                if (currentBet > 0) {
                    throw new InvalidMoveException("Cannot check when there's a bet to call");
                }
                break;

            case CALL:
                if (currentBet <= 0) {
                    throw new InvalidMoveException("Cannot call when there's no bet");
                }
                if (player.getChips() < currentBet) {
                    throw new InvalidMoveException("Not enough chips to call");
                }
                move.setAmount(currentBet);
                break;

            case RAISE:
                if (move.getAmount() <= currentBet) {
                    throw new InvalidMoveException("Raise amount must be greater than current bet");
                }
                if (move.getAmount() > player.getChips()) {
                    throw new InvalidMoveException("Not enough chips to raise");
                }
                break;

            case ALL_IN:
                move.setAmount(player.getChips());
                break;

            case FOLD:
                // No validation needed for fold
                break;

            default:
                throw new InvalidMoveException("Invalid move type");
        }
    }

    /**
     * Process a move's effect on chips and pot
     */
    private void processMove(Move move, Player player, GameRound round) {
        BettingRound bettingRound = round.getCurrentBettingRound();

        switch (move.getType()) {
            case CHECK:
                // No chips are moved when checking
                break;

            case CALL:
            case RAISE:
            case ALL_IN:
                double amount = move.getAmount();
                player.setChips(player.getChips() - amount);
                round.setPot(round.getPot() + amount);

                // If it's a raise or all-in, update the current bet
                if (move.getType() == MoveType.RAISE || move.getType() == MoveType.ALL_IN) {
                    bettingRound.setCurrentBet(amount);
                }
                break;

            case FOLD:
                // No chips are moved when folding
                break;
        }
    }

    /**
     * Advance to the next player in the betting round
     */
    private void advanceToNextPlayer(BettingRound bettingRound, Game game) {
        List<Player> orderedPlayers = getOrderedPlayers(game);

        // Filter to only active players
        List<Player> activePlayers = orderedPlayers.stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .collect(Collectors.toList());

        if (activePlayers.isEmpty()) return;

        // Increment the current player index
        int currentIndex = bettingRound.getCurrentPlayerIndex();
        currentIndex = (currentIndex + 1) % activePlayers.size();
        bettingRound.setCurrentPlayerIndex(currentIndex);
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
                // Other moves keep the player ACTIVE
                player.setStatus(PlayerStatus.ACTIVE);
        }
    }

    /**
     * Check if the current betting round is complete
     */
    private boolean isBettingRoundComplete(BettingRound bettingRound, Game game) {
        // Get all active players (not folded or all-in)
        List<Player> activePlayers = game.getPokerTable().getPlayers().stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .collect(Collectors.toList());

        // If there are no active players or only one, the betting round is complete
        if (activePlayers.size() <= 1) {
            return true;
        }

        // Check if all active players have acted
        List<Player> playersWhoActed = bettingRound.getMoves().stream()
                .map(Move::getPlayer)
                .collect(Collectors.toList());

        // If not all active players have acted, the round isn't complete
        if (activePlayers.stream().anyMatch(p -> !playersWhoActed.contains(p))) {
            return false;
        }

        // Check if all active players have the same amount committed
        // or are all-in (provided at least one player has acted)
        if (!bettingRound.getMoves().isEmpty()) {
            Double highestBet = bettingRound.getCurrentBet();

            // Each active player should have called the highest bet
            for (Player player : activePlayers) {
                Double playerBet = bettingRound.getMoves().stream()
                        .filter(m -> m.getPlayer().equals(player))
                        .mapToDouble(Move::getAmount)
                        .sum();

                if (playerBet < highestBet && player.getStatus() != PlayerStatus.ALL_IN) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Advance the game state after a completed betting round
     */
    private void advanceGame(Game game) {
        GameRound round = game.getCurrentRound();
        BettingRound bettingRound = round.getCurrentBettingRound();

        // If this was the river or there's only one player left active, determine winners and end the round
        if (bettingRound.getStage() == BettingStage.RIVER ||
                game.getPokerTable().getPlayers().stream()
                        .filter(p -> p.getStatus() != PlayerStatus.FOLDED)
                        .count() <= 1) {

            // Determine winners
            Map<Player, Double> winnings = determineWinners(game);

            // Distribute pot
            for (Map.Entry<Player, Double> entry : winnings.entrySet()) {
                Player winner = entry.getKey();
                Double amount = entry.getValue();

                winner.setChips(winner.getChips() + amount);
                playerRepository.save(winner);

                broadcastSystemMessage(game.getId(),
                        winner.getUsername() + " wins $" + amount);
            }

            // Record game results for statistics
            GameResult gameResult = statisticsService.recordGameResult(game, winnings);
            statisticsService.updateUserStatistics(gameResult);

            // Start new round or end game
            if (shouldStartNewRound(game)) {
                startNewRound(game);
            } else {
                game.setStatus(GameStatus.FINISHED);
                replayService.completeReplay(game.getId());
                broadcastSystemMessage(game.getId(), "Game finished");
            }
        } else {
            // Advance to next betting stage
            round.advanceToNextBettingRound();

            // Broadcast the new stage
            String stageName = "";
            switch (round.getCurrentBettingRound().getStage()) {
                case FLOP:
                    stageName = "flop";
                    break;
                case TURN:
                    stageName = "turn";
                    break;
                case RIVER:
                    stageName = "river";
                    break;
            }

            broadcastSystemMessage(game.getId(), "Dealing the " + stageName);
        }
    }

    /**
     * Determine the winners of the current round and distribute the pot
     */
    private Map<Player, Double> determineWinners(Game game) {
        GameRound round = game.getCurrentRound();

        // Get all players who haven't folded
        List<Player> activePlayers = game.getPokerTable().getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.FOLDED)
                .collect(Collectors.toList());

        // If only one player remains (others folded), they win the pot
        if (activePlayers.size() == 1) {
            Player winner = activePlayers.get(0);
            Map<Player, Double> winnings = new HashMap<>();
            winnings.put(winner, round.getPot());
            return winnings;
        }

        // For multiple players, compare their hands to determine winner(s)
        Map<Player, List<Card>> playerHands = activePlayers.stream()
                .collect(Collectors.toMap(
                        player -> player,
                        player -> player.getHand().getCards()
                ));

        Map<Player, Integer> rankings = handEvaluator.compareHands(playerHands, round.getCommunityCards());

        // Find the highest rank (lowest number = best hand)
        int bestRank = rankings.values().stream()
                .min(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);

        // Find all players with the best rank
        List<Player> winners = rankings.entrySet().stream()
                .filter(entry -> entry.getValue() == bestRank)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Calculate prize distribution
        double totalPot = round.getPot();
        double prizePerWinner = totalPot / winners.size();

        Map<Player, Double> winnings = new HashMap<>();
        for (Player winner : winners) {
            winnings.put(winner, prizePerWinner);
        }

        return winnings;
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
                playerRepository.save(player);
            }
        }

        // Create new round
        GameRound newRound = new GameRound();
        newRound.setRoundNumber(game.getGameRounds().size() + 1);
        newRound.setGame(game);
        newRound.setPot(0.0);
        game.getGameRounds().add(newRound);
        game.setCurrentRound(newRound);

        // Advance dealer position
        game.setDealerPosition((game.getDealerPosition() + 1) % game.getPokerTable().getPlayers().size());

        // Reset and shuffle the deck
        game.getDeck().initialize();
        game.getDeck().shuffle();

        // Deal cards
        dealPlayerCards(game);

        // Collect blinds
        collectBlinds(game);

        // Start with preflop betting
        newRound.advanceToNextBettingRound();

        broadcastSystemMessage(game.getId(), "New round started. Dealer: " +
                getPlayerAtPosition(game, game.getDealerPosition()).getUsername());
    }

    /**
     * Get the player at a specific position around the table
     */
    private Player getPlayerAtPosition(Game game, int position) {
        List<Player> players = new ArrayList<>(game.getPokerTable().getPlayers());
        if (players.isEmpty()) return null;
        return players.get(position % players.size());
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

    private boolean isPlayerTurn(Game game, Player player) {
        Player currentPlayer = determineCurrentPlayer(game);
        return currentPlayer != null && currentPlayer.getId().equals(player.getId());
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
        dto.setId(player.getId());        // Player ID
        dto.setUserId(player.getUserId()); // User ID
        dto.setUsername(player.getUsername());
        dto.setChips(player.getChips());
        dto.setStatus(player.getStatus().toString());

        // Check if this player is the current player to act
        Player currentPlayer = determineCurrentPlayer(game);
        dto.setTurn(currentPlayer != null && currentPlayer.getId().equals(player.getId()));

        // Determine if cards should be visible:
        // 1. For the game viewer (the player themselves)
        // 2. At showdown (game finished or at river with betting completed)
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            // If we can't get current user, don't show cards
        }

        boolean isViewerPlayer = currentUser != null &&
                player.getUserId().equals(currentUser.getId());

        boolean isShowdown = game.getStatus() == GameStatus.FINISHED ||
                (game.getCurrentRound() != null &&
                        game.getCurrentRound().getCurrentBettingRound() != null &&
                        game.getCurrentRound().getCurrentBettingRound().getStage() == BettingStage.RIVER &&
                        isBettingRoundComplete(game.getCurrentRound().getCurrentBettingRound(), game));

        if (player.getHand() != null && (isViewerPlayer || isShowdown)) {
            List<CardDto> cards = player.getHand().getCards().stream()
                    .map(this::convertToCardDto)
                    .collect(Collectors.toList());
            dto.setCards(cards);
        } else {
            // Create hidden cards
            List<CardDto> hiddenCards = new ArrayList<>();
            if (player.getHand() != null) {
                for (int i = 0; i < player.getHand().getCards().size(); i++) {
                    CardDto hiddenCard = new CardDto();
                    hiddenCard.setHidden(true);
                    hiddenCards.add(hiddenCard);
                }
            }
            dto.setCards(hiddenCards);
        }

        return dto;
    }

    @Override
    @Transactional
    public Game endGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        // If game is already finished, just return it
        if (game.getStatus() == GameStatus.FINISHED) {
            return game;
        }

        // Determine winners
        Map<Player, Double> winnings = determineWinners(game);

        // Distribute winnings
        for (Map.Entry<Player, Double> entry : winnings.entrySet()) {
            Player winner = entry.getKey();
            Double amount = entry.getValue();
            winner.setChips(winner.getChips() + amount);
            playerRepository.save(winner);

            broadcastSystemMessage(game.getId(),
                    winner.getUsername() + " wins $" + amount);
        }

        // Record all participants with zero winnings if they didn't win
        for (Player player : game.getPokerTable().getPlayers()) {
            if (!winnings.containsKey(player)) {
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

        broadcastSystemMessage(game.getId(), "Game finished");

        return savedGame;
    }
}