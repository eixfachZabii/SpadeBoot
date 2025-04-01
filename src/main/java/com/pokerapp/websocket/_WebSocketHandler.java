package com.pokerapp.api.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handles WebSocket communication for poker tables.
 * This class manages player connections, routes messages, and provides
 * utilities for table-based communication patterns.
 *
 * Key responsibilities:
 * 1. Track which players are connected to which tables
 * 2. Send messages to tables or specific players
 * 3. Handle request-response patterns with timeout support
 * 4. Manage player disconnection
 *
@Controller
public class _WebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(_WebSocketHandler.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private SimpUserRegistry simpUserRegistry;

    // Maps: tableId -> Map<playerId, playerInfo>
    // Tracks which players are connected to which tables and their session info
    private final Map<Long, Map<Long, PlayerInfo>> tablePlayerMap = new ConcurrentHashMap<>();

    // Maps: sessionId -> TablePlayerInfo
    // Enables quick lookup of table and player info from a session ID (for disconnects)
    private final Map<String, TablePlayerInfo> sessionRegistry = new ConcurrentHashMap<>();

    // Maps: requestId -> Future<Response>
    // Tracks pending requests waiting for client responses
    private final Map<String, CompletableFuture<Map<String, Object>>> pendingResponses = new ConcurrentHashMap<>();

    /**
     * Handles messages sent by clients to a table.
     * Routes messages appropriately and handles responses to pending requests.
     *
     * @param tableId The table ID the message is for
     * @param message The message content
     * @param headerAccessor Message headers including session info
     *
    @MessageMapping("/tables/{tableId}/message")
    public void handleTableMessage(
            @DestinationVariable Long tableId,
            @Payload Map<String, Object> message,
            SimpMessageHeaderAccessor headerAccessor) {

        // If this is a response to a pending request, complete the future
        if (message.containsKey("responseId")) {
            String responseId = (String) message.get("responseId");
            CompletableFuture<Map<String, Object>> future = pendingResponses.remove(responseId);
            if (future != null) {
                future.complete(message);
                return; // Don't broadcast response messages
            }
        }

        // For regular messages, forward to all players at the table
        sendToTable(tableId, message);
    }

    /**
     * Registers a new player connection and notifies other players at the table.
     * Called by the TableMembershipInterceptor when a player subscribes to a table topic.
     *
     * @param tableId The table ID
     * @param playerId The player ID
     * @param sessionId The WebSocket session ID
     * @param principalName The principal name (for user-specific messaging)
     * @param username The display name for the player
     *
    public void registerPlayerConnection(Long tableId, Long playerId, String sessionId,
                                         String principalName, String username) {
        // Create player info record
        PlayerInfo playerInfo = new PlayerInfo(sessionId, principalName, username);

        // Store in table->player mapping
        tablePlayerMap.computeIfAbsent(tableId, k -> new ConcurrentHashMap<>())
                .put(playerId, playerInfo);

        // Store in session registry for quick lookup during disconnects
        TablePlayerInfo tablePlayerInfo = new TablePlayerInfo(tableId, playerId, username);
        sessionRegistry.put(sessionId, tablePlayerInfo);

        // Notify all clients at the table about the new player
        Map<String, Object> connectMessage = new HashMap<>();
        connectMessage.put("type", "PLAYER_CONNECTED");
        connectMessage.put("playerId", playerId);
        connectMessage.put("playerName", username);

        sendToTable(tableId, connectMessage);

        logger.info("Player {} ({}) connected to table {}", playerId, username, tableId);
    }

    /**
     * Handles a player disconnection.
     * Removes the session from tracking and notifies other players at the table.
     * Called by WebSocketEventListener when a client disconnects.
     *
     * @param sessionId The WebSocket session ID that disconnected
     *
    public void handlePlayerDisconnection(String sessionId) {
        TablePlayerInfo info = sessionRegistry.get(sessionId);
        if (info == null) {
            return; // Session wasn't registered with any table
        }

        logger.info("Player {} disconnected from table {}", info.playerId, info.tableId);

        // Notify all clients about the player disconnection
        Map<String, Object> disconnectMessage = new HashMap<>();
        disconnectMessage.put("type", "PLAYER_DISCONNECTED");
        disconnectMessage.put("playerId", info.playerId);
        disconnectMessage.put("playerName", info.username);

        sendToTable(info.tableId, disconnectMessage);

        // Clean up session registrations
        deregisterPlayerSession(sessionId);
    }

    /**
     * Removes a player's session registration.
     * Called during player disconnection to clean up tracking maps.
     *
     * @param sessionId The WebSocket session ID to deregister
     *
    private void deregisterPlayerSession(String sessionId) {
        TablePlayerInfo info = sessionRegistry.remove(sessionId);
        if (info != null) {
            Map<Long, PlayerInfo> tablePlayers = tablePlayerMap.get(info.tableId);
            if (tablePlayers != null) {
                tablePlayers.remove(info.playerId);

                // If table has no more players, remove it from the map
                if (tablePlayers.isEmpty()) {
                    tablePlayerMap.remove(info.tableId);
                }
            }
        }
    }

    /**
     * Sends a message to all players at a specific table.
     *
     * @param tableId The table ID to send to
     * @param message The message to send
     *
    public void sendToTable(Long tableId, Object message) {
        messagingTemplate.convertAndSend(
                "/topic/tables/" + tableId + "/messages",
                message);
    }

    /**
     * Sends a message to a specific player.
     *
     * @param tableId The table ID
     * @param playerId The player ID to send to
     * @param message The message to send
     * @return True if message was sent, false if player not found
     *
    public boolean sendToPlayer(Long tableId, Long playerId, Object message) {
        Map<Long, PlayerInfo> tablePlayers = tablePlayerMap.get(tableId);
        if (tablePlayers == null) return false;

        PlayerInfo playerInfo = tablePlayers.get(playerId);
        if (playerInfo == null) return false;

        // Send to the specific user using their principal name
        messagingTemplate.convertAndSendToUser(
                playerInfo.principalName,
                "/queue/private",
                message);
        return true;
    }

    /**
     * Sends a message to a player and waits for their response with a timeout.
     * Implements a request-response pattern over WebSockets.
     *
     * @param tableId The table ID
     * @param playerId The player ID to send to
     * @param message The message to send
     * @param timeoutSeconds Maximum time to wait for response in seconds
     * @return The player's response as a Map
     * @throws TimeoutException If player doesn't respond within timeout period
     * @throws RuntimeException If player is not connected or other errors occur
     *
    public Map<String, Object> waitForPlayerResponse(Long tableId, Long playerId,
                                                     Map<String, Object> message,
                                                     int timeoutSeconds)
            throws TimeoutException {

        // Generate a unique request ID to track this specific request
        String requestId = UUID.randomUUID().toString();

        // Create a CompletableFuture to wait for the response
        CompletableFuture<Map<String, Object>> responseFuture = new CompletableFuture<>();
        pendingResponses.put(requestId, responseFuture);

        // Add request ID to message so client knows to include it in response
        message.put("requestId", requestId);

        // Send message to player
        boolean sent = sendToPlayer(tableId, playerId, message);
        if (!sent) {
            pendingResponses.remove(requestId);
            throw new RuntimeException("Player not connected or not found at table");
        }

        try {
            // Wait for response with timeout
            return responseFuture.orTimeout(timeoutSeconds, TimeUnit.SECONDS).get();
        } catch (Exception e) {
            pendingResponses.remove(requestId);
            throw new RuntimeException("Error waiting for player response", e);
        }
    }

    /**
     * Requests a client to disconnect by sending them a disconnect command.
     * The client application must listen for this message and close their connection.
     *
     * @param tableId The table ID
     * @param playerId The player ID to disconnect
     * @param reason Optional reason for disconnection
     * @return True if disconnect command was sent, false if player not found
     *
    public boolean disconnectPlayer(Long tableId, Long playerId, String reason) {
        Map<Long, PlayerInfo> tablePlayers = tablePlayerMap.get(tableId);
        if (tablePlayers == null) return false;

        PlayerInfo playerInfo = tablePlayers.get(playerId);
        if (playerInfo == null) return false;

        try {
            // Send a disconnect command message to the client
            Map<String, Object> disconnectCommand = new HashMap<>();
            disconnectCommand.put("type", "SERVER_DISCONNECT");
            disconnectCommand.put("reason", reason != null ? reason : "Disconnected by server");

            messagingTemplate.convertAndSendToUser(
                    playerInfo.principalName,
                    "/queue/private",
                    disconnectCommand
            );

            // Send a notification to other players at the table
            Map<String, Object> disconnectMessage = new HashMap<>();
            disconnectMessage.put("type", "PLAYER_DISCONNECTED");
            disconnectMessage.put("playerId", playerId);
            disconnectMessage.put("playerName", playerInfo.username);
            disconnectMessage.put("reason", reason != null ? reason : "Disconnected by server");

            sendToTable(tableId, disconnectMessage);

            logger.info("Sent disconnect command to player {} at table {}: {}",
                    playerId, tableId, reason);
            return true;
        } catch (Exception e) {
            logger.error("Error sending disconnect command to player: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Overloaded method to disconnect a player without specifying a reason.
     *
     * @param tableId The table ID
     * @param playerId The player ID to disconnect
     * @return True if disconnect command was sent, false if player not found
     *
    public boolean disconnectPlayer(Long tableId, Long playerId) {
        return disconnectPlayer(tableId, playerId, null);
    }

    /**
     * Check if a player is currently connected to a table.
     *
     * @param tableId The table ID
     * @param playerId The player ID
     * @return True if player is connected, false otherwise
     *
    public boolean isPlayerConnected(Long tableId, Long playerId) {
        Map<Long, PlayerInfo> tablePlayers = tablePlayerMap.get(tableId);
        if (tablePlayers == null) return false;

        PlayerInfo playerInfo = tablePlayers.get(playerId);
        if (playerInfo == null) return false;

        // Check if the user is still registered in the Spring user registry
        SimpUser user = simpUserRegistry.getUser(playerInfo.principalName);
        return user != null && !user.getSessions().isEmpty();
    }

    /**
     * Gets a map of all connected player IDs and usernames for a specific table.
     *
     * @param tableId The table ID
     * @return Map of player IDs to usernames
     *
    public Map<Long, String> getConnectedPlayers(Long tableId) {
        Map<Long, String> result = new HashMap<>();
        Map<Long, PlayerInfo> tablePlayers = tablePlayerMap.get(tableId);

        if (tablePlayers != null) {
            tablePlayers.forEach((playerId, info) ->
                    result.put(playerId, info.username));
        }

        return result;
    }

    /**
     * Gets the total number of connected players for a specific table.
     *
     * @param tableId The table ID
     * @return The number of connected players
     *
    public int getConnectedPlayerCount(Long tableId) {
        Map<Long, PlayerInfo> tablePlayers = tablePlayerMap.get(tableId);
        return tablePlayers != null ? tablePlayers.size() : 0;
    }

    /**
     * Information about a player in a table.
     *
     * @param sessionId The WebSocket session ID
     * @param principalName The Spring Security principal name (for user-specific messaging)
     * @param username The display name of the player
     *
    private record PlayerInfo(String sessionId, String principalName, String username) {}

    /**
     * Information about a player-table relationship for a specific session.
     *
     * @param tableId The table ID
     * @param playerId The player ID
     * @param username The display name of the player
     *
    private record TablePlayerInfo(Long tableId, Long playerId, String username) {}
}

 */