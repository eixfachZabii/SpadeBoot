package com.pokerapp.api.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Listens for WebSocket lifecycle events and handles them.
 * Primarily responsible for managing WebSocket connections and disconnections.
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private WebSocketHandler webSocketHandler;

    /**
     * Handles WebSocket connection events.
     * Logs when a new WebSocket connection is established.
     *
     * @param event The session connected event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        logger.info("New WebSocket connection established: {}", sessionId);
    }

    /**
     * Handles WebSocket disconnection events.
     * Notifies the WebSocketHandler when a client disconnects.
     *
     * @param event The session disconnect event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        logger.info("WebSocket connection closed: {}", sessionId);

        // Notify the handler about the disconnection to clean up and notify other players
        webSocketHandler.handlePlayerDisconnection(sessionId);
    }
}