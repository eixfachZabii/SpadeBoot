package com.pokerapp.service;

import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.api.websocket.GameEvent;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.PokerTable;
import com.pokerapp.domain.game.session.GameSession;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import com.pokerapp.exception.InvalidMoveException;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.GameRepository;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.repository.TableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GameService {

    private final TableRepository tableRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // Map to store active game sessions
    private final Map<Long, GameSession> activeSessions = new ConcurrentHashMap<>();

    @Autowired
    public GameService(
            TableRepository tableRepository,
            GameRepository gameRepository,
            PlayerRepository playerRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.tableRepository = tableRepository;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Creates a new poker game at the specified table
     */
    @Transactional
    public Game createGame(Long tableId) {
        PokerTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Table not found with ID: " + tableId));
        
        // Check if there's already an active game at this table
        if (table.getGame() != null) {
            throw new InvalidMoveException("There is already an active game at this table");
        }
        
        // Check if there are enough players
        List<Player> activePlayers = table.getPlayers().stream()
                .filter(player -> player.getStatus() == PlayerStatus.ACTIVE)
                .collect(Collectors.toList());
        
        if (activePlayers.size() < 2) {
            throw new InvalidMoveException("At least 2 active players are required to create a game");
        }
        
        // Create new game
        Game game = new Game();
        game.setPokerTable(table);
        game.setSmallBlind(10); // Default small blind - adjust as needed
        game.setBigBlind(20);   // Default big blind - adjust as needed
        game.setDealerIndex(0); // Start with first player as dealer
        
        Game savedGame = gameRepository.save(game);
        
        // Associate game with table
        table.setGame(savedGame);
        tableRepository.save(table);
        
        // Broadcast game creation event
        sendGameEvent(savedGame.getId(), new GameEvent(
                GameEvent.Type.SYSTEM_MESSAGE,
                "New game created at table " + table.getName(),
                null
        ));
        
        log.info("Created new game {} at table {}", savedGame.getId(), tableId);
        return savedGame;
    }

    /**
     * Starts an existing poker game
     */
    @Transactional
    public void startGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));
        
        PokerTable table = game.getPokerTable();
        if (table == null) {
            throw new NotFoundException("Table not found for game with ID: " + gameId);
        }
        
        // Check if there are enough players
        List<Player> activePlayers = table.getPlayers().stream()
                .filter(player -> player.getStatus() == PlayerStatus.ACTIVE)
                .collect(Collectors.toList());
        
        if (activePlayers.size() < 2) {
            throw new InvalidMoveException("At least 2 active players are required to start a game");
        }
        
        // Check if there's already an active session for this game
        if (activeSessions.containsKey(gameId)) {
            throw new InvalidMoveException("Game is already running");
        }
        
        // Create and start game session
        GameSession session = new GameSession(game, table, messagingTemplate, gameRepository);
        activeSessions.put(gameId, session);
        session.start();
        
        // Broadcast game started event
        sendGameEvent(gameId, new GameEvent(
                GameEvent.Type.GAME_STARTED,
                "Game started at table " + table.getName(),
                null
        ));
        
        log.info("Started game {} at table {}", gameId, table.getId());
    }

    /**
     * Ends the current poker game
     */
    @Transactional
    public void endGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));
        
        // Stop game session if running
        GameSession session = activeSessions.remove(gameId);
        if (session != null) {
            session.stop();
        }
        
        // Remove game from table
        PokerTable table = game.getPokerTable();
        if (table != null) {
            table.setGame(null);
            tableRepository.save(table);
        }
        
        // Delete the game
        gameRepository.delete(game);
        
        // Broadcast game ended event
        if (table != null) {
            sendGameEvent(gameId, new GameEvent(
                    GameEvent.Type.GAME_ENDED,
                    "Game ended at table " + table.getName(),
                    null
            ));
        }
        
        log.info("Ended game {}", gameId);
    }

    /**
     * Gets the current state of a poker game
     */
    public GameStateDto getGameState(Long gameId) {
        // Check if there's an active session for this game
        GameSession session = activeSessions.get(gameId);
        if (session != null) {
            // Use the session's game state
            return createGameStateDto(session);
        }
        
        // If no active session, create a basic state from the database
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));
        
        return createBasicGameStateDto(game);
    }

    /**
     * Handles a player action in an active game
     */
    public GameStateDto handlePlayerAction(Long gameId, Long playerId, GameSession.PlayerAction action, Integer amount) {
        GameSession session = activeSessions.get(gameId);
        if (session == null) {
            throw new InvalidMoveException("Game is not currently active");
        }
        
        // Process the action in the game session
        session.handlePlayerAction(playerId, action, amount);
        
        // Return updated game state
        return createGameStateDto(session);
    }

    /**
     * Creates a detailed game state DTO from an active session
     */
    private GameStateDto createGameStateDto(GameSession session) {
        // The session already has a method to create a game state DTO
        return session.createGameStateDto();
    }

    /**
     * Creates a basic game state DTO from a game entity (for games that aren't running)
     */
    private GameStateDto createBasicGameStateDto(Game game) {
        GameStateDto dto = new GameStateDto();
        dto.setGameId(game.getId());
        
        PokerTable table = game.getPokerTable();
        if (table != null) {
            dto.setTableId(table.getId());
            
            // Include basic player information
            Map<Long, Map<String, Object>> playerInfo = table.getPlayers().stream()
                    .collect(Collectors.toMap(
                            Player::getId,
                            player -> {
                                Map<String, Object> info = new HashMap<>();
                                info.put("id", player.getId());
                                info.put("username", player.getUser().getUsername());
                                info.put("chips", player.getChips());
                                info.put("status", player.getStatus().name());
                                return info;
                            }
                    ));
            dto.setPlayers(playerInfo);
        }
        
        dto.setGameStage(GameSession.GameStage.NOT_STARTED.name());
        dto.setSmallBlind(game.getSmallBlind());
        dto.setBigBlind(game.getBigBlind());
        dto.setDealerPosition(game.getDealerIndex());
        
        return dto;
    }

    /**
     * Sends a game event to clients
     */
    private void sendGameEvent(Long gameId, GameEvent event) {
        messagingTemplate.convertAndSend("/topic/games/" + gameId + "/events", event);
    }

    /**
     * Clean up resources when the service is destroyed
     */
    @PreDestroy
    public void cleanUp() {
        // Stop all active game sessions
        for (GameSession session : activeSessions.values()) {
            try {
                session.stop();
            } catch (Exception e) {
                log.error("Error stopping game session: {}", e.getMessage(), e);
            }
        }
        activeSessions.clear();
    }
}