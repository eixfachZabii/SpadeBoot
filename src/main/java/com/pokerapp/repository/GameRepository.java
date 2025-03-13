// src/main/java/com/pokerapp/repository/GameRepository.java
package com.pokerapp.repository;

import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByStatus(GameStatus status);
    List<Game> findByTableId(Long tableId);
}
