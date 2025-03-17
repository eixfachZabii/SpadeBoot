package com.pokerapp.service;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.game.GameRound;
import com.pokerapp.domain.poker.HandRank;
import com.pokerapp.domain.user.Player;

import java.util.List;
import java.util.Map;

/**
 * Service interface for poker hand evaluation and winner determination.
 */
public interface HandEvaluationService {

    /**
     * Evaluates the best possible poker hand from the player's cards and community cards.
     *
     * @param playerCards The player's hole cards
     * @param communityCards The community cards on the table
     * @return The hand rank of the best possible hand
     * @throws IllegalArgumentException if there are insufficient cards or invalid inputs
     */
    HandRank evaluateHand(List<Card> playerCards, List<Card> communityCards);

    /**
     * Determines the winner(s) of a poker game round and calculates their winnings.
     *
     * @param gameRound The current game round containing players and community cards
     * @return A map of winning players to their respective winnings
     */
    Map<Player, Double> determineWinners(GameRound gameRound);
}