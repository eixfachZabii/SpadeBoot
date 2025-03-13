// src/main/java/com/pokerapp/repository/PlayerRepository.java
package com.pokerapp.repository;

import com.pokerapp.domain.user.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
