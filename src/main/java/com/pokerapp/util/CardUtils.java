// src/main/java/com/pokerapp/util/CardUtils.java
package com.pokerapp.util;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Rank;
import com.pokerapp.domain.card.Suit;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CardUtils {

    public static List<Card> sortByRank(List<Card> cards) {
        return cards.stream()
                .sorted(Comparator.comparing(card -> card.getRank().getValue()))
                .collect(Collectors.toList());
    }

    public static boolean isStraight(List<Card> cards) {
        List<Card> sortedCards = sortByRank(cards);

        // Check for A-2-3-4-5 straight
        boolean lowStraight = sortedCards.stream()
                .map(card -> card.getRank())
                .collect(Collectors.toSet())
                .containsAll(List.of(Rank.ACE, Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE));

        if (lowStraight) return true;

        // Check normal straight
        for (int i = 0; i < sortedCards.size() - 1; i++) {
            if (sortedCards.get(i + 1).getRank().getValue() -
                    sortedCards.get(i).getRank().getValue() != 1) {
                return false;
            }
        }

        return true;
    }

    public static boolean isFlush(List<Card> cards) {
        Suit firstSuit = cards.get(0).getSuit();
        return cards.stream().allMatch(card -> card.getSuit() == firstSuit);
    }

    public static Map<Rank, Long> getRankFrequency(List<Card> cards) {
        return cards.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));
    }
}
