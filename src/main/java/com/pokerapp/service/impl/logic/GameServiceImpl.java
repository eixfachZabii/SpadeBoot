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

import java.util.Map;

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
        game.setDeck(deck);
        game.setManualMode(true); //TODO

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
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        if (game.getStatus() == GameStatus.WAITING) {
            // Update game status
            game.setStatus(GameStatus.IN_PROGRESS);
            gameRepository.save(game);

            // Start the first round
            GameRound gameRound = gameRoundService.createGameRound(gameId);
            gameRoundService.startGameRound(gameRound.getId());

            // Broadcast the game state update to clients
            //broadcastGameStateUpdate(game);
        } else {
            throw new IllegalStateException("Game must be in WAITING state to start");
        }
    }

    // Add this new method to advance to the next round
    @Transactional
    public void startNextRound(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game must be in IN_PROGRESS state to start next round");
        }

        // Check if we should end the game (e.g., only one player left with chips)
        if (shouldEndGame(game)) {
            endGame(gameId);
            return;
        }

        // Rotate dealer position for the next round
        game.rotateDealerPosition();
        gameRepository.save(game);

        // Create and start a new round
        GameRound gameRound = gameRoundService.createGameRound(gameId);
        gameRoundService.startGameRound(gameRound.getId());

        // Broadcast the updated game state
        //broadcastGameStateUpdate(game);
    }

    private boolean shouldEndGame(Game game) {
        // Logic to determine if the game should end
        // For example, only one player left with chips
        long playersWithChips = game.getPlayers().stream()
                .filter(player -> player.getChips() > 0)
                .count();

        return playersWithChips <= 1;
    }

    private void broadcastGameStateUpdate(Game game) {
        GameStateDto gameState = getGameState(game.getId());
        messagingTemplate.convertAndSend("/topic/games/" + game.getId(), gameState);
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

    @Override
    @Transactional
    public void setGameMode(Long gameId, boolean manualMode) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        game.setManualMode(manualMode);
        gameRepository.save(game);

        // Broadcast game mode change to clients
        messagingTemplate.convertAndSend("/topic/games/" + gameId + "/mode",
                Map.of("manualMode", manualMode));
    }
}