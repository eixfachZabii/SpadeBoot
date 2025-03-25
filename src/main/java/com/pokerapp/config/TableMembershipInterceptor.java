package com.pokerapp.config;

import com.pokerapp.domain.user.Player;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;

@Component
public class TableMembershipInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TableMembershipInterceptor.class);

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // Extract table ID from destination
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/topic/tables/")) {
                Long tableId = extractTableId(destination);
                Principal principal = accessor.getUser();

                if (principal != null && tableId != null) {
                    try {
                        // Spring Security uses Authentication as the Principal implementation
                        Authentication authentication = (Authentication) principal;
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Long userId = userDetails.getId();

                        // Find player associated with this user
                        Optional<Player> playerOpt = playerRepository.findByUserId(userId);

                        // Verify player exists and is at the specified table
                        if (playerOpt.isEmpty() ||
                                playerOpt.get().getCurrentTableId() == null ||
                                !playerOpt.get().getCurrentTableId().equals(tableId)) {

                            logger.warn("Unauthorized table access attempt: User {} tried to subscribe to table {}",
                                    userDetails.getUsername(), tableId);
                            throw new IllegalArgumentException("User not authorized to access this table");
                        }

                        logger.debug("User {} authorized to access table {}", userDetails.getUsername(), tableId);
                    } catch (ClassCastException e) {
                        logger.error("Authentication principal is not of expected type: {}", e.getMessage());
                        throw new IllegalArgumentException("Authentication error");
                    }
                }
            }
        }

        return message;
    }

    private Long extractTableId(String destination) {
        // Extract table ID from a path like "/topic/tables/123/actions"
        String[] parts = destination.split("/");
        if (parts.length >= 4) {
            try {
                return Long.parseLong(parts[3]);
            } catch (NumberFormatException e) {
                logger.error("Failed to parse table ID from destination: {}", destination);
                return null;
            }
        }
        return null;
    }
}