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
    private Double chips = 0.0;

    // Player's status in the current game
    @Enumerated(EnumType.STRING)
    private PlayerStatus status = PlayerStatus.SITTING_OUT;

    // Player's cards
    @OneToOne(cascade = CascadeType.ALL)
    private Hand hand;

    // The table the player is currently sitting at
    @Column(name = "current_table_id")
    private Long currentTableId;

    // Tracks whether the player has acted in the current betting round
    @Getter
    @Transient
    private boolean hasActed = false;

    // Current win probability calculated by the server
    @Transient
    private Double winProbability = 0.0;

    // Tracks the total bet amount for the current round
    @Transient
    private Double totalBet = 0.0;

    /**
     * Default constructor
     */
    public Player() {
    }

    /**
     * Gets the player's username (delegates to user)
     */
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Gets the player's email (delegates to user)
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Gets the player's balance (delegates to user)
     */
    public Double getBalance() {
        return user.getBalance();
    }

    /**
     * Sets the player's balance (delegates to user)
     */
    public void setBalance(Double balance) {
        user.setBalance(balance);
    }

    /**
     * Gets the user ID
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * Checks if the player is all-in (has bet all their chips)
     * @return true if the player has no chips left and is still in the game
     */
    public boolean isAllIn() {
        return status == PlayerStatus.ACTIVE && (chips == null || chips <= 0.0);
    }

    /**
     * Checks if the player has folded
     * @return true if the player's status is FOLDED
     */
    public boolean isFolded() {
        return status == PlayerStatus.FOLDED;
    }

    /**
     * Checks if the player is "all out" (not participating in the game)
     * @return true if the player is sitting out or has left the table
     */
    public boolean isAllOut() {
        return status == PlayerStatus.SITTING_OUT || currentTableId == null;
    }

    /**
     * Gets the total amount the player has bet in the current round
     * @return the total bet amount
     */
    public Double getTotalBet() {
        return totalBet != null ? totalBet : 0.0;
    }

    /**
     * Add to the player's total bet for the current round
     * @param amount Amount to add to the total bet
     */
    public void addToTotalBet(Double amount) {
        if (amount != null && amount > 0) {
            if (this.totalBet == null) {
                this.totalBet = amount;
            } else {
                this.totalBet += amount;
            }
        }
    }

    /**
     * Resets the player's total bet for a new round
     */
    public void resetTotalBet() {
        this.totalBet = 0.0;
    }

    /**
     * Rebuys chips from the player's balance
     * @param amount Amount to rebuy
     */
    public void rebuy(Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Rebuy amount must be positive");
        }

        if (amount > getBalance()) {
            throw new IllegalArgumentException("Insufficient balance for rebuy");
        }

        setBalance(getBalance() - amount);
        this.chips += amount;
    }

    /**
     * Leaves the current table, returning chips to balance
     * @param remainingChips Chips to return to balance
     */
    public void leaveTable(Double remainingChips) {
        if (remainingChips != null && remainingChips > 0) {
            setBalance(getBalance() + remainingChips);
        }

        this.chips = 0.0;
        this.currentTableId = null;
        this.status = PlayerStatus.SITTING_OUT;
        this.totalBet = 0.0;

        // Clear cards if any
        if (this.hand != null) {
            this.hand.clear();
        }
    }
}