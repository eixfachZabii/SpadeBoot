// src/main/java/com/pokerapp/domain/statistics/Statistics.java
package com.pokerapp.domain.statistics;

import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "statistics",
        uniqueConstraints = {

        })
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "player_id")
    private Player player;

    private Integer gamesPlayed = 0;
    private Integer gamesWon = 0;
    private Double winRate = 0.0;
    private Double totalWinnings = 0.0;


    public void updateStats(GameResult gameResult) {
        Double winnings = gameResult.getWinnings().getOrDefault(player.getUser(), 0.0);
        gamesPlayed++;

        if (winnings > 0) {
            gamesWon++;
        }

        totalWinnings += winnings;
        winRate = gamesPlayed > 0 ? (double) gamesWon / gamesPlayed : 0.0;
    }
}