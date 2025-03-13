// src/main/java/com/pokerapp/repository/GameResultRepository.java
package com.pokerapp.repository;

import com.pokerapp.domain.statistics.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameResultRepository extends JpaRepository<GameResult, Long> {
    List<GameResult> findByGameId(Long gameId);
    List<GameResult> findByWinningsPlayerIdOrderByTimestampDesc(Long playerId);
}

