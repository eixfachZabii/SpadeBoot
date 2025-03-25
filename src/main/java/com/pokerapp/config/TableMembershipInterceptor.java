package com.pokerapp.config;

import com.pokerapp.api.websocket.WebSocketHandler;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.repository.UserRepository;
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

/**
 * Intercepts WebSocket messages to enforce table access control and manage player connections.
 * This interceptor combines access checking and connection management in a single place.
 */
@Component
public class TableMembershipInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TableMembershipInterceptor.class);

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketHandler webSocketHandler;

    /**
     * Processes each message before it's sent through the channel.
     * For SUBSCRIBE messages to table topics:
     * 1. Verifies the user is a member of that table
     * 2. Registers the player's session
     * 3. Notifies other players about the new connection
     *
     * @param message The message being sent
     * @param channel The channel the message is being sent through
     * @return The message (possibly modified) or null to block the message
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Only intercept SUBSCRIBE commands for table topics
        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            // Check if it's a table topic
            if (destination != null && destination.startsWith("/topic/tables/")) {
                try {
                    // Extract table ID from destination
                    Long tableId = extractTableId(destination);
                    Principal principal = accessor.getUser();
                    String sessionId = accessor.getSessionId();

                    if (principal != null && tableId != null && sessionId != null) {
                        // Cast Principal to Authentication and get user details
                        Authentication authentication = (Authentication) principal;
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        Long userId = userDetails.getId();

                        // Verify player exists and is at this table
                        Optional<Player> playerOpt = playerRepository.findByUserId(userId);

                        if (playerOpt.isEmpty() ||
                                playerOpt.get().getCurrentTableId() == null ||
                                !playerOpt.get().getCurrentTableId().equals(tableId)) {

                            logger.warn("Table access denied: User {} tried to subscribe to table {}",
                                    userDetails.getUsername(), tableId);

                            // Block the subscription by returning null
                            return null;
                        }

                        // Access granted - handle player connection
                        Player player = playerOpt.get();
                        User user = userRepository.findById(userId).orElse(null);

                        if (user != null) {
                            // Register player connection in WebSocketHandler
                            webSocketHandler.registerPlayerConnection(
                                    tableId,
                                    player.getId(),
                                    sessionId,
                                    principal.getName(),
                                    user.getUsername()
                            );

                            logger.info("Table access granted: User {} subscribed to table {}",
                                    userDetails.getUsername(), tableId);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error verifying table membership: {}", e.getMessage());
                    return null; // Block on any error
                }
            }
        }

        // Allow the message to proceed
        return message;
    }

    /**
     * Extracts the table ID from a destination path.
     *
     * @param destination The destination path like "/topic/tables/123/actions"
     * @return The table ID as a Long, or null if it can't be extracted
     */
    private Long extractTableId(String destination) {
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