// src/main/java/com/pokerapp/api/controller/GameController.java
package com.pokerapp.api.controller;

import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.service.GameService;
import com.pokerapp.service.UserService;
import com.pokerapp.service.impl.GameServiceImpl;
import com.pokerapp.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameServiceImpl gameService;

    @Autowired
    private UserServiceImpl userService;



    @PostMapping("/tables/{tableId}")
    public ResponseEntity<GameStateDto> createGame(@PathVariable Long tableId) {
        Game game = gameService.createGame(tableId);
        GameStateDto gameState = gameService.getGameState(game.getId());
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/{gameId}/start")
    public ResponseEntity<GameStateDto> startGame(@PathVariable Long gameId) {
        gameService.startGame(gameId);
        GameStateDto gameState = gameService.getGameState(gameId);
        return ResponseEntity.ok(gameState);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateDto> getGameState(@PathVariable Long gameId) {
        GameStateDto gameState = gameService.getGameState(gameId);
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/{gameId}/moves")
    public ResponseEntity<GameStateDto> makeMove(
            @PathVariable Long gameId,
            @Valid @RequestBody MoveDto moveDto) {

        Long playerId = userService.getCurrentUser().getId();
        GameStateDto gameState = gameService.makeMove(gameId, playerId, moveDto);
        return ResponseEntity.ok(gameState);
    }
}
