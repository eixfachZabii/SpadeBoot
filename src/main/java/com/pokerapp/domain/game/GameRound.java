// src/main/java/com/pokerapp/domain/game/GameRound.java
package com.pokerapp.domain.game;

import com.pokerapp.domain.card.Card;
import lombok.Data;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "game_rounds")
public class GameRound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer roundNumber;

    private Double pot = 0.0;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Card> communityCards = new ArrayList<>();

    @OneToMany(mappedBy = "gameRound", cascade = CascadeType.ALL)
    private List<BettingRound> bettingRounds = new ArrayList<>();

    @ManyToOne
    private Game game;

    @OneToOne
    private BettingRound currentBettingRound;

    public void dealCommunityCards(int count) {
        for (int i = 0; i < count; i++) {
            Card card = game.getDeck().drawCard();
            card.setShowing(true);
            communityCards.add(card);
        }
    }

    public void advanceToNextBettingRound() {
        BettingStage nextStage;

        if (currentBettingRound == null) {
            nextStage = BettingStage.PREFLOP;
        } else {
            switch (currentBettingRound.getStage()) {
                case PREFLOP:
                    nextStage = BettingStage.FLOP;
                    dealCommunityCards(3);
                    break;
                case FLOP:
                    nextStage = BettingStage.TURN;
                    dealCommunityCards(1);
                    break;
                case TURN:
                    nextStage = BettingStage.RIVER;
                    dealCommunityCards(1);
                    break;
                default:
                    return;
            }
        }

        BettingRound newRound = new BettingRound();
        newRound.setStage(nextStage);
        newRound.setCurrentBet(0.0);
        newRound.setGameRound(this);

        bettingRounds.add(newRound);
        currentBettingRound = newRound;
    }
}