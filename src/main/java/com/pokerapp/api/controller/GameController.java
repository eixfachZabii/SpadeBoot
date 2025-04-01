package com.pokerapp.api.controller;

import com.pokerapp.domain.game.PokerTable;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.repository.TableRepository;
import com.pokerapp.service.UserService;
import com.pokerapp.session.GameSession;
import com.pokerapp.websocket.TableWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST controller for managing poker games.
 */
@RestController
@RequestMapping("/api/games")
public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TableWebSocketHandler webSocketHandler;

    // Map to store active game sessions by table ID
    private final Map<Long, GameSession> activeGames = new ConcurrentHashMap<>();


    @PostMapping("/tables/{tableId}/start")
    public ResponseEntity<?> startGame(
            @PathVariable Long tableId,
            @RequestParam(defaultValue = "20") int bigBlind) {

        // Get the current user
        User currentUser = userService.getCurrentUser();

        // Get the poker table
        PokerTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Table not found with ID: " + tableId));

        // Verify the current user is the table owner
        if (!table.getOwner().getUserId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Only the table owner can start a game")
            );
        }

        // Check if there are enough players
        if (table.getPlayers().size() < 2) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Need at least 2 players to start a game")
            );
        }

        // Check if a game is already in progress
        if (activeGames.containsKey(tableId)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "A game is already in progress at this table")
            );
        }

        try {
            // Get all players from the table
            List<Player> players = new ArrayList<>(table.getPlayers());

            // Create a new game session
            GameSession gameSession = new GameSession();

            // Store the game session
            activeGames.put(tableId, gameSession);

            // Start the game
            gameSession.startGame(bigBlind);

            // Notify all connected clients that the game has started
            Map<String, Object> gameStartedMessage = new HashMap<>();
            gameStartedMessage.put("type", "GAME_STARTED");
            gameStartedMessage.put("tableId", tableId);
            gameStartedMessage.put("bigBlind", bigBlind);
            gameStartedMessage.put("playerCount", players.size());

            webSocketHandler.sendToTable(tableId,
                    gameStartedMessage);

            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Game started successfully");
            response.put("tableId", tableId);
            response.put("bigBlind", bigBlind);
            response.put("playerCount", players.size());

            logger.info("Game started at table {} with {} players and big blind {}",
                    tableId, players.size(), bigBlind);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error starting game at table {}: {}", tableId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to start game: " + e.getMessage())
            );
        }
    }


    @PostMapping("/tables/{tableId}/end")
    public ResponseEntity<?> endGame(@PathVariable Long tableId) {
        // Get the current user
        User currentUser = userService.getCurrentUser();

        // Get the poker table
        PokerTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Table not found with ID: " + tableId));

        // Verify the current user is the table owner
        if (!table.getOwner().getUserId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Only the table owner can end a game")
            );
        }

        // Check if a game is in progress
        GameSession gameSession = activeGames.get(tableId);
        if (gameSession == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "No active game at this table")
            );
        }

        try {
            // End the game session
            gameSession.interrupt();
            activeGames.remove(tableId);

            // Notify all connected clients that the game has ended
            Map<String, Object> gameEndedMessage = new HashMap<>();
            gameEndedMessage.put("type", "GAME_ENDED");
            gameEndedMessage.put("tableId", tableId);

            webSocketHandler.sendToTable(tableId,
                    gameEndedMessage);

            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Game ended successfully");
            response.put("tableId", tableId);

            logger.info("Game ended at table {}", tableId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error ending game at table {}: {}", tableId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to end game: " + e.getMessage())
            );
        }
    }


    @GetMapping("/tables/{tableId}/status")
    public ResponseEntity<?> getGameStatus(@PathVariable Long tableId) {
        // Check if a game is in progress
        boolean gameActive = activeGames.containsKey(tableId);

        Map<String, Object> response = new HashMap<>();
        response.put("tableId", tableId);
        response.put("gameActive", gameActive);

        return ResponseEntity.ok(response);
    }
}