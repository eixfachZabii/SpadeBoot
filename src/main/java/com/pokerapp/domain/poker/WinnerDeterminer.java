package com.pokerapp.domain.poker;

import com.pokerapp.domain.card.Card;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to determine the winner(s) among multiple players in a poker game.
 */
public class WinnerDeterminer {
    private final HandEvaluator handEvaluator;
    private final List<Card> communityCards;

    /**
     * Constructs a new WinnerDeterminer with the given community cards.
     * @param communityCards The community cards on the table
     */
    public WinnerDeterminer(List<Card> communityCards) {
        this.handEvaluator = new HandEvaluator();
        this.communityCards = new ArrayList<>(communityCards);
    }

    /**
     * Determines the winner(s) among the given players.
     * @param players A map of player IDs to their hole cards
     * @return A list of WinnerResult objects representing the winner(s) and their best hands
     */
    public List<WinnerResult> determineWinners(Map<String, List<Card>> players) {
        if (players.isEmpty()) {
            return Collections.emptyList();
        }

        // Evaluate each player's best hand
        Map<String, HandResult> playerResults = new HashMap<>();
        for (Map.Entry<String, List<Card>> entry : players.entrySet()) {
            String playerId = entry.getKey();
            List<Card> holeCards = entry.getValue();

            HandResult result = handEvaluator.evaluateBestHand(holeCards, communityCards);
            playerResults.put(playerId, result);
        }

        // Find the best hand rank among all players
        HandRank bestRank = playerResults.values().stream()
                .map(HandResult::getHandRank)
                .min(Comparator.comparingInt(HandRank::getValue))
                .orElseThrow(() -> new IllegalStateException("No best rank found"));

        // Filter players with the best rank
        Map<String, HandResult> bestRankPlayers = playerResults.entrySet().stream()
                .filter(e -> e.getValue().getHandRank() == bestRank)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // If only one player has the best rank, they are the winner
        if (bestRankPlayers.size() == 1) {
            Map.Entry<String, HandResult> winner = bestRankPlayers.entrySet().iterator().next();
            return Collections.singletonList(new WinnerResult(winner.getKey(), winner.getValue()));
        }

        // If multiple players have the best rank, compare their hands
        return findWinnersWithSameRank(bestRankPlayers);
    }

    /**
     * Finds the winner(s) among players with the same hand rank.
     * @param playerResults Map of player IDs to their hand results
     * @return A list of WinnerResult objects representing the winner(s)
     */
    private List<WinnerResult> findWinnersWithSameRank(Map<String, HandResult> playerResults) {
        List<Map.Entry<String, HandResult>> players = new ArrayList<>(playerResults.entrySet());

        // Sort players by hand strength (best hand first)
        players.sort((e1, e2) -> -handEvaluator.compareEqualRankHands(e1.getValue(), e2.getValue()));

        // Get the best hand value
        int bestHandValue = handEvaluator.compareEqualRankHands(players.get(0).getValue(), players.get(0).getValue());

        // Find all players with the best hand value (could be multiple in case of a tie)
        List<WinnerResult> winners = new ArrayList<>();
        for (Map.Entry<String, HandResult> player : players) {
            if (handEvaluator.compareEqualRankHands(player.getValue(), players.get(0).getValue()) == 0) {
                winners.add(new WinnerResult(player.getKey(), player.getValue()));
            } else {
                // Once we find a player with a worse hand, we can stop
                break;
            }
        }

        return winners;
    }

    /**
     * Class to represent a winner result.
     */
    public static class WinnerResult {
        private final String playerId;
        private final HandResult handResult;

        /**
         * Constructs a new WinnerResult.
         * @param playerId The ID of the player
         * @param handResult The best hand result of the player
         */
        public WinnerResult(String playerId, HandResult handResult) {
            this.playerId = playerId;
            this.handResult = handResult;
        }

        /**
         * Gets the player ID.
         * @return The player ID
         */
        public String getPlayerId() {
            return playerId;
        }

        /**
         * Gets the hand result.
         * @return The hand result
         */
        public HandResult getHandResult() {
            return handResult;
        }

        /**
         * Returns a string representation of the winner result.
         * @return A string representing the winner result
         */
        @Override
        public String toString() {
            return "Player " + playerId + " with " + handResult;
        }
    }
}