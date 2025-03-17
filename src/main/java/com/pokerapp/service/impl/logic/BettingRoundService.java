package com.pokerapp.service.impl.logic;

import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.api.dto.response.MessageDto;
import com.pokerapp.domain.game.*;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import com.pokerapp.exception.InvalidMoveException;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.*;
import com.pokerapp.service.HandEvaluationService;
import com.pokerapp.service.ReplayService;
import com.pokerapp.service.StatisticsService;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BettingRoundService {

    private final GameRepository gameRepository;
    private final TableRepository tableRepository;
    private final PlayerRepository playerRepository;
    private final ReplayService replayService;
    private final StatisticsService statisticsService;
    private final SimpMessagingTemplate messagingTemplate;
    private final HandEvaluationService handEvaluationService;
    private final GameRoundRepository gameRoundRepository;
    private final BettingRoundRepository bettingRoundRepository;

    @Autowired
    public BettingRoundService(
            GameRepository gameRepository,
            TableRepository tableRepository,
            PlayerRepository playerRepository,
            ReplayService replayService,
            StatisticsService statisticsService,
            SimpMessagingTemplate messagingTemplate,
            HandEvaluationService handEvaluationService,
            GameRoundRepository gameRoundRepository,
            BettingRoundRepository bettingRoundRepository) {
        this.gameRepository = gameRepository;
        this.tableRepository = tableRepository;
        this.playerRepository = playerRepository;
        this.replayService = replayService;
        this.statisticsService = statisticsService;
        this.messagingTemplate = messagingTemplate;
        this.handEvaluationService = handEvaluationService;
        this.gameRoundRepository = gameRoundRepository;
        this.bettingRoundRepository = bettingRoundRepository;
    }

    @Transactional
    public BettingRound createBettingRound(Long gameRoundId, BettingStage bettingStage) {
        // Find the GameRound
        GameRound gameRound = gameRoundRepository.findById(gameRoundId)
                .orElseThrow(() -> new NotFoundException("GameRound not found with ID: " + gameRoundId));

        // Create a new betting round
        BettingRound bettingRound = new BettingRound();
        bettingRound.setGameRound(gameRound);
        bettingRound.setStage(bettingStage);
        bettingRound.setCurrentBet(0.0);

        // Add to game round's betting rounds
        gameRound.getBettingRounds().add(bettingRound);
        gameRound.setCurrentBettingRound(bettingRound);

        // Save the betting round
        bettingRound = bettingRoundRepository.save(bettingRound);
        gameRoundRepository.save(gameRound);

        return bettingRound;
    }

    @Transactional
    public BettingRound playBettingRound(Long bettingRoundId) {
        BettingRound bettingRound = bettingRoundRepository.findById(bettingRoundId)
                .orElseThrow(() -> new NotFoundException("BettingRound not found with ID: " + bettingRoundId));

        GameRound gameRound = bettingRound.getGameRound();
        Game game = gameRound.getGame();

        // Get players, pot and current bet
        List<Player> players = game.getPlayers().stream().filter(p -> !p.getStatus().equals(PlayerStatus.SITTING_OUT)).toList();
        Set<Player> activePlayers = players.stream().filter(p -> p.getStatus().equals(PlayerStatus.ACTIVE)).collect(Collectors.toSet());
        Set<Player> foldedPlayers = players.stream().filter(p -> p.getStatus().equals(PlayerStatus.FOLDED)).collect(Collectors.toSet());

        double pot = gameRound.getPot();
        double currentBet = bettingRound.getCurrentBet();

        // Calculate dealer, small blind, and big blind positions
        int dealerIndex = game.getDealerIndex();
        int smallBlindIndex = (dealerIndex + 1) % players.size();
        int bigBlindIndex = (dealerIndex + 2) % players.size();

        // Initialize player bets tracker
        Map<String, Double> playerBets = new HashMap<>();
        for (Player player : players) {
            playerBets.put(player.getUsername(), 0.0);
        }

        // Process blinds for preflop
        if (bettingRound.getStage() == BettingStage.PREFLOP) {
            // Post small blind
            Player smallBlindPlayer = players.get(smallBlindIndex);
            double smallBlindAmount = game.getSmallBlind();
            processBlind(smallBlindPlayer, smallBlindAmount, playerBets, gameRound, MoveType.SMALL_BLIND);

            // Post big blind
            Player bigBlindPlayer = players.get(bigBlindIndex);
            double bigBlindAmount = game.getBigBlind();
            processBlind(bigBlindPlayer, bigBlindAmount, playerBets, gameRound, MoveType.BIG_BLIND);

            // Update current bet to the big blind amount
            currentBet = bigBlindAmount;
            bettingRound.setCurrentBet(currentBet);
        }

        // Determine starting player based on betting stage
        int startIndex;
        if (bettingRound.getStage() == BettingStage.PREFLOP) {
            // In preflop, action starts with player after the big blind
            startIndex = (bigBlindIndex + 1) % players.size();
        } else {
            // In other rounds, action starts with the small blind
            startIndex = smallBlindIndex;
        }

        // Create player order starting from the appropriate position
        List<Player> playerOrder = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            playerOrder.add(players.get((startIndex + i) % players.size()));
        }

        // Track which players have acted and matched the current bet
        Map<String, Boolean> playersInRound = new HashMap<>();
        for (Player player : activePlayers) {
            playersInRound.put(player.getUsername(), false);
        }

        // Track the last player to raise
        String lastRaiser = null;

        // Main betting loop
        boolean bettingComplete = false;
        while (!bettingComplete) {
            // Check if all active players have acted and matched bets
            boolean allPlayersActed = true;
            for (Player player : activePlayers) {
                if (!foldedPlayers.contains(player) && !playersInRound.get(player.getUsername())) {
                    allPlayersActed = false;
                    break;
                }
            }

            // If all players have acted and matched bets, end the betting round
            if (allPlayersActed) {
                bettingComplete = true;
                break;
            }

            // Process each player's turn
            for (Player player : playerOrder) {
                // Skip folded players
                if (foldedPlayers.contains(player)) {
                    continue;
                }

                // Skip if this player is the last raiser and everyone else has acted
                if (player.getUsername().equals(lastRaiser) && allPlayersExceptLastRaiserHaveActed(playersInRound, foldedPlayers, activePlayers, lastRaiser)) {
                    bettingComplete = true;
                    break;
                }

                // Calculate amount needed to call
                double toCall = currentBet - playerBets.get(player.getUsername());

                // Process player's action
                boolean actionProcessed = false;
                while (!actionProcessed) {
                    // In a real application, you would wait for player input here
                    // For this implementation, we'll simulate player decisions
                    MoveDto moveDto = getPlayerMove(player, toCall, currentBet);

                    if (moveDto.getType().equals("FOLD")) {
                        // Process fold action
                        player.setStatus(PlayerStatus.FOLDED);
                        foldedPlayers.add(player);
                        activePlayers.remove(player);

                        // Record the move
                        Move move = new Move();
                        move.setType(MoveType.FOLD);
                        move.setAmount(0.0);
                        move.setPlayer(player);
                        move.setBettingRound(bettingRound);
                        bettingRound.getMoves().add(move);

                        // Log and broadcast the action
                        String message = player.getUsername() + " folds.";
                        broadcastGameMessage(game.getId(), message);

                        actionProcessed = true;
                    }
                    else if (moveDto.getType().equals("CHECK")) {
                        // Process check action (only valid if no bet to call)
                        if (toCall == 0) {
                            playersInRound.put(player.getUsername(), true);

                            // Record the move
                            Move move = new Move();
                            move.setType(MoveType.CHECK);
                            move.setAmount(0.0);
                            move.setPlayer(player);
                            move.setBettingRound(bettingRound);
                            bettingRound.getMoves().add(move);

                            // Log and broadcast the action
                            String message = player.getUsername() + " checks.";
                            broadcastGameMessage(game.getId(), message);

                            actionProcessed = true;
                        } else {
                            // Invalid action - cannot check when there's a bet to call
                            String message = "Cannot check! " + player.getUsername() + " must call " + toCall + " or fold.";
                            broadcastGameMessage(game.getId(), message);
                        }
                    }
                    else if (moveDto.getType().equals("CALL")) {
                        // Process call action
                        if (player.getChips() >= toCall) {
                            player.setChips(player.getChips() - toCall);
                            playerBets.put(player.getUsername(), playerBets.get(player.getUsername()) + toCall);
                            pot += toCall;
                            gameRound.setPot(pot);
                            playersInRound.put(player.getUsername(), true);

                            // Record the move
                            Move move = new Move();
                            move.setType(MoveType.CALL);
                            move.setAmount(toCall);
                            move.setPlayer(player);
                            move.setBettingRound(bettingRound);
                            bettingRound.getMoves().add(move);

                            // Log and broadcast the action
                            String message = player.getUsername() + " calls " + toCall + ".";
                            broadcastGameMessage(game.getId(), message);

                            actionProcessed = true;
                        } else {
                            // Player doesn't have enough chips to call
                            String message = player.getUsername() + " doesn't have enough chips to call.";
                            broadcastGameMessage(game.getId(), message);
                        }
                    }
                    else if (moveDto.getType().equals("RAISE")) {
                        // Process raise action
                        double raiseAmount = moveDto.getAmount();
                        double totalBet = toCall + raiseAmount;

                        if (player.getChips() >= totalBet) {
                            player.setChips(player.getChips() - totalBet);
                            playerBets.put(player.getUsername(), playerBets.get(player.getUsername()) + totalBet);
                            pot += totalBet;
                            gameRound.setPot(pot);
                            currentBet += raiseAmount;
                            bettingRound.setCurrentBet(currentBet);

                            // Update last raiser and reset players_in_round
                            lastRaiser = player.getUsername();
                            for (Player p : activePlayers) {
                                if (!p.equals(player) && !foldedPlayers.contains(p)) {
                                    playersInRound.put(p.getUsername(), false);
                                }
                            }
                            playersInRound.put(player.getUsername(), true);

                            // Record the move
                            Move move = new Move();
                            move.setType(MoveType.RAISE);
                            move.setAmount(totalBet);
                            move.setPlayer(player);
                            move.setBettingRound(bettingRound);
                            bettingRound.getMoves().add(move);

                            // Log and broadcast the action
                            String message = player.getUsername() + " raises to " + currentBet + ".";
                            broadcastGameMessage(game.getId(), message);

                            actionProcessed = true;
                        } else {
                            // Player doesn't have enough chips to raise
                            String message = player.getUsername() + " doesn't have enough chips to raise.";
                            broadcastGameMessage(game.getId(), message);
                        }
                    }
                    else if (moveDto.getType().equals("ALL_IN")) {
                        // Process all-in action
                        double allInAmount = player.getChips();
                        player.setChips(0.0);

                        // If all-in amount is more than current bet, treat as a raise
                        if (allInAmount > toCall) {
                            double raiseAmount = allInAmount - toCall;
                            playerBets.put(player.getUsername(), playerBets.get(player.getUsername()) + allInAmount);
                            pot += allInAmount;
                            gameRound.setPot(pot);
                            currentBet += raiseAmount;
                            bettingRound.setCurrentBet(currentBet);

                            // Update last raiser and reset players_in_round
                            lastRaiser = player.getUsername();
                            for (Player p : activePlayers) {
                                if (!p.equals(player) && !foldedPlayers.contains(p)) {
                                    playersInRound.put(p.getUsername(), false);
                                }
                            }
                        } else {
                            // All-in for less than current bet
                            playerBets.put(player.getUsername(), playerBets.get(player.getUsername()) + allInAmount);
                            pot += allInAmount;
                            gameRound.setPot(pot);
                        }

                        playersInRound.put(player.getUsername(), true);

                        // Record the move
                        Move move = new Move();
                        move.setType(MoveType.ALL_IN);
                        move.setAmount(allInAmount);
                        move.setPlayer(player);
                        move.setBettingRound(bettingRound);
                        bettingRound.getMoves().add(move);

                        // Log and broadcast the action
                        String message = player.getUsername() + " goes all-in for " + allInAmount + ".";
                        broadcastGameMessage(game.getId(), message);

                        actionProcessed = true;
                    }

                    // Update the game state after each action
                    updateGameState(game.getId());
                }

                // Check if only one active player remains
                if (activePlayers.size() - foldedPlayers.size() <= 1) {
                    bettingComplete = true;
                    break;
                }
            }
        }

        // Save the final state of the betting round
        //bettingRound = bettingRoundRepository.save(bettingRound);
        //gameRoundRepository.save(gameRound);

        // Log the end of the betting round
        String roundCompleteMessage = "Betting round complete: " + bettingRound.getStage() + ". Pot: " + pot;
        broadcastGameMessage(game.getId(), roundCompleteMessage);

        return bettingRound;
    }

    /**
     * Process posting of blinds
     */
    private void processBlind(Player player, double amount, Map<String, Double> playerBets, GameRound gameRound, MoveType blindType) {
        // Make sure player has enough chips
        double actualAmount = Math.min(amount, player.getChips());

        // Update player chips and bet tracking
        player.setChips(player.getChips() - actualAmount);
        playerBets.put(player.getUsername(), playerBets.get(player.getUsername()) + actualAmount);

        // Update pot
        double pot = gameRound.getPot() + actualAmount;
        gameRound.setPot(pot);

        // Record the move
        Move move = new Move();
        move.setType(blindType);
        move.setAmount(actualAmount);
        move.setPlayer(player);
        move.setBettingRound(gameRound.getCurrentBettingRound());
        gameRound.getCurrentBettingRound().getMoves().add(move);

        // Log the blind
        String blindTypeStr = blindType == MoveType.SMALL_BLIND ? "small blind" : "big blind";
        String message = player.getUsername() + " posts " + blindTypeStr + " of " + actualAmount;
        broadcastGameMessage(gameRound.getGame().getId(), message);

        // Save the player
        playerRepository.save(player);
    }

    /**
     * Check if all players except the last raiser have acted
     */
    private boolean allPlayersExceptLastRaiserHaveActed(Map<String, Boolean> playersInRound, Set<Player> foldedPlayers, Set<Player> activePlayers, String lastRaiser) {
        for (Player player : activePlayers) {
            if (!foldedPlayers.contains(player) && !player.getUsername().equals(lastRaiser) && !playersInRound.get(player.getUsername())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get a player's move (in a real implementation, this would wait for player input)
     * For this example, we'll simulate player decisions
     */
    private MoveDto getPlayerMove(Player player, double toCall, double currentBet) {
        MoveDto moveDto = new MoveDto();

        // Simple AI logic:
        // If no bet to call, check
        if (toCall == 0) {
            moveDto.setType("CHECK");
            return moveDto;
        }

        // If bet to call is more than half of player's chips, consider folding or going all-in
        if (toCall > player.getChips() / 2) {
            // 30% chance to go all-in, 70% chance to fold
            if (Math.random() < 0.3) {
                moveDto.setType("ALL_IN");
            } else {
                moveDto.setType("FOLD");
            }
            return moveDto;
        }

        // Otherwise, 60% chance to call, 30% chance to raise, 10% chance to fold
        double random = Math.random();
        if (random < 0.6) {
            moveDto.setType("CALL");
        } else if (random < 0.9) {
            moveDto.setType("RAISE");
            // Raise between minRaise and half of remaining chips
            double minRaise = Math.max(currentBet, 1.0); // Minimum raise is at least 1 chip
            double maxRaise = player.getChips() / 2;
            double raiseAmount = minRaise + Math.random() * (maxRaise - minRaise);
            moveDto.setAmount(raiseAmount);
        } else {
            moveDto.setType("FOLD");
        }

        return moveDto;
    }

    /**
     * Broadcast a game message to all players
     */
    private void broadcastGameMessage(Long gameId, String message) {
        MessageDto messageDto = new MessageDto();
        messageDto.setType("INFO");
        messageDto.setContent(message);
        messageDto.setTimestamp(LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/games/" + gameId + "/messages", messageDto);
        System.out.println(message); // Log to console for debugging
    }

    /**
     * Update the game state after each action
     */
    private void updateGameState(Long gameId) {
        GameStateDto gameState = getGameState(gameId);
        messagingTemplate.convertAndSend("/topic/games/" + gameId, gameState);
    }

    /**
     * Get the current game state
     * (In a real implementation, this would use a GameService method)
     */
    private GameStateDto getGameState(Long gameId) {
        // This is a placeholder - in a real implementation, you would call a method
        // from your GameService to get the complete game state
        GameStateDto gameState = new GameStateDto();
        gameState.setGameId(gameId);
        // Set other properties...
        return gameState;
    }

    /**
     * Make a move in the betting round
     * (This would be called from a controller when a player makes an action)
     */
    @Transactional
    public GameStateDto makeMove(Long gameId, Long userId, MoveDto moveDto) {
        // Find the game and the current betting round
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        GameRound gameRound = game.getCurrentRound();
        if (gameRound == null) {
            throw new IllegalStateException("No active game round found");
        }

        BettingRound bettingRound = gameRound.getCurrentBettingRound();
        if (bettingRound == null) {
            throw new IllegalStateException("No active betting round found");
        }

        // Find the player
        Player player = playerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Player not found for user ID: " + userId));

        // Validate that it's the player's turn
        // (In a real implementation, you would have a method to check this)

        // Process the move based on its type
        MoveType moveType;
        switch (moveDto.getType()) {
            case "CHECK":
                moveType = MoveType.CHECK;
                break;
            case "CALL":
                moveType = MoveType.CALL;
                break;
            case "RAISE":
                moveType = MoveType.RAISE;
                break;
            case "FOLD":
                moveType = MoveType.FOLD;
                break;
            case "ALL_IN":
                moveType = MoveType.ALL_IN;
                break;
            default:
                throw new InvalidMoveException("Invalid move type: " + moveDto.getType());
        }

        // Create and save the move
        Move move = new Move();
        move.setType(moveType);
        move.setAmount(moveDto.getAmount());
        move.setPlayer(player);
        move.setBettingRound(bettingRound);
        move.setTimestamp(System.currentTimeMillis());

        bettingRound.getMoves().add(move);
        bettingRoundRepository.save(bettingRound);

        // Record the move in the replay
        replayService.recordMove(game, move);

        // Return the updated game state
        return getGameState(gameId);
    }
}