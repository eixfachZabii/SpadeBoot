package com.pokerapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

    /**
     * Configures the WebSocket connection endpoint and message broker.
     * This is the primary configuration class for WebSocket functionality.
     */
    @Configuration
    @EnableWebSocketMessageBroker
    public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

        //@Autowired
        //private TableMembershipInterceptor tableMembershipInterceptor;

        /**
         * Configures the message broker that will handle routing messages
         * between clients and destinations.
         *
         * @param registry The message broker registry
         */
        @Override
        public void configureMessageBroker(MessageBrokerRegistry registry) {
            // Enable a simple in-memory broker for topics
            // /topic/tables/{tableId} will be used for table messages
            registry.enableSimpleBroker("/topic");

            // Set the application destination prefix for controller endpoints
            // Client will send messages to /app/... to reach @MessageMapping methods
            registry.setApplicationDestinationPrefixes("/app");
        }
        
        /**
         * Registers the STOMP endpoint that clients will use to connect.
         *
         * @param registry The STOMP endpoint registry
         */
        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            // Register the /ws endpoint, enable SockJS fallback options
            registry.addEndpoint("/ws")
                    .setAllowedOrigins("*") // Consider restricting this in production
                    .withSockJS();
        }

        /**
         * Configures the client inbound channel to use our custom interceptor.
         * This channel handles messages from clients to the server.
         *
         * @param registration The channel registration
         */
        /*@Override
        public void configureClientInboundChannel(ChannelRegistration registration) {
            // Add the table membership interceptor to verify
            // users can only subscribe to tables they've joined
            registration.interceptors(tableMembershipInterceptor);
        }
         */
    }