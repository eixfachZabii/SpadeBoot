package com.pokerapp.service.impl.logic;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.game.GameRound;
import com.pokerapp.domain.poker.HandEvaluator;
import com.pokerapp.domain.poker.HandRank;
import com.pokerapp.domain.user.Player;
import com.pokerapp.service.HandEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HandEvaluationServiceImpl implements HandEvaluationService {

    private final HandEvaluator handEvaluator;

    @Autowired
    public HandEvaluationServiceImpl(HandEvaluator handEvaluator) {
        this.handEvaluator = handEvaluator;
    }


    @Override
    public HandRank evaluateHand(List<Card> playerCards, List<Card> communityCards) {
        return null;
    }

    @Override
    public Map<Player, Double> determineWinners(GameRound gameRound) {
        return Map.of();
    }
}