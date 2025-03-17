package com.pokerapp.service.impl;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.game.GameRound;
import com.pokerapp.domain.poker.HandEvaluator;
import com.pokerapp.domain.poker.HandRank;
import com.pokerapp.domain.poker.HandResult;
import com.pokerapp.domain.poker.WinnerDeterminer;
import com.pokerapp.domain.user.Player;
import com.pokerapp.service.HandEvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HandEvaluationServiceImpl implements HandEvaluationService {
    private static final Logger logger = LoggerFactory.getLogger(HandEvaluationServiceImpl.class);

    private final HandEvaluator handEvaluator;

    @Autowired
    public HandEvaluationServiceImpl(HandEvaluator handEvaluator) {
        this.handEvaluator = handEvaluator;
    }

    @Override
    public HandRank evaluateHand(List<Card> playerCards, List<Card> communityCards) {
        if (playerCards == null || communityCards == null) {
            throw new IllegalArgumentException("Player cards and community cards cannot be null");
        }

        // Ensure we have enough cards to evaluate a hand (minimum 5 total)
        if (playerCards.size() + communityCards.size() < 5) {
            throw new IllegalArgumentException("Insufficient cards for hand evaluation. Need at least 5 cards in total.");
        }

        // Verify there are no duplicate cards
        List<Card> allCards = new ArrayList<>(playerCards);
        allCards.addAll(communityCards);
        if (hasDuplicateCards(allCards)) {
            throw new IllegalArgumentException("Duplicate cards found in hand evaluation");
        }

        // Delegate to the HandEvaluator for comprehensive hand evaluation
        HandResult result = handEvaluator.evaluateBestHand(playerCards, communityCards);
        return result.getHandRank();
    }

    @Override
    public Map<Player, Double> determineWinners(GameRound gameRound) {
        // Get community cards from the game round
        List<Card> communityCards = gameRound.getCommunityCards();

        // Filter for active players who still have valid hands
        List<Player> activePlayers = gameRound.getGame().getPokerTable().getPlayers().stream()
                .filter(this::isPlayerActive)
                .collect(Collectors.toList());

        if (activePlayers.isEmpty()) {
            logger.warn("No active players found for winner determination");
            return Collections.emptyMap();
        }

        // If only one active player remains, they win the entire pot
        if (activePlayers.size() == 1) {
            Player winner = activePlayers.get(0);
            Map<Player, Double> winnings = new HashMap<>();
            winnings.put(winner, gameRound.getPot());
            logger.info("Single active player {} wins pot of ${}", winner.getUsername(), gameRound.getPot());
            return winnings;
        }

        // Create a WinnerDeterminer with the community cards
        WinnerDeterminer winnerDeterminer = new WinnerDeterminer(communityCards);

        // Prepare player hands for comparison
        Map<String, List<Card>> playerHandsMap = new HashMap<>();
        Map<String, Player> playerIdMap = new HashMap<>();

        for (Player player : activePlayers) {
            String playerId = player.getId().toString();
            playerHandsMap.put(playerId, player.getHand().getCards());
            playerIdMap.put(playerId, player);
        }

        // Determine winners
        List<WinnerDeterminer.WinnerResult> winnerResults = winnerDeterminer.determineWinners(playerHandsMap);

        // Calculate pot distribution handling all-in scenarios and side pots
        Map<Player, Double> winnings = calculateComplexPotDistribution(gameRound, winnerResults, playerIdMap, activePlayers);

        // Log winner details for debugging
        logWinnerDetails(winnerResults, winnings, playerIdMap);

        return winnings;
    }

    /**
     * Checks if a player is active and eligible for hand evaluation.
     *
     * @param player The player to check
     * @return True if the player is active and has a valid hand
     */
    private boolean isPlayerActive(Player player) {
        return player != null
                && player.getHand() != null
                && player.getHand().getCards() != null
                && player.getHand().getCards().size() == 2  // In Texas Hold'em, players have 2 hole cards
                && !player.isFolded()
                && !player.isAllOut();
    }

    /**
     * Calculates the pot distribution among winners, handling all scenarios including side pots.
     *
     * @param gameRound The current game round
     * @param winnerResults The list of winner results
     * @param playerIdMap Map of player IDs to Player objects
     * @param activePlayers List of all active players
     * @return Map of players to their winnings
     */
    private Map<Player, Double> calculateComplexPotDistribution(
            GameRound gameRound,
            List<WinnerDeterminer.WinnerResult> winnerResults,
            Map<String, Player> playerIdMap,
            List<Player> activePlayers) {

        Map<Player, Double> winnings = new HashMap<>();

        if (winnerResults.isEmpty()) {
            logger.warn("No winners determined for pot distribution");
            return winnings;
        }

        // Convert winner results to Player objects
        List<Player> winners = winnerResults.stream()
                .map(result -> playerIdMap.get(result.getPlayerId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Handle all-in scenarios and side pots
        List<SidePot> sidePots = calculateSidePots(gameRound, activePlayers);

        if (sidePots.isEmpty()) {
            // No side pots, simple distribution
            double winningsPerPlayer = gameRound.getPot() / winners.size();
            winners.forEach(winner -> winnings.put(winner, winningsPerPlayer));
        } else {
            // Process each side pot
            for (SidePot sidePot : sidePots) {
                // Find eligible winners for this side pot
                List<Player> eligibleWinners = winners.stream()
                        .filter(sidePot::isPlayerEligible)
                        .collect(Collectors.toList());

                if (!eligibleWinners.isEmpty()) {
                    double winningsPerEligiblePlayer = sidePot.getAmount() / eligibleWinners.size();
                    for (Player eligibleWinner : eligibleWinners) {
                        winnings.put(eligibleWinner,
                                winnings.getOrDefault(eligibleWinner, 0.0) + winningsPerEligiblePlayer);
                    }
                } else {
                    // No eligible winners for this side pot (rare case)
                    logger.warn("No eligible winners for side pot of ${}", sidePot.getAmount());
                }
            }
        }

        return winnings;
    }

    /**
     * Calculates side pots based on player bets.
     *
     * @param gameRound The current game round
     * @param activePlayers The list of active players
     * @return A list of side pots, ordered from smallest to largest bet amount
     */
    private List<SidePot> calculateSidePots(GameRound gameRound, List<Player> activePlayers) {
        // Check if we need side pots
        boolean hasAllInPlayers = activePlayers.stream().anyMatch(Player::isAllIn);
        if (!hasAllInPlayers) {
            // No all-in players, just main pot
            return Collections.singletonList(new SidePot(gameRound.getPot(), activePlayers, Double.MAX_VALUE));
        }

        // Sort players by their total bet amount
        List<Player> sortedPlayers = new ArrayList<>(activePlayers);
        sortedPlayers.sort(Comparator.comparingDouble(Player::getTotalBet));

        List<SidePot> sidePots = new ArrayList<>();
        double previousBetAmount = 0;
        double remainingPot = gameRound.getPot();

        // Calculate side pots
        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player currentPlayer = sortedPlayers.get(i);
            double currentBetAmount = currentPlayer.getTotalBet();

            // Skip players with the same bet as previous player
            if (currentBetAmount <= previousBetAmount) {
                continue;
            }

            // Calculate pot contribution from the difference in bet amounts
            double potContribution = (currentBetAmount - previousBetAmount) * i;

            if (potContribution > 0) {
                // Create a side pot for players who at least matched this bet
                List<Player> eligiblePlayers = sortedPlayers.subList(i, sortedPlayers.size());
                sidePots.add(new SidePot(potContribution, eligiblePlayers, currentBetAmount));
                remainingPot -= potContribution;
            }

            previousBetAmount = currentBetAmount;
        }

        // Add the main pot (contains all remaining chips)
        if (remainingPot > 0) {
            sidePots.add(new SidePot(remainingPot, sortedPlayers, Double.MAX_VALUE));
        }

        return sidePots;
    }

    /**
     * Helper class to represent a side pot in a poker game.
     */
    private static class SidePot {
        private final double amount;
        private final List<Player> eligiblePlayers;
        private final double maxBetAmount;

        public SidePot(double amount, List<Player> eligiblePlayers, double maxBetAmount) {
            this.amount = amount;
            this.eligiblePlayers = eligiblePlayers;
            this.maxBetAmount = maxBetAmount;
        }

        public double getAmount() {
            return amount;
        }

        public boolean isPlayerEligible(Player player) {
            return eligiblePlayers.contains(player) && player.getTotalBet() >= maxBetAmount;
        }
    }

    /**
     * Logs detailed information about the winners for debugging purposes.
     *
     * @param winnerResults The list of winner results
     * @param winnings Map of players to their winnings
     * @param playerIdMap Map of player IDs to Player objects
     */
    private void logWinnerDetails(
            List<WinnerDeterminer.WinnerResult> winnerResults,
            Map<Player, Double> winnings,
            Map<String, Player> playerIdMap) {

        logger.info("Winner determination completed with {} winner(s)", winnerResults.size());

        for (WinnerDeterminer.WinnerResult result : winnerResults) {
            Player winner = playerIdMap.get(result.getPlayerId());
            if (winner != null) {
                logger.info("Winner: {} (ID: {})", winner.getUsername(), winner.getId());
                logger.info("  Hand: {}", result.getHandResult());
                logger.info("  Winnings: ${}", winnings.get(winner));

                // Log the cards that make up the winning hand
                List<Card> winningCards = result.getHandResult().getBestCards();
                String cards = winningCards.stream()
                        .map(Card::toString)
                        .collect(Collectors.joining(", "));
                logger.info("  Winning Cards: {}", cards);
            }
        }
    }

    /**
     * Checks if the list of cards contains any duplicates.
     *
     * @param cards The list of cards to check
     * @return true if duplicates are found, false otherwise
     */
    private boolean hasDuplicateCards(List<Card> cards) {
        Set<String> uniqueCards = new HashSet<>();
        for (Card card : cards) {
            String cardKey = card.getCRank() + "-" + card.getSuit();
            if (!uniqueCards.add(cardKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate hand strength as a numerical value for comparison.
     * Lower values indicate stronger hands.
     *
     * @param handRank The hand rank to evaluate
     * @return A numerical value representing hand strength
     */
    private int calculateHandStrength(HandRank handRank) {
        // HandRank enum values are already ordered from strongest to weakest
        return handRank.getValue();
    }
}