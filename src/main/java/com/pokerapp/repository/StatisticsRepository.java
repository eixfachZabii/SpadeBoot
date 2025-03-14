// src/main/java/com/pokerapp/repository/StatisticsRepository.java
package com.pokerapp.repository;

import com.pokerapp.domain.statistics.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

    @Query("SELECT s FROM Statistics s WHERE s.player.id  = ?1")
    Optional<Statistics> findByPlayerId(Long playerId);
}
