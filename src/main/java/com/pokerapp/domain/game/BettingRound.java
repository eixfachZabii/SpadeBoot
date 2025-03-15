// src/main/java/com/pokerapp/domain/game/BettingRound.java
package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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

    /**
     * Determines the next player who should act in the betting round
     * @return The next player who should take action
     */
    public Player getNextPlayer() {
        // Logic to determine the next player
        //TODO
        return null; // Placeholder
    }
}
