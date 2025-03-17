package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Represents a single betting stage within a poker round (PreFlop, Flop, Turn, or River).
 * Each GameRound contains multiple BettingRounds.
 */
@Getter
@Setter
@Entity
@Table(name = "betting_rounds")
public class BettingRound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The stage of this betting round
    @Enumerated(EnumType.STRING)
    private BettingStage stage;

    // The current highest bet amount
    private Double currentBet = 0.0;

    // Reference to the parent game round
    @ManyToOne
    private GameRound gameRound;

    // All moves made during this betting round
    @OneToMany(mappedBy = "bettingRound", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Move> moves = new ArrayList<>();

    // The player who last raised the bet
    @ManyToOne
    private Player lastRaiser;

    public Set<Player> getPlayers() {
        return gameRound != null ? gameRound.getPlayers() : Collections.emptySet();
    }
}