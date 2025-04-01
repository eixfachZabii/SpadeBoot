package com.pokerapp.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handler for WebSocket communications for poker tables.
 */
@Component
public class TableWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(TableWebSocketHandler.class);

    private final SimpMessagingTemplate messagingTemplate;

    // Map to track player sessions by table
    private final Map<Long, Map<Long, String>> tablePlayerMap = new ConcurrentHashMap<>();

    // Map to store pending requests waiting for responses
    private final Map<String, CompletableFuture<Map<String, Object>>> pendingResponses = new ConcurrentHashMap<>();

    public TableWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Register a player connection to a table
     *
     * @param tableId The table ID
     * @param playerId The player ID
     * @param username The player's username
     */
    public void registerPlayerConnection(Long tableId, Long playerId, String username) {
        tablePlayerMap.computeIfAbsent(tableId, k -> new ConcurrentHashMap<>())
                .put(playerId, username);

        logger.info("Player {} ({}) connected to table {}", playerId, username, tableId);
    }

    /**
     * Remove a player connection from a table
     *
     * @param tableId The table ID
     * @param playerId The player ID
     */
    public void removePlayerConnection(Long tableId, Long playerId) {
        Map<Long, String> tablePlayers = tablePlayerMap.get(tableId);
        if (tablePlayers != null) {
            String username = tablePlayers.remove(playerId);
            logger.info("Player {} ({}) disconnected from table {}", playerId, username, tableId);

            // If table has no more players, remove it from the map
            if (tablePlayers.isEmpty()) {
                tablePlayerMap.remove(tableId);
            }
        }
    }

    /**
     * Send a message to all players at a table
     *
     * @param tableId The table ID
     * @param message The message to send
     */
    public void sendToTable(Long tableId, Object message) {
        messagingTemplate.convertAndSend(
                "/topic/tables/" + tableId,
                message
        );
        logger.debug("Sent message to all players at table {}: {}", tableId, message);
    }

    /**
     * Send a message to a specific player
     *
     * @param playerId The player ID
     * @param message The message to send
     * @return true if message was sent, false if player not found
     */
    public boolean sendToPlayer(Long playerId, Object message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    playerId.toString(),
                    "/queue/private",
                    message
            );
            logger.debug("Sent message to player {}: {}", playerId, message);
            return true;
        } catch (Exception e) {
            logger.error("Error sending message to player {}: {}", playerId, e.getMessage());
            return false;
        }
    }

    /**
     * Send a request to a player and wait for their response with a timeout
     *
     * @param playerId The player ID
     * @param message The message to send
     * @param timeoutSeconds Maximum time to wait for response
     * @return The player's response
     * @throws TimeoutException If player doesn't respond within timeout
     */
    public Map<String, Object> waitForPlayerResponse(Long playerId, Map<String, Object> message,
                                                     int timeoutSeconds) throws TimeoutException {
        // Generate a unique request ID
        String requestId = UUID.randomUUID().toString();

        // Create a CompletableFuture to wait for the response
        CompletableFuture<Map<String, Object>> responseFuture = new CompletableFuture<>();
        pendingResponses.put(requestId, responseFuture);

        // Add request ID to message so client knows to include it in response
        message.put("requestId", requestId);

        // Send message to player
        boolean sent = sendToPlayer(playerId, message);
        if (!sent) {
            pendingResponses.remove(requestId);
            throw new RuntimeException("Player not connected or not found");
        }

        try {
            // Wait for response with timeout
            return responseFuture.orTimeout(timeoutSeconds, TimeUnit.SECONDS).get();
        } catch (Exception e) {
            pendingResponses.remove(requestId);
            if (e.getCause() instanceof TimeoutException) {
                throw new TimeoutException("Timeout waiting for player response");
            }
            throw new RuntimeException("Error waiting for player response", e);
        }
    }

    /**
     * Handle a response from a player to a pending request
     *
     * @param responseId The request ID being responded to
     * @param response The response data
     * @return true if response was handled, false if no pending request found
     */
    public boolean handlePlayerResponse(String responseId, Map<String, Object> response) {
        CompletableFuture<Map<String, Object>> future = pendingResponses.remove(responseId);
        if (future != null) {
            future.complete(response);
            return true;
        }
        return false;
    }

    /**
     * Check if a player is connected to a table
     *
     * @param tableId The table ID
     * @param playerId The player ID
     * @return true if player is connected, false otherwise
     */
    public boolean isPlayerConnected(Long tableId, Long playerId) {
        Map<Long, String> tablePlayers = tablePlayerMap.get(tableId);
        return tablePlayers != null && tablePlayers.containsKey(playerId);
    }

    /**
     * Get all connected players for a table
     *
     * @param tableId The table ID
     * @return Map of player IDs to usernames
     */
    public Map<Long, String> getConnectedPlayers(Long tableId) {
        return tablePlayerMap.getOrDefault(tableId, new ConcurrentHashMap<>());
    }
}