package com.pokerapp.api.dto.response;

import com.pokerapp.domain.card.Card;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class GameStateDto {
    private Long gameId;
    private Long tableId;
    private String gameStage;
    private int pot;
    private int currentBet;
    private int smallBlind;
    private int bigBlind;
    private List<Card> communityCards;
    private Map<Long, Map<String, Object>> players;
    private int dealerPosition;
    private int smallBlindPosition;
    private int bigBlindPosition;
    private Long currentPlayerId;
    private long lastUpdateTime = System.currentTimeMillis();
}