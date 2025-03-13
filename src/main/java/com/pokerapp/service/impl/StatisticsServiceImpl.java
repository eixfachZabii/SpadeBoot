// src/main/java/com/pokerapp/service/impl/StatisticsServiceImpl.java
package com.pokerapp.service.impl;

import com.pokerapp.api.dto.response.StatisticsDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.statistics.GameResult;
import com.pokerapp.domain.statistics.Statistics;
import com.pokerapp.domain.user.User;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.StatisticsRepository;
import com.pokerapp.service.StatisticsService;
import com.pokerapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import javax.transaction.Transactional;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private final StatisticsRepository statisticsRepository;

    @Autowired
    private final UserService userService;

    @Autowired
    public StatisticsServiceImpl(
            StatisticsRepository statisticsRepository,
            UserService userService) {
        this.statisticsRepository = statisticsRepository;
        this.userService = userService;
    }

    @Override
    public StatisticsDto getUserStatistics(Long userId) {
        Statistics statistics = statisticsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userService.getUserById(userId);
                    Statistics newStats = new Statistics();
                    newStats.setUser(user);
                    return statisticsRepository.save(newStats);
                });

        return convertToDto(statistics);
    }

    @Override
   // @Transactional
    public GameResult recordGameResult(Game game, Map<User, Double> winnings) {
        GameResult gameResult = new GameResult();
        gameResult.setGame(game);

        winnings.forEach((user, amount) -> {
            if (user instanceof com.pokerapp.domain.user.Player) {
                gameResult.getWinnings().put((com.pokerapp.domain.user.Player) user, amount);
            }
        });

        return gameResult;
    }

    @Override
  //  @Transactional
    public void updateUserStatistics(GameResult gameResult) {
        gameResult.getWinnings().forEach((player, amount) -> {
            Statistics statistics = statisticsRepository.findByUserId(player.getId())
                    .orElseGet(() -> {
                        Statistics newStats = new Statistics();
                        newStats.setUser(player);
                        return newStats;
                    });

            statistics.updateStats(gameResult);
            statisticsRepository.save(statistics);
        });
    }

    private StatisticsDto convertToDto(Statistics statistics) {
        StatisticsDto dto = new StatisticsDto();
        dto.setUserId(statistics.getUser().getId());
        dto.setUsername(statistics.getUser().getUsername());
        dto.setGamesPlayed(statistics.getGamesPlayed());
        dto.setGamesWon(statistics.getGamesWon());
        dto.setWinRate(statistics.getWinRate());
        dto.setTotalWinnings(statistics.getTotalWinnings());
        return dto;
    }
}
