// src/main/java/com/pokerapp/repository/GameRepository.java
package com.pokerapp.repository;

import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByStatus(GameStatus status);

    @Query("SELECT g from Game g WHERE g.pokerTable.id = ?1")
    List<Game> findByTableId(Long tableId);
}
