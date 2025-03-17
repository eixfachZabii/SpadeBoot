package com.pokerapp.service;

import com.pokerapp.api.dto.response.ReplayDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.Move;
import com.pokerapp.domain.replay.Replay;

import java.util.List;

public interface ReplayService {
    /**
     * Creates a new replay for a game if one doesn't exist
     * @param game The game to create a replay for
     * @return The created replay
     */
    Replay createReplay(Game game);

    /**
     * Gets an existing replay or creates a new one if none exists
     * @param game The game to get/create a replay for
     * @return The existing or newly created replay
     */
    Replay getOrCreateReplay(Game game);

    /**
     * Records a move in the game's replay
     * @param game The game where the move was made
     * @param move The move to record
     */
    void recordMove(Game game, Move move);

    /**
     * Gets a replay by ID
     * @param replayId The ID of the replay
     * @return The replay data
     */
    ReplayDto getReplay(Long replayId);

    /**
     * Gets all replays for a user
     * @param userId The ID of the user
     * @return List of replays
     */
    List<ReplayDto> getReplaysByUser(Long userId);

    /**
     * Gets all replays for a table
     * @param tableId The ID of the table
     * @return List of replays
     */
    List<ReplayDto> getReplaysByTable(Long tableId);

    /**
     * Marks a replay as complete
     * @param gameId The ID of the game
     */
    void completeReplay(Long gameId);
}