// src/main/java/com/pokerapp/service/GameService.java
package com.pokerapp.service;

import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.GameRound;
import jakarta.transaction.Transactional;

public interface GameService {
    Game createGame(Long tableId);
    void startGame(Long gameId);
    GameStateDto getGameState(Long gameId);
    Game endGame(Long gameId);
}
