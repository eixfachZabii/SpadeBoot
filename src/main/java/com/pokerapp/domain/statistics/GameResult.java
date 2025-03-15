// src/main/java/com/pokerapp/domain/statistics/GameResult.java
package com.pokerapp.domain.statistics;

import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.user.Player;
import jakarta.persistence.*;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
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
}
