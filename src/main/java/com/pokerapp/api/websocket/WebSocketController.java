package com.pokerapp.api.websocket;

import com.pokerapp.api.dto.request.ChatMessageDto;
import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.api.dto.response.MessageDto;
import com.pokerapp.domain.user.Player;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Optional;

@Controller
public class WebSocketController {

    private final GameService gameService;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketController(
            GameService gameService,
            PlayerRepository playerRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Processes a move in the poker game via WebSocket
     */
    @MessageMapping("/games/{gameId}/move")
    @SendTo("/topic/games/{gameId}")
    public GameStateDto processMove(
            @DestinationVariable Long gameId,
            MoveDto moveDto,
            SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Extract user ID from principal name
        Long userId = Long.parseLong(principal.getName());

        // Make the move and return updated game state
        //return gameService.makeMove(gameId, userId, moveDto);
        return null;
    }

    /**
     * Processes a chat message in the poker game via WebSocket
     */
    @MessageMapping("/games/{gameId}/chat")
    @SendTo("/topic/games/{gameId}/chat")
    public MessageDto processChat(
            @DestinationVariable Long gameId,
            ChatMessageDto chatMessageDto,
            SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Extract user ID from principal name
        Long userId = Long.parseLong(principal.getName());

        // Get player info if available
        Optional<Player> playerOpt = playerRepository.findByUserId(userId);
        String playerName = playerOpt.map(Player::getUsername).orElse("Unknown");

        // Create and return a message DTO
        MessageDto messageDto = new MessageDto();
        messageDto.setType("CHAT");
        messageDto.setContent(chatMessageDto.getMessage());
        messageDto.setPlayerId(userId);
        messageDto.setTimestamp(String.valueOf(System.currentTimeMillis()));

        return messageDto;
    }

    /**
     * Sends a system notification to all players in a game
     */
    public void sendSystemNotification(Long gameId, String message) {
        MessageDto notification = new MessageDto();
        notification.setType("SYSTEM");
        notification.setContent(message);
        notification.setTimestamp(String.valueOf(System.currentTimeMillis()));

        messagingTemplate.convertAndSend("/topic/games/" + gameId + "/notifications", notification);
    }

    /**
     * Broadcasts the current game state to all players
     */
    public void broadcastGameState(Long gameId) {
        try {
            GameStateDto gameState = gameService.getGameState(gameId);
            messagingTemplate.convertAndSend("/topic/games/" + gameId, gameState);
        } catch (Exception e) {
            System.err.println("Error broadcasting game state: " + e.getMessage());
        }
    }
}