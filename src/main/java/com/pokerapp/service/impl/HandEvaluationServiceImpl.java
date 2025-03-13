// src/main/java/com/pokerapp/service/impl/HandEvaluationServiceImpl.java
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
        return handEvaluator.evaluateHand(playerCards, communityCards);
    }

    @Override
    public Map<Player, Double> determineWinners(GameRound gameRound) {
        List<Player> activePlayers = gameRound.getGame().getPokerTable().getPlayers().stream()
                .filter(p -> p.getHand() != null && p.getHand().getCards().size() == 2)
                .collect(Collectors.toList());

        Map<Player, List<Card>> playerHands = activePlayers.stream()
                .collect(Collectors.toMap(
                        player -> player,
                        player -> player.getHand().getCards()
                ));

        Map<Player, Integer> rankings = handEvaluator.compareHands(playerHands, gameRound.getCommunityCards());

        // Find the highest rank (lowest number = best hand)
        int bestRank = rankings.values().stream()
                .min(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);

        // Find all players with the best rank
        List<Player> winners = rankings.entrySet().stream()
                .filter(entry -> entry.getValue() == bestRank)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Calculate prize distribution
        double totalPot = gameRound.getPot();
        double prizePerWinner = totalPot / winners.size();

        Map<Player, Double> winnings = new HashMap<>();
        for (Player winner : winners) {
            winnings.put(winner, prizePerWinner);
        }

        return winnings;
    }
}
