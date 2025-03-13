// src/main/java/com/pokerapp/repository/TableRepository.java
package com.pokerapp.repository;

import com.pokerapp.domain.game.PokerTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableRepository extends JpaRepository<PokerTable, Long> {
    List<PokerTable> findByNameContainingIgnoreCase(String name);
}
