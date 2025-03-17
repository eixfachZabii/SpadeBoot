package com.pokerapp.service.impl;

import com.pokerapp.api.dto.response.GameActionDto;
import com.pokerapp.api.dto.response.ReplayDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.Move;
import com.pokerapp.domain.replay.GameAction;
import com.pokerapp.domain.replay.Replay;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.ReplayRepository;
import com.pokerapp.service.ReplayService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReplayServiceImpl implements ReplayService {

    private final ReplayRepository replayRepository;

    @Autowired
    public ReplayServiceImpl(ReplayRepository replayRepository) {
        this.replayRepository = replayRepository;
    }

    @Override
    @Transactional
    public Replay createReplay(Game game) {
        if (game == null || game.getId() == null) {
            throw new IllegalArgumentException("Game must be saved before creating a replay");
        }

        // Check if a replay already exists for this game
        Optional<Replay> existingReplay = replayRepository.findByGameId(game.getId());
        if (existingReplay.isPresent()) {
            return existingReplay.get();
        }

        // Create a new replay only if one doesn't exist
        Replay replay = new Replay();
        replay.setGame(game);
        return replayRepository.save(replay);
    }

    @Override
    @Transactional
    public Replay getOrCreateReplay(Game game) {
        if (game == null || game.getId() == null) {
            throw new IllegalArgumentException("Game must be saved before accessing replay");
        }

        return replayRepository.findByGameId(game.getId())
                .orElseGet(() -> {
                    Replay newReplay = new Replay();
                    newReplay.setGame(game);
                    return replayRepository.save(newReplay);
                });
    }

    @Override
    @Transactional
    public void recordMove(Game game, Move move) {
        if (game == null || game.getId() == null || move == null) {
            return; // Skip recording if not valid
        }

        try {
            Replay replay = getOrCreateReplay(game);

            GameAction action = GameAction.fromMove(move, replay.getActionCounter() + 1);
            action.setReplay(replay);
            replay.getActions().add(action);
            replay.setActionCounter(replay.getActionCounter() + 1);

            replayRepository.save(replay);
        } catch (Exception e) {
            // Log but don't fail the operation
            System.err.println("Error recording move: " + e.getMessage());
        }
    }

    @Override
    public ReplayDto getReplay(Long replayId) {
        Replay replay = replayRepository.findById(replayId)
                .orElseThrow(() -> new NotFoundException("Replay not found"));

        return convertToDto(replay);
    }

    @Override
    public List<ReplayDto> getReplaysByUser(Long userId) {
        // Implementation depends on how you store the relationship between replays and users
        return replayRepository.findAll().stream()
                .filter(r -> r.getGame().getPokerTable().getPlayers().stream()
                        .anyMatch(p -> p.getUserId().equals(userId)))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReplayDto> getReplaysByTable(Long tableId) {
        return replayRepository.findByGameTableId(tableId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void completeReplay(Long gameId) {
        Replay replay = replayRepository.findByGameId(gameId)
                .orElseThrow(() -> new NotFoundException("Replay not found for game"));

        replay.completeReplay();
        replayRepository.save(replay);
    }

    private ReplayDto convertToDto(Replay replay) {
        ReplayDto dto = new ReplayDto();
        dto.setId(replay.getId());
        dto.setGameId(replay.getGame().getId());
        dto.setTableId(replay.getGame().getPokerTable().getId());
        dto.setStartTime(replay.getStartTime().toString());

        if (replay.getEndTime() != null) {
            dto.setEndTime(replay.getEndTime().toString());
        }

        List<GameActionDto> actions = replay.getActions().stream()
                .map(action -> {
                    GameActionDto actionDto = new GameActionDto();
                    actionDto.setId(action.getId());
                    actionDto.setPlayerId(action.getPlayer().getUserId());
                    actionDto.setPlayerName(action.getPlayer().getUsername());
                    actionDto.setActionType(action.getActionType());
                    actionDto.setActionData(action.getActionData());
                    actionDto.setTimestamp(action.getTimestamp().toString());
                    actionDto.setSequenceNumber(action.getSequenceNumber());
                    return actionDto;
                }).collect(Collectors.toList());

        dto.setActions(actions);
        return dto;
    }
}