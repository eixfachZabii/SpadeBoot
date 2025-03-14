// src/main/java/com/pokerapp/domain/game/Move.java
package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

//@Data
@Entity
@Table(name = "moves")
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MoveType type;

    private Double amount;

    @ManyToOne
    private Player player;

    @ManyToOne
    private BettingRound bettingRound;

    private Long timestamp = System.currentTimeMillis();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MoveType getType() {
        return type;
    }

    public void setType(MoveType type) {
        this.type = type;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public BettingRound getBettingRound() {
        return bettingRound;
    }

    public void setBettingRound(BettingRound bettingRound) {
        this.bettingRound = bettingRound;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
