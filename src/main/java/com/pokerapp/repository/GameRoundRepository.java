// src/main/java/com/pokerapp/repository/GameRepository.java
package com.pokerapp.repository;

import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.GameRound;
import com.pokerapp.domain.game.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GameRoundRepository extends JpaRepository<GameRound, Long> {
    List<Game> findByStatus(GameStatus status);

    @Query("SELECT g from GameRound g WHERE g.game.id = ?1")
    List<Game> findByGameId(Long gameId);
}
