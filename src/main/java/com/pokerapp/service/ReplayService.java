// src/main/java/com/pokerapp/service/ReplayService.java
package com.pokerapp.service;

import com.pokerapp.api.dto.response.ReplayDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.replay.Replay;

import java.util.List;

public interface ReplayService {
    Replay createReplay(Game game);
    ReplayDto getReplay(Long replayId);
    List<ReplayDto> getReplaysByUser(Long userId);
    List<ReplayDto> getReplaysByTable(Long tableId);
    void completeReplay(Long gameId);
}