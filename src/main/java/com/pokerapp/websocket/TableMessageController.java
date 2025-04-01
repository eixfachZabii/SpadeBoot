package com.pokerapp.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;


import java.security.Principal;
import java.util.Map;

/**
 * Controller to handle WebSocket messages for poker tables
 */
@Controller
public class TableMessageController {
    private static final Logger logger = LoggerFactory.getLogger(TableMessageController.class);
    
    private final TableWebSocketHandler webSocketHandler;
    
    public TableMessageController(TableWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }
    
    /**
     * Handle messages sent to a table
     * Routes responses to pending requests
     * 
     * @param tableId Table ID from the destination path
     * @param message Message payload
     * @param headerAccessor Message headers
     * @param principal Message sender principal
     */
    @MessageMapping("/tables/{tableId}")
    public void handleTableMessage(
            @DestinationVariable Long tableId,
            @Payload Map<String, Object> message,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
            
        // Extract player ID from principal
        Long playerId = Long.parseLong(principal.getName());
        
        // If this is a response to a pending request, handle it
        if (message.containsKey("responseId")) {
            String responseId = (String) message.get("responseId");
            boolean handled = webSocketHandler.handlePlayerResponse(responseId, message);
            
            if (handled) {
                logger.debug("Handled response from player {} for request {}", playerId, responseId);
                return; // Don't broadcast response messages
            }
        }
        
        // Add player info to the message
        message.put("playerId", playerId);
        
        // For regular messages, forward to all players at the table
        webSocketHandler.sendToTable(tableId, message);
    }
}
