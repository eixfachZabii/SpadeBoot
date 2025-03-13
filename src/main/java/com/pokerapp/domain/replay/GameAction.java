// src/main/java/com/pokerapp/domain/replay/GameAction.java
package com.pokerapp.domain.replay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pokerapp.domain.game.Move;
import com.pokerapp.domain.user.Player;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

//@Data
@Entity
//@Table(name = "game_actions")
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Replay getReplay() {
        return replay;
    }

    public void setReplay(Replay replay) {
        this.replay = replay;
    }
}
