package com.pokerapp.api.controller;


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
//    @PostMapping("/tables/{tableId}")
//    public ResponseEntity<GameStateDto> createGame(@PathVariable Long tableId) {
//        Game game = gameService.createGame(tableId);
//        GameStateDto gameState = gameService.getGameState(game.getId());
//        return ResponseEntity.ok(gameState);
//    }

    /**
     * Starts an existing poker game
     */
//    @PostMapping("/{gameId}/start")
//    public ResponseEntity<GameStateDto> startGame(@PathVariable Long gameId) {
//        gameService.startGame(gameId);
//        GameStateDto gameState = gameService.getGameState(gameId);
//        return ResponseEntity.ok(gameState);
//    }

//    @PostMapping("/{gameId}/next-round")
//    public ResponseEntity<GameStateDto> startNextRound(@PathVariable Long gameId) {
//        ((GameServiceImpl) gameService).startNextRound(gameId);
//        GameStateDto gameState = gameService.getGameState(gameId);
//        return ResponseEntity.ok(gameState);
//    }

    /**
     * Gets the current state of a poker game
     */
//    @GetMapping("/{gameId}")
//    public ResponseEntity<GameStateDto> getGameState(@PathVariable Long gameId) {
//        GameStateDto gameState = gameService.getGameState(gameId);
//        return ResponseEntity.ok(gameState);
//    }

    /**
     * Ends the current poker game
     */
//    @PostMapping("/{gameId}/end")
//    public ResponseEntity<GameStateDto> endGame(@PathVariable Long gameId) {
//        gameService.endGame(gameId);
//        GameStateDto gameState = gameService.getGameState(gameId);
//        return ResponseEntity.ok(gameState);
//    }
}