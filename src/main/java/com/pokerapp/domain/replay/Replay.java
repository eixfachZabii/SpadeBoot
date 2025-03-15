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