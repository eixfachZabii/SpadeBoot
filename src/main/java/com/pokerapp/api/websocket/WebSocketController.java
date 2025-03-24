package com.pokerapp.api.websocket;

import com.pokerapp.api.dto.request.ChatMessageDto;
import com.pokerapp.api.dto.request.PlayerActionDto;
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
     * Processes a player action in the poker game via WebSocket
     */
    @MessageMapping("/games/{gameId}/action")
    @SendTo("/topic/games/{gameId}")
    public GameStateDto processPlayerAction(
            @DestinationVariable Long gameId,
            PlayerActionDto actionDto,
            SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Extract user ID from principal name
        Long userId = Long.parseLong(principal.getName());

        // Find player by user ID
        Player player = playerRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Player not found for user ID: " + userId));

        // Process player action in game service
        return gameService.handlePlayerAction(gameId, player.getId(), actionDto.getAction(), actionDto.getAmount());
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
        String playerName = playerOpt.map(p -> p.getUser().getUsername()).orElse("Unknown");

        // Create and return a message DTO
        MessageDto messageDto = new MessageDto();
        messageDto.setType("CHAT");
        messageDto.setContent(chatMessageDto.getMessage());
        messageDto.setPlayerId(userId);
        messageDto.setPlayerName(playerName);
        messageDto.setTimestamp(String.valueOf(System.currentTimeMillis()));

        return messageDto;
    }

    /**
     * Request to join a game as a spectator (receive updates without playing)
     */
    @MessageMapping("/games/{gameId}/join-spectator")
    public void joinAsSpectator(
            @DestinationVariable Long gameId,
            SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Send current game state to the new spectator
        try {
            GameStateDto gameState = gameService.getGameState(gameId);
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/games/" + gameId, 
                    gameState);
        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    new GameEvent(GameEvent.Type.SYSTEM_MESSAGE, "Error joining game: " + e.getMessage(), null));
        }
    }
}