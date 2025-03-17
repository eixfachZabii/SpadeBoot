package com.pokerapp.domain.poker;

import com.pokerapp.domain.card.Card;

import java.util.List;

/**
 * Class to represent the result of a hand evaluation.
 */
public class HandResult {
    private final HandRank handRank;
    private final List<Card> bestCards;

    /**
     * Constructs a new HandResult.
     * @param handRank The rank of the hand
     * @param bestCards The best 5 cards that make up the hand
     */
    public HandResult(HandRank handRank, List<Card> bestCards) {
        this.handRank = handRank;
        this.bestCards = bestCards;
    }

    /**
     * Gets the rank of the hand.
     * @return The hand rank
     */
    public HandRank getHandRank() {
        return handRank;
    }

    /**
     * Gets the best 5 cards that make up the hand.
     * @return The list of cards
     */
    public List<Card> getBestCards() {
        return bestCards;
    }

    /**
     * Returns a string representation of the hand result.
     * @return A string representing the hand result
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(handRank.name()).append(": ");

        for (Card card : bestCards) {
            sb.append(card.toString()).append(", ");
        }

        // Remove the trailing comma and space
        if (!bestCards.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
    }
}