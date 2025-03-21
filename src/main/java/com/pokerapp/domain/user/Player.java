package com.pokerapp.domain.user;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Hand;
import com.pokerapp.domain.game.Move;
import com.pokerapp.domain.poker.HandEvaluator;
import com.pokerapp.domain.poker.HandRank;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the poker game. A player is a user with poker-specific properties.
 */
@Getter
@Setter
@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Current chip count at the table
    private Integer chips = 0;

    // Player's status in the current game
    @Enumerated(EnumType.STRING)
    private PlayerStatus status = PlayerStatus.SITTING_OUT;

    // The table the player is currently sitting at
    @Column(name = "current_table_id")
    private Long currentTableId;

    // Current win probability calculated by the server
    @Transient
    private Double winProbability = 0.0;

    // Tracks the total bet amount for the current round
    @Transient
    private Integer totalBet = 0;

    public Long getUserId() {
        return user.getId();
    }

    public boolean isAllIn() {
        return status == PlayerStatus.ACTIVE && (chips == null || chips <= 0.0);
    }

    public boolean isFolded() {
        return status == PlayerStatus.FOLDED;
    }

    public void rebuy(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Rebuy amount must be positive");
        }

        if (amount > user.getBalance()) {
            throw new IllegalArgumentException("Insufficient user balance for rebuy");
        }

        user.setBalance(user.getBalance() - amount);
        this.chips += amount;
    }

    public void leaveTable(Integer remainingChips) {
        if (remainingChips != null && remainingChips > 0) {
            user.setBalance(user.getBalance() + remainingChips);
        }

        this.chips = 0;
        this.currentTableId = null;
        this.status = PlayerStatus.SITTING_OUT;
        this.totalBet = 0;
    }
}