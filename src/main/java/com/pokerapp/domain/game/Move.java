package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a player's move in a poker game (check, call, raise, fold, etc.)
 */
@Getter
@Setter
@Entity
@Table(name = "moves")
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The type of move (CHECK, CALL, RAISE, FOLD, ALL_IN, SMALL_BLIND, BIG_BLIND)
    @Enumerated(EnumType.STRING)
    private MoveType type;

    // The amount of chips involved in the move (for CALL, RAISE, etc.)
    private Double amount;

    // The player who made the move
    @ManyToOne
    private Player player;

    // The betting round in which the move was made
    @ManyToOne
    private BettingRound bettingRound;

    // Timestamp when the move was made
    private Long timestamp = System.currentTimeMillis();

    /**
     * Creates a descriptive string representation of the move
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(player != null ? player.getUsername() : "Unknown");
        sb.append(" ");
        sb.append(type);

        if (amount != null && amount > 0) {
            sb.append(" $").append(String.format("%.2f", amount));
        }

        return sb.toString();
    }

    /**
     * Creates a formatted string for display in the game log
     */
    public String toLogString() {
        String playerName = player != null ? player.getUsername() : "Unknown";
        String stageInfo = "";

        if (bettingRound != null && bettingRound.getStage() != null) {
            stageInfo = "[" + bettingRound.getStage() + "] ";
        }

        switch (type) {
            case CHECK:
                return stageInfo + playerName + " checks";
            case CALL:
                return stageInfo + playerName + " calls $" + String.format("%.2f", amount);
            case RAISE:
                return stageInfo + playerName + " raises to $" + String.format("%.2f", amount);
            case FOLD:
                return stageInfo + playerName + " folds";
            case ALL_IN:
                return stageInfo + playerName + " goes all-in for $" + String.format("%.2f", amount);
            case SMALL_BLIND:
                return stageInfo + playerName + " posts small blind $" + String.format("%.2f", amount);
            case BIG_BLIND:
                return stageInfo + playerName + " posts big blind $" + String.format("%.2f", amount);
            default:
                return stageInfo + playerName + " " + type;
        }
    }
}