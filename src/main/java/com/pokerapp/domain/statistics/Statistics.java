// src/main/java/com/pokerapp/domain/statistics/Statistics.java
package com.pokerapp.domain.statistics;

import com.pokerapp.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;


//@Data
@Entity
//@Table(name = "statistics")
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    private Integer gamesPlayed = 0;

    private Integer gamesWon = 0;

    private Double winRate = 0.0;

    private Double totalWinnings = 0.0;

    public void updateStats(GameResult gameResult) {
        Double winnings = gameResult.getWinnings().getOrDefault(user, 0.0);
        gamesPlayed++;

        if (winnings > 0) {
            gamesWon++;
        }

        totalWinnings += winnings;
        winRate = (double) gamesWon / gamesPlayed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(Integer gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public Integer getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(Integer gamesWon) {
        this.gamesWon = gamesWon;
    }

    public Double getWinRate() {
        return winRate;
    }

    public void setWinRate(Double winRate) {
        this.winRate = winRate;
    }

    public Double getTotalWinnings() {
        return totalWinnings;
    }

    public void setTotalWinnings(Double totalWinnings) {
        this.totalWinnings = totalWinnings;
    }
}
