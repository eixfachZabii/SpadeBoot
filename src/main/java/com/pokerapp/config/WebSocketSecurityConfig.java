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
                // Require authentication for all connections
                .simpTypeMatchers(SimpMessageType.CONNECT).authenticated()
                // Validate table membership via custom channel interceptor
                .simpDestMatchers("/app/tables/*/connect").authenticated()
                .simpSubscribeDestMatchers("/topic/tables/*/**").authenticated()
                .anyMessage().authenticated()
                .build();
    }
}