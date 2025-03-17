package com.pokerapp.service.impl;

import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.response.CardDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.api.dto.response.PlayerStateDto;
import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Deck;
import com.pokerapp.domain.card.Hand;
import com.pokerapp.domain.game.BettingRound;
import com.pokerapp.domain.game.BettingStage;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.GameRound;
import com.pokerapp.domain.game.GameStatus;
import com.pokerapp.domain.game.Move;
import com.pokerapp.domain.game.MoveType;
import com.pokerapp.domain.game.PokerTable;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import com.pokerapp.exception.InvalidMoveException;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.GameRepository;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.repository.TableRepository;
import com.pokerapp.service.GameService;
import com.pokerapp.service.HandEvaluationService;
import com.pokerapp.service.ReplayService;
import com.pokerapp.service.StatisticsService;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final TableRepository tableRepository;
    private final PlayerRepository playerRepository;
    private final ReplayService replayService;
    private final StatisticsService statisticsService;
    private final SimpMessagingTemplate messagingTemplate;
    private final HandEvaluationService handEvaluationService;

    @Autowired
    public GameServiceImpl(
            GameRepository gameRepository,
            TableRepository tableRepository,
            PlayerRepository playerRepository,
            ReplayService replayService,
            StatisticsService statisticsService,
            SimpMessagingTemplate messagingTemplate,
            HandEvaluationService handEvaluationService) {
        this.gameRepository = gameRepository;
        this.tableRepository = tableRepository;
        this.playerRepository = playerRepository;
        this.replayService = replayService;
        this.statisticsService = statisticsService;
        this.messagingTemplate = messagingTemplate;
        this.handEvaluationService = handEvaluationService;
    }


    @Override
    public Game createGame(Long tableId) {
        return null;
    }

    @Override
    public Game startGame(Long gameId) {
        return null;
    }

    @Override
    public GameStateDto getGameState(Long gameId) {
        return null;
    }

    @Override
    public GameStateDto makeMove(Long gameId, Long playerId, MoveDto moveDto) {
        return null;
    }

    @Override
    public Game endGame(Long gameId) {
        return null;
    }
}