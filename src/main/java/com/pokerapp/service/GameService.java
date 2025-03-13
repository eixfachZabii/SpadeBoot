// src/main/java/com/pokerapp/service/GameService.java
package com.pokerapp.service;

import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.domain.game.Game;

public interface GameService {
    Game createGame(Long tableId);
    Game startGame(Long gameId);
    GameStateDto getGameState(Long gameId);
    GameStateDto makeMove(Long gameId, Long playerId, MoveDto moveDto);
    Game endGame(Long gameId);
}
