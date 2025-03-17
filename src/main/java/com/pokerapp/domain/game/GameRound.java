package com.pokerapp.domain.game;

import com.pokerapp.domain.card.Card;
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
 * Represents a complete round of poker from dealing cards to determining winners.
 * A game consists of multiple game rounds, each with multiple betting rounds.
 */
@Getter
@Setter
@Entity
@Table(name = "game_rounds")
public class GameRound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The round number within the game (e.g., 1st hand, 2nd hand, etc.)
    private Integer roundNumber;

    // The total pot amount for this round
    private Double pot = 0.0;

    // The community cards for this round (flop, turn, river)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Card> communityCards = new ArrayList<>();

    // All betting rounds (preflop, flop, turn, river) for this round
    @OneToMany(mappedBy = "gameRound", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BettingRound> bettingRounds = new ArrayList<>();

    // The current betting round being played
    @OneToOne
    private BettingRound currentBettingRound;

    // The game this round belongs to
    @ManyToOne
    private Game game;

    public Set<Player> getPlayers() {
        return game != null ? game.getPlayers() : Collections.emptySet();
    }

    public Integer getDealerIndex(){
        return game != null ? game.getDealerIndex() : -1;
    }


    /**
     * Deals community cards for the next stage (flop, turn, river)
     * @param count Number of cards to deal
     */
    public void dealCommunityCards(int count) {
        if (game == null || game.getDeck() == null) {
            throw new IllegalStateException("Game or deck not initialized");
        }

        for (int i = 0; i < count; i++) {
            Card card = game.getDeck().drawCard();
            card.setShowing(true); // Community cards are visible to all
            communityCards.add(card);
        }
    }

    /**
     * Advances to the next betting stage (PreFlop -> Flop -> Turn -> River)
     */
    public void advanceToNextBettingRound() {
        BettingStage nextStage;

        if (currentBettingRound == null) {
            nextStage = BettingStage.PREFLOP;
        } else {
            switch (currentBettingRound.getStage()) {
                case PREFLOP:
                    nextStage = BettingStage.FLOP;
                    dealCommunityCards(3); // Deal flop (3 cards)
                    break;
                case FLOP:
                    nextStage = BettingStage.TURN;
                    dealCommunityCards(1); // Deal turn (1 card)
                    break;
                case TURN:
                    nextStage = BettingStage.RIVER;
                    dealCommunityCards(1); // Deal river (1 card)
                    break;
                case RIVER:
                    // Round is complete after river, determine winner
                    return;
                default:
                    throw new IllegalStateException("Unknown betting stage");
            }
        }

        // Create new betting round for the next stage
        BettingRound newRound = new BettingRound();
        newRound.setStage(nextStage);
        newRound.setCurrentBet(0.0); // Reset the current bet for the new stage
        newRound.setGameRound(this);

        bettingRounds.add(newRound);
        currentBettingRound = newRound;
    }
}