// src/main/java/com/pokerapp/domain/game/BettingRound.java
package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
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
