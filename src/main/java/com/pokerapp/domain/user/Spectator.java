// src/main/java/com/pokerapp/domain/user/Spectator.java
package com.pokerapp.domain.user;

import com.pokerapp.domain.game.Game;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.Map;
import java.util.HashMap;

//@Data
@Entity
//@Table(name = "spectators")
public class Spectator extends User {

    @Column(name = "watching_table_id")
    private Long watchingTableId;

    public Map<Player, Double> viewWinOdds() {
        // Logic to calculate win odds
        return new HashMap<>();
    }

    public Spectator() {
        super();
        this.setUserType(UserType.SPECTATOR);
    }

    public void watchReplay(Game game) {
        // Logic for watching game replay
    }

    public Long getWatchingTableId() {
        return watchingTableId;
    }

    public void setWatchingTableId(Long watchingTableId) {
        this.watchingTableId = watchingTableId;
    }
}