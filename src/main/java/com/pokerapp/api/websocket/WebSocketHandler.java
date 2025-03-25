package com.pokerapp.api.websocket;

import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.repository.TableRepository;
import com.pokerapp.service.TableService;
import com.pokerapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles WebSocket communication for the poker game.
 * Manages player connections, table state, and game events.
 */
@Controller
public class WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private TableService tableService;

    // Maps: tableId -> Map<playerId, sessionId>
    private final Map<Long, Map<Long, String>> tablePlayerSessions = new ConcurrentHashMap<>();

    // Maps: sessionId -> TablePlayerInfo
    private final Map<String, TablePlayerInfo> sessionRegistry = new ConcurrentHashMap<>();


    @MessageMapping("/tables/{tableId}/connect")
    public void handleTableConnect(
            @DestinationVariable Long tableId,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {

        String sessionId = headerAccessor.getSessionId();
        User currentUser = userService.getCurrentUser();

        // Find the player associated with this user
        Optional<Player> playerOpt = playerRepository.findByUserId(currentUser.getId());
        if (playerOpt.isEmpty()) {
            logger.error("Player not found for user: {}", currentUser.getUsername());
            sendErrorToClient(principal.getName(), "Player not found");
            return;
        }

        Player player = playerOpt.get();

        // Verify player is actually at this table
        if (player.getCurrentTableId() == null || !player.getCurrentTableId().equals(tableId)) {
            logger.error("Player {} attempted to connect to table {} but isn't registered there",
                    player.getId(), tableId);
            sendErrorToClient(principal.getName(), "You are not a member of this table");
            return;
        }

        // Register this session
        registerPlayerSession(tableId, player.getId(), sessionId, principal.getName());

        // Notify all clients about the new player
        Map<String, Object> connectMessage = new HashMap<>();
        connectMessage.put("type", "PLAYER_CONNECTED");
        connectMessage.put("playerId", player.getId());
        connectMessage.put("playerName", currentUser.getUsername());

        messagingTemplate.convertAndSend(
                "/topic/tables/" + tableId + "/players",
                connectMessage);

        logger.info("Player {} connected to table {}", player.getId(), tableId);
    }

    private void sendErrorToClient(String username, String errorMessage) {
        Map<String, Object> errorPayload = new HashMap<>();
        errorPayload.put("type", "ERROR");
        errorPayload.put("message", errorMessage);

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/errors",
                errorPayload);
    }



    @MessageMapping("/tables/{tableId}/disconnect")
    public void handleTableDisconnect(
            @DestinationVariable Long tableId,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        handleSessionDisconnect(sessionId);
    }


    @MessageMapping("/tables/{tableId}/message")
    public void handleTableMessage(
            @DestinationVariable Long tableId,
            @Payload Object message) {

        // Broadcast message to all players at this table
        messagingTemplate.convertAndSend(
                "/topic/tables/" + tableId + "/messages",
                message);
    }



    @MessageMapping("/tables/{tableId}/action")
    public void handlePlayerAction(
            @DestinationVariable Long tableId,
            @Payload Map<String, Object> action,
            Principal principal) {

        // Validate the player belongs to this table
        User currentUser = userService.getCurrentUser();
        Player player = playerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Player not found for user"));

        if (player.getCurrentTableId() == null || !player.getCurrentTableId().equals(tableId)) {
            logger.error("Player {} tried to act on table {} but isn't seated there",
                    player.getId(), tableId);
            return;
        }

        // Add player information to the action
        action.put("playerId", player.getId());
        action.put("playerName", currentUser.getUsername());

        // Broadcast the action to all players at this table
        messagingTemplate.convertAndSend(
                "/topic/tables/" + tableId + "/actions",
                action);
    }



    private void registerPlayerSession(Long tableId, Long playerId, String sessionId, String username) {
        // Store in table -> player mapping
        tablePlayerSessions.computeIfAbsent(tableId, k -> new ConcurrentHashMap<>())
                .put(playerId, sessionId);

        // Store in session registry for easy lookup on disconnect
        TablePlayerInfo info = new TablePlayerInfo(tableId, playerId, username);
        sessionRegistry.put(sessionId, info);
    }



    private void deregisterPlayerSession(String sessionId) {
        TablePlayerInfo info = sessionRegistry.remove(sessionId);
        if (info != null) {
            Map<Long, String> tablePlayers = tablePlayerSessions.get(info.tableId);
            if (tablePlayers != null) {
                tablePlayers.remove(info.playerId);

                // If table has no more players, remove it from the map
                if (tablePlayers.isEmpty()) {
                    tablePlayerSessions.remove(info.tableId);
                }
            }
        }
    }



    public void handlePlayerDisconnection(String sessionId) {
        TablePlayerInfo info = sessionRegistry.get(sessionId);
        if (info == null) {
            return; // Session wasn't registered
        }

        logger.info("Player {} disconnected from table {}", info.playerId, info.tableId);

        // Notify all clients about the player disconnection
        Map<String, Object> disconnectMessage = new HashMap<>();
        disconnectMessage.put("type", "PLAYER_DISCONNECTED");
        disconnectMessage.put("playerId", info.playerId);
        disconnectMessage.put("playerName", info.username);

        messagingTemplate.convertAndSend(
                "/topic/tables/" + info.tableId + "/players",
                disconnectMessage);

        // Clean up session registrations
        deregisterPlayerSession(sessionId);

        // Remove player from the actual table in the database
        try {
            Player player = playerRepository.findById(info.playerId).orElse(null);
            if (player != null && player.getCurrentTableId() != null &&
                    player.getCurrentTableId().equals(info.tableId)) {

                tableService.leaveTable(info.tableId, player.getUserId());
                logger.info("Player {} removed from table {}", info.playerId, info.tableId);
            }
        } catch (Exception e) {
            logger.error("Error removing player {} from table {}: {}",
                    info.playerId, info.tableId, e.getMessage());
        }
    }



    public void handleSessionDisconnect(String sessionId) {
        handlePlayerDisconnection(sessionId);
    }



    public void sendToTable(Long tableId, String destination, Object message) {
        messagingTemplate.convertAndSend(destination, message);
    }



    public void sendToPlayer(Long playerId, Object message) {

        // Search for the player in all tables
        for (Map.Entry<Long, Map<Long, String>> tableEntry : tablePlayerSessions.entrySet()) {
            Map<Long, String> players = tableEntry.getValue();
            String username = null;

            // Find the player's session in this table
            for (Map.Entry<String, TablePlayerInfo> entry : sessionRegistry.entrySet()) {
                if (entry.getValue().playerId.equals(playerId)) {
                    username = entry.getValue().username;
                    break;
                }
            }

            if (username != null) {
                // Send to the specific user
                messagingTemplate.convertAndSendToUser(
                        username,
                        "/queue/private",
                        message);
                return;
            }
        }
    }



    public boolean isPlayerConnectedToTable(Long tableId, Long playerId) {
        Map<Long, String> tablePlayers = tablePlayerSessions.get(tableId);
        return tablePlayers != null && tablePlayers.containsKey(playerId);
    }



    public Map<Long, String> getPlayersInTable(Long tableId) {
        return tablePlayerSessions.getOrDefault(tableId, new ConcurrentHashMap<>());
    }


    private record TablePlayerInfo(Long tableId, Long playerId, String username) {
    }
}