// src/main/java/com/pokerapp/service/StatisticsService.java
package com.pokerapp.service;

import com.pokerapp.api.dto.response.StatisticsDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.statistics.GameResult;
import com.pokerapp.domain.user.Player;

import java.util.Map;

public interface StatisticsService {
    StatisticsDto getUserStatistics(Long userId);
    GameResult recordGameResult(Game game, Map<Player, Double> winnings);
    void updateUserStatistics(GameResult gameResult);
}
