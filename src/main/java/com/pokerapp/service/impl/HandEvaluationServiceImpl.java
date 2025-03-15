package com.pokerapp.service.impl;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.game.GameRound;
import com.pokerapp.domain.poker.HandEvaluator;
import com.pokerapp.domain.poker.HandRank;
import com.pokerapp.domain.user.Player;
import com.pokerapp.service.HandEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HandEvaluationServiceImpl implements HandEvaluationService {

    @Autowired
    private final HandEvaluator handEvaluator;

    @Autowired
    public HandEvaluationServiceImpl(HandEvaluator handEvaluator) {
        this.handEvaluator = handEvaluator;
    }

    @Override
    public HandRank evaluateHand(List<Card> playerCards, List<Card> communityCards) {
        // TODO: Implement comprehensive hand evaluation
        // - Validate input (minimum 5 cards total)
        // - Handle edge cases (insufficient cards, empty lists)
        // - Consider different poker variants (Texas Hold'em, Omaha, etc.)

        // Current implementation delegates to HandEvaluator
        return handEvaluator.evaluateHand(playerCards, communityCards);
    }

    @Override
    public Map<Player, Double> determineWinners(GameRound gameRound) {
        // TODO: Implement advanced winner determination logic

        // Initial filter for active players
        List<Player> activePlayers = gameRound.getGame().getPokerTable().getPlayers().stream()
                .filter(p -> {
                    // TODO: Enhance player eligibility check
                    // - Verify player is still in the game
                    // - Check player's hand and status
                    return p.getHand() != null && p.getHand().getCards().size() == 2;
                })
                .collect(Collectors.toList());

        // Prepare player hands for comparison
        Map<Player, List<Card>> playerHands = activePlayers.stream()
                .collect(Collectors.toMap(
                        player -> player,
                        player -> player.getHand().getCards()
                ));

        // Determine hand rankings
        Map<Player, Integer> rankings = handEvaluator.compareHands(playerHands, gameRound.getCommunityCards());

        // TODO: Implement more sophisticated pot distribution
        // - Handle side pots
        // - Deal with all-in scenarios
        // - Consider different betting round contributions

        // Find the best rank
        int bestRank = rankings.values().stream()
                .min(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);

        // Identify winners with the best rank
        List<Player> winners = rankings.entrySet().stream()
                .filter(entry -> entry.getValue() == bestRank)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Basic pot distribution
        double totalPot = gameRound.getPot();
        double prizePerWinner = totalPot / Math.max(1, winners.size());

        Map<Player, Double> winnings = new HashMap<>();
        for (Player winner : winners) {
            // TODO: Implement more nuanced winnings calculation
            // - Consider player's initial bet
            // - Handle cases with multiple winners
            winnings.put(winner, prizePerWinner);
        }

        // TODO: Add logging or additional winner determination logic
        // - Log winner details
        // - Perform any additional game-specific winner processing

        return winnings;
    }

    // TODO: Add additional utility methods
    // - Method to calculate hand strength
    // - Method to handle tie-breaking scenarios
    // - Method to validate hand combinations

    /**
     * Utility method for logging and debugging winner determination
     * @param winners List of winning players
     * @param winnings Winnings distribution
     */
    private void logWinnerDetails(List<Player> winners, Map<Player, Double> winnings) {
        // TODO: Implement detailed winner logging
        // - Log each winner's username
        // - Log their winning amount
        // - Log their hand combination
    }
}