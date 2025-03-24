package com.pokerapp.api.websocket;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GameEvent {
    private Type type;
    private String message;
    private Long playerId;
    private long timestamp;

    public enum Type {
        GAME_STARTED,
        PLAYER_JOINED,
        PLAYER_LEFT,
        PLAYER_ACTION,
        PLAYER_TIMEOUT,
        NEW_ROUND,
        ROUND_RESULT,
        GAME_ENDED,
        SYSTEM_MESSAGE
    }

    public GameEvent(Type type, String message, Long playerId) {
        this.type = type;
        this.message = message;
        this.playerId = playerId;
        this.timestamp = System.currentTimeMillis();
    }
}