// src/main/java/com/pokerapp/domain/game/Move.java
package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "moves")
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MoveType type;

    private Double amount;

    @ManyToOne
    private Player player;

    @ManyToOne
    private BettingRound bettingRound;

    private Long timestamp = System.currentTimeMillis();
}
