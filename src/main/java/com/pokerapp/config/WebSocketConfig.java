package com.pokerapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private TableMembershipInterceptor tableMembershipInterceptor;


    /*
        * CLIENT                              SERVER
        |                                   |
        |-- SEND /app/tables/123/action --->| → @MessageMapping processes
        |                                   |
        |<--- SUBSCRIBE /topic/tables/123 --|
        |                                   |
        |<--- MESSAGE /topic/tables/123 ----| ← convertAndSend() broadcasts
        |                                   |
        |<-- SUBSCRIBE /user/queue/private -|
        |                                   |
        |<-- MESSAGE /user/queue/private ---| ← convertAndSendToUser() to specific user
        *
        *  |
        *  |
        *  \/
   */

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple broker for topic and queue destinations
        registry.enableSimpleBroker("/topic", "/queue");
        // Set prefix for application destinations
        registry.setApplicationDestinationPrefixes("/app");
        // Set prefix for user-specific destinations
        registry.setUserDestinationPrefix("/user");
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Configure message size limits and timeouts
        registration.setMessageSizeLimit(65536) // 64KB
                .setSendBufferSizeLimit(512 * 1024) // 512KB
                .setSendTimeLimit(20000); // 20 seconds
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Register the table membership interceptor for inbound messages
        registration.interceptors(tableMembershipInterceptor);
    }
}