package com.pokerapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    public AuthorizationManager<org.springframework.messaging.Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        return messages
                // Explicitly permit connect, disconnect, heartbeat, and unsubscribe messages
                .simpTypeMatchers(SimpMessageType.CONNECT,
                        SimpMessageType.DISCONNECT,
                        SimpMessageType.HEARTBEAT,
                        SimpMessageType.UNSUBSCRIBE).permitAll()

                // User must be authenticated to send messages to application destinations
                .simpDestMatchers("/app/**").authenticated()

                // User must be authenticated to subscribe to destinations
                // Our custom interceptor will handle the table-specific authorization
                .simpSubscribeDestMatchers("/topic/tables/**").authenticated()
                .simpSubscribeDestMatchers("/user/**").authenticated()

                // Default fallback - authenticated for anything not explicitly configured
                .anyMessage().authenticated()
                .build();
    }
}