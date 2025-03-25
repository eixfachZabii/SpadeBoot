package com.pokerapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.pokerapp.service.TableService;

@Component
public class TableMembershipInterceptor implements ChannelInterceptor {

    @Autowired
    private TableService tableService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // Extract table ID from destination
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/topic/tables/")) {
                String tableId = extractTableId(destination);
                Authentication authentication = (Authentication) accessor.getUser();

                if (authentication != null && tableId != null) {
                    String username = authentication.getName();
                    // Verify if user is allowed to access this table
                    if (!tableService.getTableById(tableId).) {
                        throw new IllegalArgumentException("User not authorized to access this table");
                    }
                }
            }
        }

        return message;
    }

    private String extractTableId(String destination) {
        // Extract table ID from a path like "/topic/tables/123/actions"
        String[] parts = destination.split("/");
        if (parts.length >= 4) {
            return parts[3];
        }
        return null;
    }
}