package com.pokerapp.service.impl.logic;

import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.domain.card.Deck;
import com.pokerapp.domain.game.*;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.GameRepository;
import com.pokerapp.repository.GameRoundRepository;
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

@Service
public class GameRoundService {


    private final GameRepository gameRepository;
    private final TableRepository tableRepository;
    private final PlayerRepository playerRepository;
    private final ReplayService replayService;
    private final StatisticsService statisticsService;
    private final SimpMessagingTemplate messagingTemplate;
    private final HandEvaluationService handEvaluationService;
    private final GameRoundRepository gameRoundRepository;
    private final BettingRoundService bettingRoundService;

    @Autowired
    public GameRoundService(
            GameRepository gameRepository,
            TableRepository tableRepository,
            PlayerRepository playerRepository,
            ReplayService replayService,
            StatisticsService statisticsService,
            SimpMessagingTemplate messagingTemplate,
            HandEvaluationService handEvaluationService, GameRoundRepository gameRoundRepository, BettingRoundService bettingRoundService) {
        this.gameRepository = gameRepository;
        this.tableRepository = tableRepository;
        this.playerRepository = playerRepository;
        this.replayService = replayService;
        this.statisticsService = statisticsService;
        this.messagingTemplate = messagingTemplate;
        this.handEvaluationService = handEvaluationService;
        this.gameRoundRepository = gameRoundRepository;
        this.bettingRoundService = bettingRoundService;
    }

    @Transactional
    public GameRound createGameRound(Long gameId) {
        // Find the poker table
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));


        // Create a new round
        GameRound gameRound = new GameRound();
        game.getGameRounds().add(gameRound);
        gameRound.setRoundNumber(game.getGameRounds().size());
        gameRound.setPot(0.0);

        return gameRoundRepository.save(gameRound);
    }


    @Transactional
    public void startGameRound(Long gameRoundId) {
        GameRound gameRound = gameRoundRepository.findById(gameRoundId).orElseThrow(() -> new NotFoundException("GameRound not found with ID: " + gameRoundId));

        BettingRound preFlop = bettingRoundService.createBettingRound(gameRoundId, BettingStage.PREFLOP);
        bettingRoundService.playBettingRound(preFlop.getId());

        BettingRound flop = bettingRoundService.createBettingRound(gameRoundId, BettingStage.PREFLOP);
        bettingRoundService.playBettingRound(flop.getId());

        BettingRound turn = bettingRoundService.createBettingRound(gameRoundId, BettingStage.PREFLOP);
        bettingRoundService.playBettingRound(turn.getId());

        BettingRound river = bettingRoundService.createBettingRound(gameRoundId, BettingStage.PREFLOP);
        bettingRoundService.playBettingRound(river.getId());

    }
}

