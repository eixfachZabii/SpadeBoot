// src/main/java/com/pokerapp/repository/ReplayRepository.java
package com.pokerapp.repository;

import com.pokerapp.domain.replay.Replay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReplayRepository extends JpaRepository<Replay, Long> {
    Optional<Replay> findByGameId(Long gameId);

    @Query("SELECT g from Game g WHERE g.pokerTable.id = ?1")
    List<Replay> findByGameTableId(Long tableId);
}
