package com.pokerapp.domain.game.session;

import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.api.websocket.GameEvent;
import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Deck;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.HandEvaluation;
import com.pokerapp.domain.game.PokerTable;
import com.pokerapp.domain.game.Round;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import com.pokerapp.exception.InvalidMoveException;
import com.pokerapp.repository.GameRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class GameSession implements Runnable {

    private final Game game;
    private final PokerTable table;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameRepository gameRepository;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private Round currentRound;
    private Deck deck;
    private List<Card> communityCards = new ArrayList<>();
    private Map<Long, List<Card>> playerCards = new ConcurrentHashMap<>();
    private Map<Long, Integer> playerBets = new ConcurrentHashMap<>();
    private int currentBet = 0;
    private int pot = 0;
    private int smallBlind;
    private int bigBlind;
    private int dealerIndex = 0;
    private int smallBlindIndex = 1;
    private int bigBlindIndex = 2;
    private int currentPlayerIndex;
    private GameStage gameStage = GameStage.NOT_STARTED;
    private long lastActionTime;
    private static final long PLAYER_TIMEOUT_MS = 30000; // 30 seconds
    private Thread gameThread;
    private Map<Long, PlayerGameStatus> playerStatus = new ConcurrentHashMap<>();

    public enum GameStage {
        NOT_STARTED, PREFLOP, FLOP, TURN, RIVER, SHOWDOWN, FINISHED
    }

    public GameSession(Game game, PokerTable table, SimpMessagingTemplate messagingTemplate, 
                       GameRepository gameRepository) {
        this.game = game;
        this.table = table;
        this.messagingTemplate = messagingTemplate;
        this.gameRepository = gameRepository;
        this.smallBlind = game.getSmallBlind();
        this.bigBlind = game.getBigBlind();
        
        // Initialize player statuses
        for (Player player : table.getPlayers()) {
            if (player.getStatus() == PlayerStatus.ACTIVE) {
                playerStatus.put(player.getId(), new PlayerGameStatus());
            }
        }
        
        this.dealerIndex = game.getDealerIndex();
        calculateBlindPositions();
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            gameThread = new Thread(this);
            gameThread.setName("GameSession-" + game.getId());
            gameThread.start();
            log.info("Game session {} started", game.getId());
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (gameThread != null) {
                gameThread.interrupt();
            }
            scheduler.shutdownNow();
            log.info("Game session {} stopped", game.getId());
        }
    }

    public void pause() {
        paused.set(true);
        log.info("Game session {} paused", game.getId());
    }

    public void resume() {
        paused.set(false);
        log.info("Game session {} resumed", game.getId());
    }

    @Override
    public void run() {
        try {
            while (running.get()) {
                if (paused.get()) {
                    Thread.sleep(500);
                    continue;
                }

                if (gameStage == GameStage.NOT_STARTED) {
                    startNewRound();
                } else if (shouldTimeoutCurrentPlayer()) {
                    handlePlayerTimeout();
                } else {
                    Thread.sleep(100); // Small delay to prevent CPU hogging
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Game session {} interrupted", game.getId());
        } catch (Exception e) {
            log.error("Error in game session {}: {}", game.getId(), e.getMessage(), e);
        } finally {
            running.set(false);
        }
    }

    private void startNewRound() {
        // Reset game state
        communityCards.clear();
        playerCards.clear();
        playerBets.clear();
        pot = 0;
        currentBet = 0;
        
        // Create new deck and shuffle
        deck = new Deck();
        deck.initialize();
        
        // Deal cards to active players
        dealPlayerCards();
        
        // Post blinds
        collectBlinds();
        
        // Set initial game stage
        gameStage = GameStage.PREFLOP;
        
        // Set first player to act (after big blind)
        currentPlayerIndex = (bigBlindIndex + 1) % getActivePlayers().size();
        
        // Start tracking action time
        lastActionTime = System.currentTimeMillis();
        
        // Broadcast game state
        broadcastGameState();
        
        log.info("New round started in game {}", game.getId());
    }
    
    private void dealPlayerCards() {
        List<Player> activePlayers = getActivePlayers();
        for (Player player : activePlayers) {
            List<Card> cards = new ArrayList<>();
            cards.add(deck.drawCard());
            cards.add(deck.drawCard());
            playerCards.put(player.getId(), cards);
        }
    }
    
    private void collectBlinds() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.size() < 2) {
            log.error("Not enough active players to collect blinds");
            return;
        }
        
        // Small blind
        Player smallBlindPlayer = activePlayers.get(smallBlindIndex);
        int smallBlindAmount = Math.min(smallBlind, smallBlindPlayer.getChips());
        placeBet(smallBlindPlayer.getId(), smallBlindAmount);
        
        // Big blind
        Player bigBlindPlayer = activePlayers.get(bigBlindIndex);
        int bigBlindAmount = Math.min(bigBlind, bigBlindPlayer.getChips());
        placeBet(bigBlindPlayer.getId(), bigBlindAmount);
        
        // Current bet is the big blind
        currentBet = bigBlindAmount;
    }
    
    private void placeBet(Long playerId, int amount) {
        Player player = findPlayerById(playerId);
        if (player == null) {
            throw new InvalidMoveException("Player not found");
        }
        
        // Check if player has enough chips
        if (player.getChips() < amount) {
            throw new InvalidMoveException("Not enough chips");
        }
        
        // Update player's chips
        player.setChips(player.getChips() - amount);
        
        // Update player's bet
        int currentPlayerBet = playerBets.getOrDefault(playerId, 0);
        playerBets.put(playerId, currentPlayerBet + amount);
        
        // Update pot
        pot += amount;
        
        // Update last action time
        lastActionTime = System.currentTimeMillis();
    }
    
    public void handlePlayerAction(Long playerId, PlayerAction action, int betAmount) {
        // Verify it's this player's turn
        Player player = findPlayerById(playerId);
        if (player == null) {
            throw new InvalidMoveException("Player not found");
        }
        
        List<Player> activePlayers = getActivePlayers();
        Player currentPlayer = activePlayers.get(currentPlayerIndex);
        
        if (!currentPlayer.getId().equals(playerId)) {
            throw new InvalidMoveException("Not your turn");
        }
        
        // Process action
        switch (action) {
            case FOLD:
                handleFold(playerId);
                break;
            case CHECK:
                handleCheck(playerId);
                break;
            case CALL:
                handleCall(playerId);
                break;
            case RAISE:
                handleRaise(playerId, betAmount);
                break;
            case ALL_IN:
                handleAllIn(playerId);
                break;
        }
        
        // Move to next player or stage
        advanceGame();
        
        // Broadcast updated game state
        broadcastGameState();
    }
    
    private void handleFold(Long playerId) {
        Player player = findPlayerById(playerId);
        player.setStatus(PlayerStatus.FOLDED);
        playerStatus.get(playerId).setFolded(true);
        log.info("Player {} folded", playerId);
    }
    
    private void handleCheck(Long playerId) {
        int playerBet = playerBets.getOrDefault(playerId, 0);
        if (playerBet < currentBet) {
            throw new InvalidMoveException("Cannot check, must call or raise");
        }
        log.info("Player {} checked", playerId);
    }
    
    private void handleCall(Long playerId) {
        Player player = findPlayerById(playerId);
        int playerBet = playerBets.getOrDefault(playerId, 0);
        int callAmount = Math.min(currentBet - playerBet, player.getChips());
        
        if (callAmount <= 0) {
            throw new InvalidMoveException("Nothing to call");
        }
        
        placeBet(playerId, callAmount);
        log.info("Player {} called {}", playerId, callAmount);
    }
    
    private void handleRaise(Long playerId, int raiseAmount) {
        Player player = findPlayerById(playerId);
        int playerBet = playerBets.getOrDefault(playerId, 0);
        
        // Minimum raise is at least the big blind
        if (raiseAmount < bigBlind || playerBet + raiseAmount <= currentBet) {
            throw new InvalidMoveException("Raise amount too small");
        }
        
        // Check if player has enough chips
        if (player.getChips() < raiseAmount) {
            throw new InvalidMoveException("Not enough chips to raise");
        }
        
        placeBet(playerId, raiseAmount);
        currentBet = playerBet + raiseAmount;
        
        // Reset player actions since there's a new bet
        for (Map.Entry<Long, PlayerGameStatus> entry : playerStatus.entrySet()) {
            if (!entry.getValue().isFolded() && !entry.getKey().equals(playerId)) {
                entry.getValue().setActedInCurrentStage(false);
            }
        }
        
        log.info("Player {} raised to {}", playerId, currentBet);
    }
    
    private void handleAllIn(Long playerId) {
        Player player = findPlayerById(playerId);
        int allInAmount = player.getChips();
        
        if (allInAmount <= 0) {
            throw new InvalidMoveException("Already all-in");
        }
        
        placeBet(playerId, allInAmount);
        
        int playerTotalBet = playerBets.get(playerId);
        if (playerTotalBet > currentBet) {
            currentBet = playerTotalBet;
            
            // Reset player actions since there's a new bet
            for (Map.Entry<Long, PlayerGameStatus> entry : playerStatus.entrySet()) {
                if (!entry.getValue().isFolded() && !entry.getKey().equals(playerId)) {
                    entry.getValue().setActedInCurrentStage(false);
                }
            }
        }
        
        playerStatus.get(playerId).setAllIn(true);
        log.info("Player {} is all-in with {}", playerId, allInAmount);
    }
    
    private void advanceGame() {
        // Mark current player as acted
        List<Player> activePlayers = getActivePlayers();
        Player currentPlayer = activePlayers.get(currentPlayerIndex);
        playerStatus.get(currentPlayer.getId()).setActedInCurrentStage(true);
        
        // Check if round is over (all players have acted)
        boolean roundComplete = true;
        for (Player player : activePlayers) {
            PlayerGameStatus status = playerStatus.get(player.getId());
            if (!status.isFolded() && !status.isAllIn() && 
                (!status.hasActedInCurrentStage() || playerBets.getOrDefault(player.getId(), 0) < currentBet)) {
                roundComplete = false;
                break;
            }
        }
        
        if (roundComplete) {
            // Move to next stage
            advanceGameStage();
        } else {
            // Move to next player
            do {
                currentPlayerIndex = (currentPlayerIndex + 1) % activePlayers.size();
                Player nextPlayer = activePlayers.get(currentPlayerIndex);
                PlayerGameStatus nextPlayerStatus = playerStatus.get(nextPlayer.getId());
                
                // Skip folded or all-in players
                if (nextPlayerStatus.isFolded() || nextPlayerStatus.isAllIn()) {
                    continue;
                }
                
                // If this player has already acted and has the correct bet, 
                // and all other players have acted or folded or are all-in, end the round
                if (nextPlayerStatus.hasActedInCurrentStage() && 
                    playerBets.getOrDefault(nextPlayer.getId(), 0) == currentBet) {
                    boolean allPlayersActed = true;
                    for (Player p : activePlayers) {
                        PlayerGameStatus pStatus = playerStatus.get(p.getId());
                        if (!pStatus.isFolded() && !pStatus.isAllIn() && 
                            (!pStatus.hasActedInCurrentStage() || 
                             playerBets.getOrDefault(p.getId(), 0) < currentBet)) {
                            allPlayersActed = false;
                            break;
                        }
                    }
                    
                    if (allPlayersActed) {
                        advanceGameStage();
                        return;
                    }
                }
                
                break;
            } while (true);
        }
        
        // Reset action timer
        lastActionTime = System.currentTimeMillis();
    }
    
    private void advanceGameStage() {
        // Reset player actions for the new stage
        for (PlayerGameStatus status : playerStatus.values()) {
            status.setActedInCurrentStage(false);
        }
        
        switch (gameStage) {
            case PREFLOP:
                gameStage = GameStage.FLOP;
                dealFlop();
                break;
            case FLOP:
                gameStage = GameStage.TURN;
                dealTurn();
                break;
            case TURN:
                gameStage = GameStage.RIVER;
                dealRiver();
                break;
            case RIVER:
                gameStage = GameStage.SHOWDOWN;
                handleShowdown();
                break;
            case SHOWDOWN:
                gameStage = GameStage.FINISHED;
                finishGame();
                break;
            case FINISHED:
                prepareNextRound();
                break;
            default:
                break;
        }
        
        // Set first player for the new round
        // After the preflop, the first active player after the dealer acts first
        if (gameStage != GameStage.FINISHED && gameStage != GameStage.NOT_STARTED) {
            List<Player> activePlayers = getActivePlayers();
            currentPlayerIndex = (dealerIndex + 1) % activePlayers.size();
            
            // Skip players who are folded or all-in
            while (activePlayers.size() > 0) {
                Player player = activePlayers.get(currentPlayerIndex);
                PlayerGameStatus status = playerStatus.get(player.getId());
                
                if (status.isFolded() || status.isAllIn()) {
                    currentPlayerIndex = (currentPlayerIndex + 1) % activePlayers.size();
                } else {
                    break;
                }
            }
        }
    }
    
    private void dealFlop() {
        // Burn one card
        deck.drawCard();
        
        // Deal three community cards
        communityCards.add(deck.drawCard());
        communityCards.add(deck.drawCard());
        communityCards.add(deck.drawCard());
        
        log.info("Flop dealt: {}", communityCards);
    }
    
    private void dealTurn() {
        // Burn one card
        deck.drawCard();
        
        // Deal one community card
        communityCards.add(deck.drawCard());
        
        log.info("Turn dealt: {}", communityCards.get(3));
    }
    
    private void dealRiver() {
        // Burn one card
        deck.drawCard();
        
        // Deal one community card
        communityCards.add(deck.drawCard());
        
        log.info("River dealt: {}", communityCards.get(4));
    }
    
    private void handleShowdown() {
        // Evaluate hands and determine winner(s)
        Map<Long, Long> playerHandRankings = new HashMap<>();
        Map<Long, String> playerHandDescriptions = new HashMap<>();
        
        for (Player player : getActivePlayers()) {
            if (playerStatus.get(player.getId()).isFolded()) {
                continue;
            }
            
            List<Card> playerHand = new ArrayList<>(playerCards.get(player.getId()));
            playerHand.addAll(communityCards);
            
            long handRank = HandEvaluation.cardsToRankNumber(playerHand);
            String handDescription = HandEvaluation.cardsToRankString(playerHand);
            
            playerHandRankings.put(player.getId(), handRank);
            playerHandDescriptions.put(player.getId(), handDescription);
        }
        
        // Find the highest hand rank
        Optional<Map.Entry<Long, Long>> highestHand = playerHandRankings.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());
        
        if (highestHand.isPresent()) {
            // Find all players with the highest hand (in case of a tie)
            List<Long> winners = playerHandRankings.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().equals(highestHand.get().getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            // Distribute pot equally among winners
            int winAmount = pot / winners.size();
            
            for (Long winnerId : winners) {
                Player winner = findPlayerById(winnerId);
                winner.setChips(winner.getChips() + winAmount);
                
                // Send win notification
                sendGameEvent(new GameEvent(
                    GameEvent.Type.ROUND_RESULT,
                    "Player " + winner.getUser().getUsername() + " wins with " + 
                    playerHandDescriptions.get(winnerId),
                    winnerId
                ));
            }
            
            // Handle remainder chips (give to first winner)
            int remainder = pot % winners.size();
            if (remainder > 0 && !winners.isEmpty()) {
                Player firstWinner = findPlayerById(winners.get(0));
                firstWinner.setChips(firstWinner.getChips() + remainder);
            }
        }
        
        // Wait a few seconds before next round
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void finishGame() {
        // Update dealer position for next game
        moveDealerButton();
        
        // Save game state
        saveGameState();
        
        // Wait a few seconds before starting new round
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        prepareNextRound();
    }
    
    private void prepareNextRound() {
        // Check if there are enough players to continue
        List<Player> activePlayers = table.getPlayers().stream()
                .filter(player -> player.getStatus() == PlayerStatus.ACTIVE && player.getChips() > 0)
                .collect(Collectors.toList());
        
        if (activePlayers.size() < 2) {
            gameStage = GameStage.FINISHED;
            sendGameEvent(new GameEvent(
                GameEvent.Type.GAME_ENDED,
                "Not enough players to continue",
                null
            ));
            return;
        }
        
        // Reset game state for new round
        gameStage = GameStage.NOT_STARTED;
        
        // Reset player statuses
        for (PlayerGameStatus status : playerStatus.values()) {
            status.reset();
        }
        
        // Reset any folded players back to active
        for (Player player : table.getPlayers()) {
            if (player.getStatus() == PlayerStatus.FOLDED) {
                player.setStatus(PlayerStatus.ACTIVE);
            }
        }
        
        // Broadcast that a new round is about to start
        sendGameEvent(new GameEvent(
            GameEvent.Type.NEW_ROUND,
            "New round starting",
            null
        ));
    }
    
    private void moveDealerButton() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.size() < 2) {
            return;
        }
        
        dealerIndex = (dealerIndex + 1) % activePlayers.size();
        calculateBlindPositions();
        
        // Update game entity
        game.setDealerIndex(dealerIndex);
        saveGameState();
        
        log.info("Dealer moved to position {}", dealerIndex);
    }
    
    private void calculateBlindPositions() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.size() < 2) {
            return;
        }
        
        smallBlindIndex = (dealerIndex + 1) % activePlayers.size();
        bigBlindIndex = (dealerIndex + 2) % activePlayers.size();
        
        // Special case for heads-up (2 players)
        if (activePlayers.size() == 2) {
            smallBlindIndex = dealerIndex;
            bigBlindIndex = (dealerIndex + 1) % 2;
        }
    }
    
    private List<Player> getActivePlayers() {
        return table.getPlayers().stream()
                .filter(player -> player.getStatus() != PlayerStatus.SITTING_OUT)
                .collect(Collectors.toList());
    }
    
    private Player findPlayerById(Long playerId) {
        return table.getPlayers().stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }
    
    private boolean shouldTimeoutCurrentPlayer() {
        if (gameStage == GameStage.NOT_STARTED || 
            gameStage == GameStage.FINISHED || 
            gameStage == GameStage.SHOWDOWN) {
            return false;
        }
        
        return System.currentTimeMillis() - lastActionTime > PLAYER_TIMEOUT_MS;
    }
    
    private void handlePlayerTimeout() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.isEmpty() || currentPlayerIndex >= activePlayers.size()) {
            return;
        }
        
        Player currentPlayer = activePlayers.get(currentPlayerIndex);
        log.info("Player {} timed out", currentPlayer.getId());
        
        // If player can check, do that, otherwise fold
        int playerBet = playerBets.getOrDefault(currentPlayer.getId(), 0);
        if (playerBet >= currentBet) {
            handleCheck(currentPlayer.getId());
        } else {
            handleFold(currentPlayer.getId());
        }
        
        // Move to next player or stage
        advanceGame();
        
        // Broadcast updated game state
        broadcastGameState();
        
        // Send timeout notification
        sendGameEvent(new GameEvent(
            GameEvent.Type.PLAYER_TIMEOUT,
            "Player " + currentPlayer.getUser().getUsername() + " timed out",
            currentPlayer.getId()
        ));
    }
    
    private void broadcastGameState() {
        GameStateDto gameState = createGameStateDto();
        messagingTemplate.convertAndSend("/topic/games/" + game.getId(), gameState);
    }
    
    private void sendGameEvent(GameEvent event) {
        messagingTemplate.convertAndSend("/topic/games/" + game.getId() + "/events", event);
    }
    
    public GameStateDto createGameStateDto() {
        GameStateDto dto = new GameStateDto();
        dto.setGameId(game.getId());
        dto.setTableId(table.getId());
        dto.setGameStage(gameStage.name());
        dto.setPot(pot);
        dto.setCurrentBet(currentBet);
        dto.setSmallBlind(smallBlind);
        dto.setBigBlind(bigBlind);
        
        // Community cards
        dto.setCommunityCards(communityCards);
        
        // Player information
        List<Player> activePlayers = getActivePlayers();
        Map<Long, Map<String, Object>> playerInfo = new HashMap<>();
        
        for (Player player : activePlayers) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", player.getId());
            info.put("username", player.getUser().getUsername());
            info.put("chips", player.getChips());
            info.put("status", player.getStatus().name());
            info.put("bet", playerBets.getOrDefault(player.getId(), 0));
            
            // Only include player cards for the player themselves or at showdown
            if (gameStage == GameStage.SHOWDOWN && !playerStatus.get(player.getId()).isFolded()) {
                info.put("cards", playerCards.get(player.getId()));
            }
            
            playerInfo.put(player.getId(), info);
        }
        dto.setPlayers(playerInfo);
        
        // Dealer and blinds positions
        dto.setDealerPosition(dealerIndex);
        dto.setSmallBlindPosition(smallBlindIndex);
        dto.setBigBlindPosition(bigBlindIndex);
        
        // Current player to act
        if (gameStage != GameStage.NOT_STARTED && 
            gameStage != GameStage.FINISHED && 
            gameStage != GameStage.SHOWDOWN &&
            currentPlayerIndex < activePlayers.size()) {
            dto.setCurrentPlayerId(activePlayers.get(currentPlayerIndex).getId());
        }
        
        return dto;
    }
    
    private void saveGameState() {
        gameRepository.save(game);
    }
    
    public enum PlayerAction {
        FOLD, CHECK, CALL, RAISE, ALL_IN
    }
    
    private static class PlayerGameStatus {
        @Getter
        private boolean folded = false;
        
        @Getter
        private boolean allIn = false;
        
        @Getter
        private boolean actedInCurrentStage = false;
        
        public void setFolded(boolean folded) {
            this.folded = folded;
        }
        
        public void setAllIn(boolean allIn) {
            this.allIn = allIn;
        }
        
        public void setActedInCurrentStage(boolean acted) {
            this.actedInCurrentStage = acted;
        }
        
        public boolean hasActedInCurrentStage() {
            return actedInCurrentStage;
        }
        
        public void reset() {
            folded = false;
            actedInCurrentStage = false;
            // Note: We do not reset allIn state, as it persists between rounds
        }
    }
}