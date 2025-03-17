package com.pokerapp.service.impl.logic;

import com.pokerapp.domain.game.BettingRound;
import com.pokerapp.domain.game.BettingStage;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.GameRound;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.*;
import com.pokerapp.service.HandEvaluationService;
import com.pokerapp.service.ReplayService;
import com.pokerapp.service.StatisticsService;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BettingRoundService {


    private final GameRepository gameRepository;
    private final TableRepository tableRepository;
    private final PlayerRepository playerRepository;
    private final ReplayService replayService;
    private final StatisticsService statisticsService;
    private final SimpMessagingTemplate messagingTemplate;
    private final HandEvaluationService handEvaluationService;
    private final GameRoundRepository gameRoundRepository;
    private final BettingRoundRepository bettingRoundRepository;

    @Autowired
    public BettingRoundService(
            GameRepository gameRepository,
            TableRepository tableRepository,
            PlayerRepository playerRepository,
            ReplayService replayService,
            StatisticsService statisticsService,
            SimpMessagingTemplate messagingTemplate,
            HandEvaluationService handEvaluationService, GameRoundRepository gameRoundRepository, BettingRoundRepository bettingRoundRepository) {
        this.gameRepository = gameRepository;
        this.tableRepository = tableRepository;
        this.playerRepository = playerRepository;
        this.replayService = replayService;
        this.statisticsService = statisticsService;
        this.messagingTemplate = messagingTemplate;
        this.handEvaluationService = handEvaluationService;
        this.gameRoundRepository = gameRoundRepository;
        this.bettingRoundRepository = bettingRoundRepository;
    }

    @Transactional
    public BettingRound createBettingRound(Long gameRoundId, BettingStage bettingStage) {
        // Find the GameRound
        GameRound gameRound = gameRoundRepository.findById(gameRoundId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameRoundId));


        // Create a new round
        BettingRound bettingRound = new BettingRound();
        gameRound.getBettingRounds().add(bettingRound);
        bettingRound.setStage(bettingStage);
        bettingRound.setCurrentBet(0.0);

        bettingRound = bettingRoundRepository.save(bettingRound);


        return bettingRound;
    }

    @Transactional
    public BettingRound playBettingRound(Long bettingRoundId) {
        BettingRound bettingRound = bettingRoundRepository.findById(bettingRoundId).orElseThrow(() -> new NotFoundException("GameRound not found with ID: " + bettingRoundId));

        GameRound gameRound = bettingRound.getGameRound();
        Set<Player> players = bettingRound.getPlayers();
        Set<Player> activePlayers = players.stream().filter(p -> p.getStatus().equals(PlayerStatus.ACTIVE)).collect(Collectors.toSet());
        Set<Player> foldedPlayers = players.stream().filter(p -> p.getStatus().equals(PlayerStatus.FOLDED)).collect(Collectors.toSet());
        double pot = bettingRound.getPot();
        double currentBet = bettingRound.getCurrentBet();
        double dealerIndex = bettingRound.getDealerIndex();
        double smallBlindIndex = (dealerIndex + 1) % activePlayers.size();
        double bigBlindIndex = (dealerIndex + 2) % activePlayers.size();






        return null;
    }
}