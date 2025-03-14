// src/main/java/com/pokerapp/domain/user/Spectator.java
package com.pokerapp.domain.user;

import com.pokerapp.domain.game.Game;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.Map;
import java.util.HashMap;

//@Data
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
        return new HashMap<>();
    }

    public void watchReplay(Game game) {
        // Logic for watching game replay
    }

    // Getters and setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getWatchingTableId() {
        return watchingTableId;
    }

    public void setWatchingTableId(Long watchingTableId) {
        this.watchingTableId = watchingTableId;
    }
}