package com.pokerapp.domain.poker;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Suit;
import com.pokerapp.domain.card.c_Rank;
import com.pokerapp.domain.user.Player;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HandEvaluator {

    //TODO: ALLES CHECKEN ODER SELBER MACHEN

    public HandRank evaluateHand(List<Card> hand, List<Card> communityCards) {
        List<Card> allCards = new ArrayList<>(hand);
        allCards.addAll(communityCards);

        // Need at least 5 cards to form a hand
        if (allCards.size() < 5) {
            return HandRank.HIGH_CARD;
        }

        if (isRoyalFlush(allCards)) return HandRank.ROYAL_FLUSH;
        if (isStraightFlush(allCards)) return HandRank.STRAIGHT_FLUSH;
        if (isFourOfAKind(allCards)) return HandRank.FOUR_OF_A_KIND;
        if (isFullHouse(allCards)) return HandRank.FULL_HOUSE;
        if (isFlush(allCards)) return HandRank.FLUSH;
        if (isStraight(allCards)) return HandRank.STRAIGHT;
        if (isThreeOfAKind(allCards)) return HandRank.THREE_OF_A_KIND;
        if (isTwoPair(allCards)) return HandRank.TWO_PAIR;
        if (isPair(allCards)) return HandRank.PAIR;

        return HandRank.HIGH_CARD;
    }

    public Map<Player, Integer> compareHands(Map<Player, List<Card>> playerHands, List<Card> communityCards) {
        Map<Player, Integer> rankings = new HashMap<>();
        Map<Player, HandRank> handRanks = new HashMap<>();

        // First, evaluate each player's hand
        for (Map.Entry<Player, List<Card>> entry : playerHands.entrySet()) {
            Player player = entry.getKey();
            List<Card> hand = entry.getValue();
            HandRank rank = evaluateHand(hand, communityCards);
            handRanks.put(player, rank);
        }

        // Sort players by hand rank (lower value is better)
        List<Player> sortedPlayers = new ArrayList<>(handRanks.keySet());
        sortedPlayers.sort(Comparator.comparing(p -> handRanks.get(p).getValue() * -1));

        // Assign rankings (1 is best, handling ties)
        int currentRank = 1;
        HandRank previousRank = null;

        for (Player player : sortedPlayers) {
            HandRank rank = handRanks.get(player);

            // If this hand is weaker than the previous, increment the ranking
            if (previousRank != null && rank.getValue() < previousRank.getValue()) {
                currentRank++;
            }

            rankings.put(player, currentRank);
            previousRank = rank;
        }

        return rankings;
    }

    // Hand evaluation methods
    private boolean isRoyalFlush(List<Card> cards) {
        if (!isStraightFlush(cards)) {
            return false;
        }
        // Prüfen, ob die Karten genau A, K, Q, J, 10 enthalten
        Set<c_Rank> royalRanks = Set.of(c_Rank.ACE, c_Rank.KING, c_Rank.QUEEN, c_Rank.JACK, c_Rank.TEN);
        return cards.stream().map(Card::getCRank).collect(Collectors.toSet()).equals(royalRanks);
    }

    private boolean isStraightFlush(List<Card> cards) {
        // Group cards by suit
        Map<Suit, List<Card>> cardsBySuit = cards.stream()
                .collect(Collectors.groupingBy(Card::getSuit));

        // Check each suit group for a straight
        for (List<Card> sameSuitCards : cardsBySuit.values()) {
            if (sameSuitCards.size() >= 5 && isStraight(sameSuitCards)) {
                return true;
            }
        }

        return false;
    }

    private boolean isFourOfAKind(List<Card> cards) {
        Map<c_Rank, Long> rankCounts = getRankFrequency(cards);
        return rankCounts.values().stream().anyMatch(count -> count >= 4);
    }

    private boolean isFullHouse(List<Card> cards) {
        Map<c_Rank, Long> rankCounts = getRankFrequency(cards);
        boolean hasThree = rankCounts.values().stream().anyMatch(count -> count >= 3);
        boolean hasPair = rankCounts.values().stream().anyMatch(count -> count >= 2);

        // Make sure we have both a three of a kind and a pair with different ranks
        if (hasThree && hasPair) {
            c_Rank threeRank = rankCounts.entrySet().stream()
                    .filter(e -> e.getValue() >= 3)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);

            // Make sure there's another rank with at least 2 cards
            return rankCounts.entrySet().stream()
                    .anyMatch(e -> e.getKey() != threeRank && e.getValue() >= 2);
        }

        return false;
    }

    private boolean isFlush(List<Card> cards) {
        Map<Suit, Long> suitCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::getSuit, Collectors.counting()));
        return suitCounts.values().stream().anyMatch(count -> count >= 5);
    }

    private boolean isStraight(List<Card> cards) {
        // Need at least 5 cards for a straight
        if (cards.size() < 5) return false;

        // Get unique ranks sorted by value
        List<Integer> rankValues = cards.stream()
                .map(c -> c.getCRank().getValue())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Check for A-2-3-4-5 straight
        if (rankValues.contains(14) && // Ace
                rankValues.contains(2) &&
                rankValues.contains(3) &&
                rankValues.contains(4) &&
                rankValues.contains(5)) {
            return true;
        }

        // Check for consecutive values
        for (int i = 0; i <= rankValues.size() - 5; i++) {
            boolean isStraight = true;
            for (int j = 0; j < 4; j++) {
                if (rankValues.get(i + j + 1) != rankValues.get(i + j) + 1) {
                    isStraight = false;
                    break;
                }
            }
            if (isStraight) return true;
        }

        return false;
    }

    private boolean isThreeOfAKind(List<Card> cards) {
        Map<c_Rank, Long> rankCounts = getRankFrequency(cards);
        return rankCounts.values().stream().anyMatch(count -> count >= 3);
    }

    private boolean isTwoPair(List<Card> cards) {
        Map<c_Rank, Long> rankCounts = getRankFrequency(cards);
        long pairCount = rankCounts.values().stream().filter(count -> count >= 2).count();
        return pairCount >= 2;
    }

    private boolean isPair(List<Card> cards) {
        Map<c_Rank, Long> rankCounts = getRankFrequency(cards);
        return rankCounts.values().stream().anyMatch(count -> count >= 2);
    }

    public static List<Card> sortByRank(List<Card> cards) {
        return cards.stream()
                .sorted(Comparator.comparing(card -> card.getCRank().getValue()))
                .collect(Collectors.toList());
    }

    public static Map<c_Rank, Long> getRankFrequency(List<Card> cards) {
        return cards.stream()
                .collect(Collectors.groupingBy(Card::getCRank, Collectors.counting()));
    }
}