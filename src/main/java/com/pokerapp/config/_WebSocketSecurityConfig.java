package com.pokerapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

/**
 * Configures security for WebSocket connections.
 * This class defines authorization rules for different types of STOMP messages.
 * /
@Configuration
@EnableWebSocketSecurity
public class _WebSocketSecurityConfig {

    /**
     * Creates an authorization manager for WebSocket messages.
     * This defines which message types and destinations require authentication.
     *
     * @param messages Builder for message authorization rules
     * @return An authorization manager for WebSocket messages
     * /
    @Bean
    public AuthorizationManager<org.springframework.messaging.Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        return messages
                // Allow connection and disconnection messages without authentication
                // These messages are needed for the initial setup and clean disconnection
                .simpTypeMatchers(
                        SimpMessageType.CONNECT,
                        SimpMessageType.DISCONNECT,
                        SimpMessageType.HEARTBEAT,
                        SimpMessageType.UNSUBSCRIBE
                ).permitAll()

                // Require authentication for subscribing to table topics
                // Our custom interceptor will further check if they're allowed at the specific table
                .simpSubscribeDestMatchers("/topic/tables/**").authenticated()

                // Require authentication for sending messages to application destinations
                .simpDestMatchers("/app/**").authenticated()

                // Default policy: require authentication for any other messages
                .anyMessage().authenticated()
                .build();
    }
}
*/
