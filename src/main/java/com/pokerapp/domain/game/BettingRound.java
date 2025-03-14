// src/main/java/com/pokerapp/domain/game/BettingRound.java
package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

//@Data
@Entity
@Table(name = "betting_rounds")
public class BettingRound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private BettingStage stage;

    private Double currentBet;

    @ManyToOne
    private GameRound gameRound;

    @OneToMany(mappedBy = "bettingRound", cascade = CascadeType.ALL)
    private List<Move> moves = new ArrayList<>();

    @Transient
    private int currentPlayerIndex;

    public void processMove(Player player, Move move) {
        // Logic to process a player's move
        move.setPlayer(player);
        move.setBettingRound(this);
        moves.add(move);
    }

    public Player getNextPlayer() {
        // Logic to determine the next player
        return null; // Placeholder
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BettingStage getStage() {
        return stage;
    }

    public void setStage(BettingStage stage) {
        this.stage = stage;
    }

    public Double getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(Double currentBet) {
        this.currentBet = currentBet;
    }

    public GameRound getGameRound() {
        return gameRound;
    }

    public void setGameRound(GameRound gameRound) {
        this.gameRound = gameRound;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }
}
