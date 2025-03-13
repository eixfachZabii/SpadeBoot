// src/main/java/com/pokerapp/repository/SpectatorRepository.java
package com.pokerapp.repository;

import com.pokerapp.domain.user.Spectator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpectatorRepository extends JpaRepository<Spectator, Long> {
    List<Spectator> findByWatchingTableId(Long tableId);
}
