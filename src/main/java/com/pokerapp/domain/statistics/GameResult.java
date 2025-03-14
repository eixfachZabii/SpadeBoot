// src/main/java/com/pokerapp/domain/statistics/GameResult.java
package com.pokerapp.domain.statistics;

import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.user.Player;
import jakarta.persistence.*;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

//@Data
@Entity
@Table(name = "game_results")
public class GameResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Game game;

    @ElementCollection
    @CollectionTable(name = "player_winnings",
            joinColumns = @JoinColumn(name = "result_id"))
    @MapKeyJoinColumn(name = "player_id")
    @Column(name = "amount")
    private Map<Player, Double> winnings = new HashMap<>();

    private Long timestamp = System.currentTimeMillis();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Map<Player, Double> getWinnings() {
        return winnings;
    }

    public void setWinnings(Map<Player, Double> winnings) {
        this.winnings = winnings;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
