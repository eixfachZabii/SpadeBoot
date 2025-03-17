package com.pokerapp.domain.poker;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Suit;
import com.pokerapp.domain.card.c_Rank;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to evaluate poker hands and determine the best 5-card hand from a set of cards.
 */
public class HandEvaluator {

    /**
     * Evaluates the best 5-card hand from the given cards.
     * @param playerCards The player's 2 hole cards
     * @param communityCards The community cards (up to 5)
     * @return A HandResult object containing the hand rank and the best 5 cards
     */
    public HandResult evaluateBestHand(List<Card> playerCards, List<Card> communityCards) {
        // Combine player cards and community cards
        List<Card> allCards = new ArrayList<>(playerCards);
        allCards.addAll(communityCards);

        // Check if there are duplicate cards
        if (hasDuplicateCards(allCards)) {
            throw new IllegalArgumentException("Duplicate cards found in the hand");
        }

        // Get all possible 5-card combinations
        List<List<Card>> combinations = getAllCombinations(allCards, 5);

        HandResult bestResult = null;

        // Evaluate each combination and find the best hand
        for (List<Card> combination : combinations) {
            HandResult result = evaluateHand(combination);

            if (bestResult == null || result.getHandRank().getValue() > bestResult.getHandRank().getValue() ||
                    (result.getHandRank().equals(bestResult.getHandRank()) &&
                            compareEqualRankHands(result, bestResult) > 0)) {
                bestResult = result;
            }
        }

        return bestResult;
    }

    /**
     * Checks if the list of cards contains any duplicates.
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
     * Generates all possible combinations of k elements from the input list.
     * @param input The input list
     * @param k The size of each combination
     * @return A list of all possible combinations
     */
    private <T> List<List<T>> getAllCombinations(List<T> input, int k) {
        List<List<T>> result = new ArrayList<>();
        generateCombinations(input, k, 0, new ArrayList<>(), result);
        return result;
    }

    /**
     * Helper method for generating combinations.
     */
    private <T> void generateCombinations(List<T> input, int k, int start, List<T> current, List<List<T>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < input.size(); i++) {
            current.add(input.get(i));
            generateCombinations(input, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    /**
     * Evaluates a 5-card hand and determines its rank.
     * @param hand The 5-card hand to evaluate
     * @return A HandResult object containing the hand rank and hand details
     */
    private HandResult evaluateHand(List<Card> hand) {
        if (hand.size() != 5) {
            throw new IllegalArgumentException("Hand must contain exactly 5 cards");
        }

        // Sort the hand by rank in descending order
        List<Card> sortedHand = new ArrayList<>(hand);
        sortedHand.sort((c1, c2) -> Integer.compare(c2.getCRank().getValue(), c1.getCRank().getValue()));

        // Check for Royal Flush
        if (isRoyalFlush(sortedHand)) {
            return new HandResult(HandRank.ROYAL_FLUSH, sortedHand);
        }

        // Check for Straight Flush
        if (isStraightFlush(sortedHand)) {
            return new HandResult(HandRank.STRAIGHT_FLUSH, sortedHand);
        }

        // Check for Four of a Kind
        if (isFourOfAKind(sortedHand)) {
            return new HandResult(HandRank.FOUR_OF_A_KIND, sortedHand);
        }

        // Check for Full House
        if (isFullHouse(sortedHand)) {
            return new HandResult(HandRank.FULL_HOUSE, sortedHand);
        }

        // Check for Flush
        if (isFlush(sortedHand)) {
            return new HandResult(HandRank.FLUSH, sortedHand);
        }

        // Check for Straight
        if (isStraight(sortedHand)) {
            return new HandResult(HandRank.STRAIGHT, sortedHand);
        }

        // Check for Three of a Kind
        if (isThreeOfAKind(sortedHand)) {
            return new HandResult(HandRank.THREE_OF_A_KIND, sortedHand);
        }

        // Check for Two Pair
        if (isTwoPair(sortedHand)) {
            return new HandResult(HandRank.TWO_PAIR, sortedHand);
        }

        // Check for One Pair
        if (isOnePair(sortedHand)) {
            return new HandResult(HandRank.PAIR, sortedHand);
        }

        // High Card
        return new HandResult(HandRank.HIGH_CARD, sortedHand);
    }

    /**
     * Compares two hands of the same rank to determine which is better.
     * @param hand1 The first hand result
     * @param hand2 The second hand result
     * @return positive if hand1 is better, negative if hand2 is better, 0 if equal
     */
    public int compareEqualRankHands(HandResult hand1, HandResult hand2) {
        if (!hand1.getHandRank().equals(hand2.getHandRank())) {
            throw new IllegalArgumentException("Hand ranks must be equal to compare");
        }

        HandRank rank = hand1.getHandRank();
        List<Card> cards1 = hand1.getBestCards();
        List<Card> cards2 = hand2.getBestCards();

        // Sort hands by rank in descending order
        cards1.sort((c1, c2) -> Integer.compare(c2.getCRank().getValue(), c1.getCRank().getValue()));
        cards2.sort((c1, c2) -> Integer.compare(c2.getCRank().getValue(), c1.getCRank().getValue()));

        return switch (rank) {
            case ROYAL_FLUSH ->
                // All royal flushes are equal
                    0;
            case STRAIGHT_FLUSH, STRAIGHT ->
                // Compare the highest card in the straight
                    compareHighestCard(cards1, cards2);
            case FOUR_OF_A_KIND ->
                // Compare the rank of the four cards, then the kicker
                    compareFourOfAKind(cards1, cards2);
            case FULL_HOUSE ->
                // Compare the three of a kind rank, then the pair rank
                    compareFullHouse(cards1, cards2);
            case FLUSH, HIGH_CARD ->
                // Compare cards one by one from highest to lowest
                    compareCardByCard(cards1, cards2);
            case THREE_OF_A_KIND ->
                // Compare the three of a kind rank, then the kickers
                    compareThreeOfAKind(cards1, cards2);
            case TWO_PAIR ->
                // Compare the higher pair, then the lower pair, then the kicker
                    compareTwoPair(cards1, cards2);
            case PAIR ->
                // Compare the pair rank, then the kickers
                    comparePair(cards1, cards2);
            default -> throw new IllegalStateException("Unknown hand rank: " + rank);
        };
    }

    /**
     * Checks if the hand is a royal flush.
     * @param hand The hand to check
     * @return true if the hand is a royal flush, false otherwise
     */
    private boolean isRoyalFlush(List<Card> hand) {
        // A royal flush is a straight flush with A-K-Q-J-10
        if (!isFlush(hand)) {
            return false;
        }

        Set<c_Rank> royalRanks = new HashSet<>(Arrays.asList(
                c_Rank.ACE, c_Rank.KING, c_Rank.QUEEN, c_Rank.JACK, c_Rank.TEN));

        Set<c_Rank> handRanks = hand.stream().map(Card::getCRank).collect(Collectors.toSet());

        return handRanks.equals(royalRanks);
    }

    /**
     * Checks if the hand is a straight flush.
     * @param hand The hand to check
     * @return true if the hand is a straight flush, false otherwise
     */
    private boolean isStraightFlush(List<Card> hand) {
        return isFlush(hand) && isStraight(hand);
    }

    /**
     * Checks if the hand is four of a kind.
     * @param hand The hand to check
     * @return true if the hand is four of a kind, false otherwise
     */
    private boolean isFourOfAKind(List<Card> hand) {
        Map<c_Rank, Integer> rankCounts = getRankCounts(hand);
        return rankCounts.values().contains(4);
    }

    /**
     * Checks if the hand is a full house.
     * @param hand The hand to check
     * @return true if the hand is a full house, false otherwise
     */
    private boolean isFullHouse(List<Card> hand) {
        Map<c_Rank, Integer> rankCounts = getRankCounts(hand);
        return rankCounts.values().contains(3) && rankCounts.values().contains(2);
    }

    /**
     * Checks if the hand is a flush.
     * @param hand The hand to check
     * @return true if the hand is a flush, false otherwise
     */
    private boolean isFlush(List<Card> hand) {
        Suit firstSuit = hand.get(0).getSuit();
        return hand.stream().allMatch(card -> card.getSuit() == firstSuit);
    }

    /**
     * Checks if the hand is a straight.
     * @param hand The hand to check
     * @return true if the hand is a straight, false otherwise
     */
    private boolean isStraight(List<Card> hand) {
        List<Integer> values = hand.stream()
                .map(card -> card.getCRank().getValue())
                .sorted()
                .collect(Collectors.toList());

        // Check for special case: A-2-3-4-5 (Ace low straight)
        if (values.equals(Arrays.asList(2, 3, 4, 5, 14))) {
            return true;
        }

        // Check for consecutive values
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i) + 1 != values.get(i + 1)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the hand is three of a kind.
     * @param hand The hand to check
     * @return true if the hand is three of a kind, false otherwise
     */
    private boolean isThreeOfAKind(List<Card> hand) {
        Map<c_Rank, Integer> rankCounts = getRankCounts(hand);
        return rankCounts.values().contains(3) && !rankCounts.values().contains(2);
    }

    /**
     * Checks if the hand is two pair.
     * @param hand The hand to check
     * @return true if the hand is two pair, false otherwise
     */
    private boolean isTwoPair(List<Card> hand) {
        Map<c_Rank, Integer> rankCounts = getRankCounts(hand);
        return Collections.frequency(rankCounts.values(), 2) == 2;
    }

    /**
     * Checks if the hand is one pair.
     * @param hand The hand to check
     * @return true if the hand is one pair, false otherwise
     */
    private boolean isOnePair(List<Card> hand) {
        Map<c_Rank, Integer> rankCounts = getRankCounts(hand);
        return Collections.frequency(rankCounts.values(), 2) == 1
                && !rankCounts.values().contains(3)
                && !rankCounts.values().contains(4);
    }

    /**
     * Gets the counts of each rank in the hand.
     * @param hand The hand to analyze
     * @return A map of ranks to their counts
     */
    private Map<c_Rank, Integer> getRankCounts(List<Card> hand) {
        Map<c_Rank, Integer> counts = new HashMap<>();
        for (Card card : hand) {
            counts.put(card.getCRank(), counts.getOrDefault(card.getCRank(), 0) + 1);
        }
        return counts;
    }

    /**
     * Compares two hands card by card.
     * @param hand1 The first hand
     * @param hand2 The second hand
     * @return positive if hand1 is better, negative if hand2 is better, 0 if equal
     */
    private int compareCardByCard(List<Card> hand1, List<Card> hand2) {
        for (int i = 0; i < hand1.size(); i++) {
            int comp = Integer.compare(hand1.get(i).getCRank().getValue(),
                    hand2.get(i).getCRank().getValue());
            if (comp != 0) {
                return comp;
            }
        }
        return 0;
    }

    /**
     * Compares the highest card in two hands.
     * @param hand1 The first hand
     * @param hand2 The second hand
     * @return positive if hand1 is better, negative if hand2 is better, 0 if equal
     */
    private int compareHighestCard(List<Card> hand1, List<Card> hand2) {
        int maxRank1 = hand1.stream().mapToInt(c -> c.getCRank().getValue()).max().orElse(0);
        int maxRank2 = hand2.stream().mapToInt(c -> c.getCRank().getValue()).max().orElse(0);

        // Special case for A-5 straight (Ace is low)
        if (isAceLowStraight(hand1)) {
            maxRank1 = 5;
        }
        if (isAceLowStraight(hand2)) {
            maxRank2 = 5;
        }

        return Integer.compare(maxRank1, maxRank2);
    }

    /**
     * Checks if the hand is an A-5 straight.
     * @param hand The hand to check
     * @return true if the hand is an A-5 straight, false otherwise
     */
    private boolean isAceLowStraight(List<Card> hand) {
        List<Integer> values = hand.stream()
                .map(card -> card.getCRank().getValue())
                .sorted()
                .collect(Collectors.toList());

        return values.equals(Arrays.asList(2, 3, 4, 5, 14));
    }

    /**
     * Compares two four of a kind hands.
     * @param hand1 The first hand
     * @param hand2 The second hand
     * @return positive if hand1 is better, negative if hand2 is better, 0 if equal
     */
    private int compareFourOfAKind(List<Card> hand1, List<Card> hand2) {
        c_Rank fourRank1 = getFourOfAKindRank(hand1);
        c_Rank fourRank2 = getFourOfAKindRank(hand2);

        int comp = Integer.compare(fourRank1.getValue(), fourRank2.getValue());
        if (comp != 0) {
            return comp;
        }

        // Compare kickers
        c_Rank kicker1 = getKickerInFourOfAKind(hand1, fourRank1);
        c_Rank kicker2 = getKickerInFourOfAKind(hand2, fourRank2);

        return Integer.compare(kicker1.getValue(), kicker2.getValue());
    }

    /**
     * Gets the rank of the four of a kind.
     * @param hand The hand containing four of a kind
     * @return The rank of the four of a kind
     */
    private c_Rank getFourOfAKindRank(List<Card> hand) {
        Map<c_Rank, Integer> rankCounts = getRankCounts(hand);
        return rankCounts.entrySet().stream()
                .filter(e -> e.getValue() == 4)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No four of a kind found"));
    }

    /**
     * Gets the kicker in a four of a kind hand.
     * @param hand The hand containing four of a kind
     * @param fourRank The rank of the four of a kind
     * @return The rank of the kicker
     */
    private c_Rank getKickerInFourOfAKind(List<Card> hand, c_Rank fourRank) {
        return hand.stream()
                .filter(c -> c.getCRank() != fourRank)
                .map(Card::getCRank)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No kicker found"));
    }

    /**
     * Compares two full house hands.
     * @param hand1 The first hand
     * @param hand2 The second hand
     * @return positive if hand1 is better, negative if hand2 is better, 0 if equal
     */
    private int compareFullHouse(List<Card> hand1, List<Card> hand2) {
        c_Rank threeRank1 = getThreeOfAKindRank(hand1);
        c_Rank threeRank2 = getThreeOfAKindRank(hand2);

        int comp = Integer.compare(threeRank1.getValue(), threeRank2.getValue());
        if (comp != 0) {
            return comp;
        }

        // Compare pair ranks
        c_Rank pairRank1 = getPairRankInFullHouse(hand1, threeRank1);
        c_Rank pairRank2 = getPairRankInFullHouse(hand2, threeRank2);

        return Integer.compare(pairRank1.getValue(), pairRank2.getValue());
    }

    /**
     * Gets the rank of the three of a kind.
     * @param hand The hand containing three of a kind
     * @return The rank of the three of a kind
     */
    private c_Rank getThreeOfAKindRank(List<Card> hand) {
        Map<c_Rank, Integer> rankCounts = getRankCounts(hand);
        return rankCounts.entrySet().stream()
                .filter(e -> e.getValue() == 3)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No three of a kind found"));
    }

    /**
     * Gets the rank of the pair in a full house.
     * @param hand The hand containing a full house
     * @param threeRank The rank of the three of a kind
     * @return The rank of the pair
     */
    private c_Rank getPairRankInFullHouse(List<Card> hand, c_Rank threeRank) {
        Map<c_Rank, Integer> rankCounts = getRankCounts(hand);
        return rankCounts.entrySet().stream()
                .filter(e -> e.getValue() == 2)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No pair found in full house"));
    }

    /**
     * Compares two three of a kind hands.
     * @param hand1 The first hand
     * @param hand2 The second hand
     * @return positive if hand1 is better, negative if hand2 is better, 0 if equal
     */
    private int compareThreeOfAKind(List<Card> hand1, List<Card> hand2) {
        c_Rank threeRank1 = getThreeOfAKindRank(hand1);
        c_Rank threeRank2 = getThreeOfAKindRank(hand2);

        int comp = Integer.compare(threeRank1.getValue(), threeRank2.getValue());
        if (comp != 0) {
            return comp;
        }

        // Compare kickers
        List<Card> kickers1 = getKickersInThreeOfAKind(hand1, threeRank1);
        List<Card> kickers2 = getKickersInThreeOfAKind(hand2, threeRank2);

        // Sort kickers by rank in descending order
        kickers1.sort((c1, c2) -> Integer.compare(c2.getCRank().getValue(), c1.getCRank().getValue()));
        kickers2.sort((c1, c2) -> Integer.compare(c2.getCRank().getValue(), c1.getCRank().getValue()));

        // Compare kickers one by one
        for (int i = 0; i < kickers1.size(); i++) {
            comp = Integer.compare(kickers1.get(i).getCRank().getValue(),
                    kickers2.get(i).getCRank().getValue());
            if (comp != 0) {
                return comp;
            }
        }

        return 0;
    }

    /**
     * Gets the kickers in a three of a kind hand.
     * @param hand The hand containing three of a kind
     * @param threeRank The rank of the three of a kind
     * @return The list of kickers
     */
    private List<Card> getKickersInThreeOfAKind(List<Card> hand, c_Rank threeRank) {
        return hand.stream()
                .filter(c -> c.getCRank() != threeRank)
                .collect(Collectors.toList());
    }

    /**
     * Compares two two pair hands.
     * @param hand1 The first hand
     * @param hand2 The second hand
     * @return positive if hand1 is better, negative if hand2 is better, 0 if equal
     */
    private int compareTwoPair(List<Card> hand1, List<Card> hand2) {
        List<c_Rank> pairs1 = getTwoPairRanks(hand1);
        List<c_Rank> pairs2 = getTwoPairRanks(hand2);

        // Sort pairs by rank in descending order
        pairs1.sort((r1, r2) -> Integer.compare(r2.getValue(), r1.getValue()));
        pairs2.sort((r1, r2) -> Integer.compare(r2.getValue(), r1.getValue()));

        // Compare high pair
        int comp = Integer.compare(pairs1.get(0).getValue(), pairs2.get(0).getValue());
        if (comp != 0) {
            return comp;
        }

        // Compare low pair
        comp = Integer.compare(pairs1.get(1).getValue(), pairs2.get(1).getValue());
        if (comp != 0) {
            return comp;
        }

        // Compare kicker
        c_Rank kicker1 = getKickerInTwoPair(hand1, pairs1);
        c_Rank kicker2 = getKickerInTwoPair(hand2, pairs2);

        return Integer.compare(kicker1.getValue(), kicker2.getValue());
    }

    /**
     * Gets the ranks of the two pairs.
     * @param hand The hand containing two pairs
     * @return The list of ranks of the two pairs
     */
    private List<c_Rank> getTwoPairRanks(List<Card> hand) {
        Map<c_Rank, Integer> rankCounts = getRankCounts(hand);
        return rankCounts.entrySet().stream()
                .filter(e -> e.getValue() == 2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Gets the kicker in a two pair hand.
     * @param hand The hand containing two pairs
     * @param pairRanks The ranks of the two pairs
     * @return The rank of the kicker
     */
    private c_Rank getKickerInTwoPair(List<Card> hand, List<c_Rank> pairRanks) {
        return hand.stream()
                .filter(c -> !pairRanks.contains(c.getCRank()))
                .map(Card::getCRank)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No kicker found in two pair"));
    }

    /**
     * Compares two one pair hands.
     * @param hand1 The first hand
     * @param hand2 The second hand
     * @return positive if hand1 is better, negative if hand2 is better, 0 if equal
     */
    private int comparePair(List<Card> hand1, List<Card> hand2) {
        c_Rank pairRank1 = getPairRank(hand1);
        c_Rank pairRank2 = getPairRank(hand2);

        int comp = Integer.compare(pairRank1.getValue(), pairRank2.getValue());
        if (comp != 0) {
            return comp;
        }

        // Compare kickers
        List<Card> kickers1 = getKickersInPair(hand1, pairRank1);
        List<Card> kickers2 = getKickersInPair(hand2, pairRank2);

        // Sort kickers by rank in descending order
        kickers1.sort((c1, c2) -> Integer.compare(c2.getCRank().getValue(), c1.getCRank().getValue()));
        kickers2.sort((c1, c2) -> Integer.compare(c2.getCRank().getValue(), c1.getCRank().getValue()));

        // Compare kickers one by one
        for (int i = 0; i < kickers1.size(); i++) {
            comp = Integer.compare(kickers1.get(i).getCRank().getValue(),
                    kickers2.get(i).getCRank().getValue());
            if (comp != 0) {
                return comp;
            }
        }

        return 0;
    }

    /**
     * Gets the rank of the pair.
     * @param hand The hand containing one pair
     * @return The rank of the pair
     */
    private c_Rank getPairRank(List<Card> hand) {
        Map<c_Rank, Integer> rankCounts = getRankCounts(hand);
        return rankCounts.entrySet().stream()
                .filter(e -> e.getValue() == 2)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No pair found"));
    }

    /**
     * Gets the kickers in a one pair hand.
     * @param hand The hand containing one pair
     * @param pairRank The rank of the pair
     * @return The list of kickers
     */
    private List<Card> getKickersInPair(List<Card> hand, c_Rank pairRank) {
        return hand.stream()
                .filter(c -> c.getCRank() != pairRank)
                .collect(Collectors.toList());
    }
}