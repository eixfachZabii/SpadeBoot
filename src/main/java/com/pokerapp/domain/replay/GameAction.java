// src/main/java/com/pokerapp/domain/replay/GameAction.java
package com.pokerapp.domain.replay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pokerapp.domain.game.Move;
import com.pokerapp.domain.user.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "game_actions")
public class GameAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Player player;

    private String actionType;

    private String actionData;

    private LocalDateTime timestamp = LocalDateTime.now();

    private Integer sequenceNumber;

    @ManyToOne
    @JsonIgnore
    private Replay replay;

    public static GameAction fromMove(Move move, Integer sequenceNumber) {
        GameAction action = new GameAction();
        action.setPlayer(move.getPlayer());
        action.setActionType("MOVE");
        action.setActionData(move.getType() + ":" + move.getAmount());
        action.setSequenceNumber(sequenceNumber);
        return action;
    }
}
