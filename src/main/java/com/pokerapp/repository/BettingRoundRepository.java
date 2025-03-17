// src/main/java/com/pokerapp/repository/GameRepository.java
package com.pokerapp.repository;

import com.pokerapp.domain.game.BettingRound;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.GameRound;
import com.pokerapp.domain.game.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BettingRoundRepository extends JpaRepository<BettingRound, Long> {
    List<Game> findByStatus(GameStatus status);

    @Query("SELECT b from BettingRound b WHERE b.gameRound.id = ?1")
    GameRound findByGameId(Long gameRoundId);
}
