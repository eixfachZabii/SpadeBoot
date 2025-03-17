package com.pokerapp.api.controller;

import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.service.GameService;
import com.pokerapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * Creates a new poker game at the specified table
     */
    @PostMapping("/tables/{tableId}")
    public ResponseEntity<GameStateDto> createGame(@PathVariable Long tableId) {
        Game game = gameService.createGame(tableId);
        GameStateDto gameState = gameService.getGameState(game.getId());
        return ResponseEntity.ok(gameState);
    }

    /**
     * Starts an existing poker game
     */
    @PostMapping("/{gameId}/start")
    public ResponseEntity<GameStateDto> startGame(@PathVariable Long gameId) {
        gameService.startGame(gameId);
        GameStateDto gameState = gameService.getGameState(gameId);
        return ResponseEntity.ok(gameState);
    }

    /**
     * Gets the current state of a poker game
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateDto> getGameState(@PathVariable Long gameId) {
        GameStateDto gameState = gameService.getGameState(gameId);
        return ResponseEntity.ok(gameState);
    }

    /**
     * Makes a move in the poker game (check, call, raise, fold, all-in)
     */
    @PostMapping("/{gameId}/moves")
    public ResponseEntity<GameStateDto> makeMove(
            @PathVariable Long gameId,
            @Valid @RequestBody MoveDto moveDto) {

        // Get the current user ID from the security context
        Long userId = userService.getCurrentUser().getId();

        // Pass the user ID to the service, not the player ID
        //GameStateDto gameState = gameService.makeMove(gameId, userId, moveDto);
        //return ResponseEntity.ok(gameState);
        return null;
    }

    /**
     * Ends the current poker game
     */
    @PostMapping("/{gameId}/end")
    public ResponseEntity<GameStateDto> endGame(@PathVariable Long gameId) {
        gameService.endGame(gameId);
        GameStateDto gameState = gameService.getGameState(gameId);
        return ResponseEntity.ok(gameState);
    }

    /**
     * Skips a player's turn (admin only)
     */
    @PostMapping("/{gameId}/skip/{playerId}")
    public ResponseEntity<GameStateDto> skipPlayer(
            @PathVariable Long gameId,
            @PathVariable Long playerId) {

        // Verify user has admin role
        if (!userService.getCurrentUser().getRoles().contains("ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        // Create a fold move for the player
        MoveDto moveDto = new MoveDto();
        moveDto.setType("FOLD");

        // Find the player's userId
        playerRepository.findById(playerId).ifPresent(player -> {
            //gameService.makeMove(gameId, player.getUserId(), moveDto);
        });

        return ResponseEntity.ok(gameService.getGameState(gameId));
    }

    /**
     * Gets possible actions for the current player
     */
    @GetMapping("/{gameId}/possible-actions")
    public ResponseEntity<Map<String, Object>> getPossibleActions(@PathVariable Long gameId) {
        GameStateDto gameState = gameService.getGameState(gameId);

        Map<String, Object> response = new HashMap<>();
        response.put("gameId", gameId);
        response.put("currentPlayerId", gameState.getCurrentPlayerId());
        response.put("currentPlayerName", gameState.getCurrentPlayerName());
        response.put("possibleActions", gameState.getPossibleActions());
        response.put("currentBet", gameState.getCurrentBet());

        return ResponseEntity.ok(response);
    }
}