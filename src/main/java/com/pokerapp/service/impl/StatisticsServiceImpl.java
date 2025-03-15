package com.pokerapp.service.impl;

import com.pokerapp.api.dto.response.StatisticsDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.statistics.GameResult;
import com.pokerapp.domain.statistics.Statistics;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.repository.StatisticsRepository;
import com.pokerapp.service.StatisticsService;
import com.pokerapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final PlayerRepository playerRepository;
    private final UserService userService;

    @Autowired
    public StatisticsServiceImpl(
            StatisticsRepository statisticsRepository,
            PlayerRepository playerRepository,
            UserService userService) {
        this.statisticsRepository = statisticsRepository;
        this.playerRepository = playerRepository;
        this.userService = userService;
    }

    @Override
    public StatisticsDto getUserStatistics(Long userId) {
        // First find the player associated with this user
        Player player = playerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("No player found for user with ID: " + userId));

        // Then get or create statistics for this player
        Statistics statistics = statisticsRepository.findByPlayerId(player.getId())
                .orElseGet(() -> {
                    Statistics newStats = new Statistics();
                    newStats.setPlayer(player);
                    return statisticsRepository.save(newStats);
                });

        return convertToDto(statistics);
    }

    @Override
    @Transactional
    public GameResult recordGameResult(Game game, Map<Player, Double> winnings) {
        GameResult gameResult = new GameResult();
        gameResult.setGame(game);
        gameResult.setWinnings(winnings);
        return gameResult;
    }

    @Override
    @Transactional
    public void updateUserStatistics(GameResult gameResult) {
        gameResult.getWinnings().forEach((player, amount) -> {
            Statistics statistics = statisticsRepository.findByPlayerId(player.getUserId())
                    .orElseGet(() -> {
                        Statistics newStats = new Statistics();
                        newStats.setPlayer(player);
                        return newStats;
                    });

            statistics.updateStats(gameResult);
            statisticsRepository.save(statistics);
        });
    }

    private StatisticsDto convertToDto(Statistics statistics) {
        Player player = statistics.getPlayer();
        User user = player.getUser();
        
        StatisticsDto dto = new StatisticsDto();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPlayerId(player.getUserId());
        dto.setGamesPlayed(statistics.getGamesPlayed());
        dto.setGamesWon(statistics.getGamesWon());
        dto.setWinRate(statistics.getWinRate());
        dto.setTotalWinnings(statistics.getTotalWinnings());
        return dto;
    }
}