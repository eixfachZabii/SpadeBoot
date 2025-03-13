// src/main/java/com/pokerapp/service/impl/ReplayServiceImpl.java
package com.pokerapp.service.impl;

import com.pokerapp.api.dto.response.GameActionDto;
import com.pokerapp.api.dto.response.ReplayDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.replay.Replay;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.ReplayRepository;
import com.pokerapp.service.ReplayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReplayServiceImpl implements ReplayService {

    @Autowired
    private final ReplayRepository replayRepository;

    @Autowired
    public ReplayServiceImpl(ReplayRepository replayRepository) {
        this.replayRepository = replayRepository;
    }

    @Override
    @Transactional
    public Replay createReplay(Game game) {
        Replay replay = new Replay();
        replay.setGame(game);
        return replayRepository.save(replay);
    }

    @Override
    public ReplayDto getReplay(Long replayId) {
        Replay replay = replayRepository.findById(replayId)
                .orElseThrow(() -> new NotFoundException("Replay not found"));

        return convertToDto(replay);
    }

    @Override
    public List<ReplayDto> getReplaysByUser(Long userId) {
        // Implementation would depend on how you store the relationship between replays and users
        // This is a simplified example
        return replayRepository.findAll().stream()
                .filter(r -> r.getGame().getPokerTable().getPlayers().stream()
                        .anyMatch(p -> p.getId().equals(userId)))
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
                    actionDto.setPlayerId(action.getPlayer().getId());
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
