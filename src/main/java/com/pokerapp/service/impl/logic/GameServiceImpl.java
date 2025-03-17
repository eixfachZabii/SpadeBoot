package com.pokerapp.service.impl.logic;

import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.domain.card.Deck;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.GameRound;
import com.pokerapp.domain.game.GameStatus;
import com.pokerapp.domain.game.PokerTable;
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
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final TableRepository tableRepository;
    private final PlayerRepository playerRepository;
    private final ReplayService replayService;
    private final StatisticsService statisticsService;
    private final SimpMessagingTemplate messagingTemplate;
    private final HandEvaluationService handEvaluationService;
    private final GameRoundRepository gameRoundRepository;

    private final GameRoundService gameRoundService;

    @Autowired
    public GameServiceImpl(
            GameRepository gameRepository,
            TableRepository tableRepository,
            PlayerRepository playerRepository,
            ReplayService replayService,
            StatisticsService statisticsService,
            SimpMessagingTemplate messagingTemplate,
            HandEvaluationService handEvaluationService, GameRoundRepository gameRoundRepository, GameRoundService gameRoundService) {
        this.gameRepository = gameRepository;
        this.tableRepository = tableRepository;
        this.playerRepository = playerRepository;
        this.replayService = replayService;
        this.statisticsService = statisticsService;
        this.messagingTemplate = messagingTemplate;
        this.handEvaluationService = handEvaluationService;
        this.gameRoundRepository = gameRoundRepository;
        this.gameRoundService = gameRoundService;
    }


    @Override
    @Transactional
    public Game createGame(Long tableId) {
        // Find the poker table
        PokerTable pokerTable = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Table not found with ID: " + tableId));

        // Basic validation
        if (pokerTable.getPlayers().size() < 2) {
            throw new IllegalStateException("Cannot create a game with fewer than 2 players");
        }

        // Create a new game
        Game game = new Game();
        game.setPokerTable(pokerTable);
        game.setStatus(GameStatus.WAITING);

        // TODO: Implement initial game setup
        game.setDealerIndex(0);
        game.setSmallBlind(pokerTable.getMaxBuyIn() / 200);
        game.setBigBlind(pokerTable.getMaxBuyIn() / 100);

        Deck deck = new Deck();
        deck.initialize();
        game.setDeck(deck);


        game = gameRepository.save(game);

        // Update the table's current game
        pokerTable.setCurrentGame(game);
        tableRepository.save(pokerTable);

        // Create replay
        replayService.createReplay(game);

        // TODO: Broadcast game creation event to players
        return game;
    }

    @Override
    @Transactional
    public void startGame(Long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));
        game.setStatus(GameStatus.IN_PROGRESS);
        boolean success = true;

        while(game.getStatus() == GameStatus.IN_PROGRESS && success) {
            GameRound gameRound = gameRoundService.createGameRound(gameId);
            gameRoundService.startGameRound(gameRound.getId());
        }
    }


    @Override
    public GameStateDto getGameState(Long gameId) {
        return null;
    }

    @Override
    public Game endGame(Long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));
        game.setStatus(GameStatus.FINISHED);
        return game;
    }
}