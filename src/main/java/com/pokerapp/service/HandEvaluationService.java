// src/main/java/com/pokerapp/service/HandEvaluationService.java
package com.pokerapp.service;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.game.GameRound;
import com.pokerapp.domain.poker.HandRank;
import com.pokerapp.domain.user.Player;

import java.util.List;
import java.util.Map;

public interface HandEvaluationService {
    HandRank evaluateHand(List<Card> playerCards, List<Card> communityCards);
    Map<Player, Double> determineWinners(GameRound gameRound);
}
