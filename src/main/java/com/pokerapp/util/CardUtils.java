// src/main/java/com/pokerapp/util/CardUtils.java
package com.pokerapp.util;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.c_Rank;
import com.pokerapp.domain.card.Suit;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//TODO
public class CardUtils {

    public static List<Card> sortByRank(List<Card> cards) {
        return cards.stream()
                .sorted(Comparator.comparing(card -> card.getCRank().getValue()))
                .collect(Collectors.toList());
    }

    public static boolean isStraight(List<Card> cards) {
        List<Card> sortedCards = sortByRank(cards);

        // Check for A-2-3-4-5 straight
        boolean lowStraight = sortedCards.stream()
                .map(Card::getCRank)
                .collect(Collectors.toSet())
                .containsAll(List.of(c_Rank.ACE, c_Rank.TWO, c_Rank.THREE, c_Rank.FOUR, c_Rank.FIVE));

        if (lowStraight) return true;

        // Check normal straight
        for (int i = 0; i < sortedCards.size() - 1; i++) {
            if (sortedCards.get(i + 1).getCRank().getValue() -
                    sortedCards.get(i).getCRank().getValue() != 1) {
                return false;
            }
        }

        return true;
    }

    public static boolean isFlush(List<Card> cards) {
        Suit firstSuit = cards.get(0).getSuit();
        return cards.stream().allMatch(card -> card.getSuit() == firstSuit);
    }

    public static Map<c_Rank, Long> getRankFrequency(List<Card> cards) {
        return cards.stream()
                .collect(Collectors.groupingBy(Card::getCRank, Collectors.counting()));
    }
}
