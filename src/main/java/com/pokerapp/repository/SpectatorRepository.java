package com.pokerapp.repository;

import com.pokerapp.domain.user.Spectator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpectatorRepository extends JpaRepository<Spectator, Long> {
    List<Spectator> findByWatchingTableId(Long tableId);

    @Query("SELECT s FROM Spectator s WHERE s.user.id = :userId")
    Optional<Spectator> findByUserId(@Param("userId") Long userId);
}