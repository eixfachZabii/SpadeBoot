// src/main/java/com/pokerapp/domain/user/Spectator.java
package com.pokerapp.domain.user;

import com.pokerapp.domain.game.Game;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "spectators")
public class Spectator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "watching_table_id")
    private Long watchingTableId;

    // Delegate methods
    public String getUsername() {
        return user.getUsername();
    }
    
    public Long getUserId() {
        return user.getId();
    }

    // Spectator-specific methods
    public Map<Player, Double> viewWinOdds() {
        // Logic to calculate win odds
        //TODO
        return new HashMap<>();
    }

    public void watchReplay(Game game) {
        //TODO
        // Logic for watching game replay
    }
}