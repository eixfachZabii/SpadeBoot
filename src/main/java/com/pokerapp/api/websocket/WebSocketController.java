// src/main/java/com/pokerapp/api/websocket/WebSocketController.java
package com.pokerapp.api.websocket;


import com.pokerapp.api.dto.request.ChatMessageDto;
import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.api.dto.response.MessageDto;
import com.pokerapp.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {

    @Autowired
    private final GameService gameService;

    @Autowired
    public WebSocketController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/games/{gameId}/move")
    @SendTo("/topic/games/{gameId}")
    public GameStateDto processMove(
            @DestinationVariable Long gameId,
            MoveDto moveDto,
            SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        Long playerId = Long.parseLong(principal.getName());

        return gameService.makeMove(gameId, playerId, moveDto);
    }

    @MessageMapping("/games/{gameId}/chat")
    @SendTo("/topic/games/{gameId}/chat")
    public MessageDto processChat(
            @DestinationVariable Long gameId,
            ChatMessageDto chatMessageDto,
            SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        Long playerId = Long.parseLong(principal.getName());

        // Create and return a message DTO
        MessageDto messageDto = new MessageDto();
        messageDto.setType("CHAT");
        messageDto.setContent(chatMessageDto.getMessage());
        messageDto.setPlayerId(playerId);
        messageDto.setTimestamp(String.valueOf(System.currentTimeMillis()));

        return messageDto;
    }
}
