package com.pokerapp.service.impl;

import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.PokerTable;
import com.pokerapp.domain.game.GameStatus;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.exception.InvalidMoveException;
import com.pokerapp.repository.*;
import com.pokerapp.service.GameService;
import com.pokerapp.service.ReplayService;
import com.pokerapp.service.StatisticsService;
import com.pokerapp.service.UserService;
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
    private final UserService userService;

    @Autowired
    public GameServiceImpl(
            GameRepository gameRepository,
            TableRepository tableRepository,
            PlayerRepository playerRepository,
            ReplayService replayService,
            StatisticsService statisticsService,
            SimpMessagingTemplate messagingTemplate,
            UserService userService) {
        this.gameRepository = gameRepository;
        this.tableRepository = tableRepository;
        this.playerRepository = playerRepository;
        this.replayService = replayService;
        this.statisticsService = statisticsService;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
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
        // - Set initial dealer position
        // - Initialize game parameters (blinds, etc.)

        // Save the game
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
    public Game startGame(Long gameId) {
        // Find the game
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        // Basic validation
        if (game.getStatus() != GameStatus.WAITING) {
            throw new IllegalStateException("Game is not in WAITING state");
        }

        // TODO: Implement game start logic
        // - Shuffle and deal cards to players
        // - Collect blinds
        // - Set initial betting round
        // - Determine first player to act

        // Mark game as in progress
        game.setStatus(GameStatus.IN_PROGRESS);
        Game savedGame = gameRepository.save(game);

        // TODO: Broadcast game start event to players

        return savedGame;
    }

    @Override
    @Transactional
    public GameStateDto getGameState(Long gameId) {
        // Find the game
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        // Create a basic game state DTO
        GameStateDto gameStateDto = new GameStateDto();
        gameStateDto.setGameId(game.getId());
        gameStateDto.setStatus(game.getStatus().toString());

        // TODO: Populate game state details
        // - Add current player
        // - Add community cards
        // - Add pot size
        // - Add player positions and statuses
        // - Add possible actions for current player

        return gameStateDto;
    }

    @Override
    @Transactional
    public GameStateDto makeMove(Long gameId, Long userId, MoveDto moveDto) {
        // Find the game
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        // Find player by user ID
        Player player = playerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Player not found for user ID: " + userId));

        // Basic validation
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }

        // TODO: Implement comprehensive move validation
        // - Check if it's the player's turn
        // - Validate move type (check, call, raise, fold, all-in)
        // - Verify player has enough chips
        // - Check move against current betting round rules

        // TODO: Process the move
        // - Update player chips
        // - Update pot
        // - Update betting round
        // - Handle special cases (all-in, etc.)

        // TODO: Check for betting round completion
        // - Determine if round is complete
        // - If complete, advance to next stage (flop, turn, river)
        // - Check for game end conditions

        // Placeholder for basic move validation
        if (moveDto == null || moveDto.getType() == null) {
            throw new InvalidMoveException("Invalid move");
        }

        // Save the game
        gameRepository.save(game);

        // TODO: Broadcast move to other players

        // Get updated game state
        return getGameState(gameId);
    }

    @Override
    @Transactional
    public Game endGame(Long gameId) {
        // Find the game
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        // If game is already finished, just return it
        if (game.getStatus() == GameStatus.FINISHED) {
            return game;
        }

        // TODO: Implement winner determination
        // - Evaluate player hands
        // - Determine winner(s)
        // - Distribute pot

        // TODO: Record game statistics
        // - Update player statistics
        // - Record game result

        // Mark game as finished
        game.setStatus(GameStatus.FINISHED);
        Game savedGame = gameRepository.save(game);

        // Complete the replay
        replayService.completeReplay(game.getId());

        // TODO: Broadcast game end event to players

        return savedGame;
    }
}