package com.pokerapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
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
                // Require authentication for all connections
                .simpTypeMatchers(SimpMessageType.CONNECT).authenticated()
                // Require authentication for app destinations
                .simpDestMatchers("/app/**").authenticated()
                // Require authentication for subscriptions to topic/games
                .simpSubscribeDestMatchers("/topic/games/**").authenticated()
                // Default rule for all other messages
                .anyMessage().authenticated()
                .build();
    }
}