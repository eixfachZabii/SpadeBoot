// src/main/java/com/pokerapp/domain/replay/Replay.java
package com.pokerapp.domain.replay;

import com.pokerapp.domain.game.Game;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "replays")
public class Replay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getActionCounter() {
        return actionCounter;
    }

    public void setActionCounter(Integer actionCounter) {
        this.actionCounter = actionCounter;
    }

    public List<GameAction> getActions() {
        return actions;
    }

    public void setActions(List<GameAction> actions) {
        this.actions = actions;
    }

    @OneToOne
    private Game game;

    private LocalDateTime startTime = LocalDateTime.now();

    private LocalDateTime endTime;

    private Integer actionCounter = 0;

    @OneToMany(mappedBy = "replay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sequenceNumber ASC")
    private List<GameAction> actions = new ArrayList<>();

    public void recordAction(GameAction action) {
        action.setReplay(this);
        action.setSequenceNumber(++actionCounter);
        actions.add(action);
    }

    public void completeReplay() {
        this.endTime = LocalDateTime.now();
    }
}