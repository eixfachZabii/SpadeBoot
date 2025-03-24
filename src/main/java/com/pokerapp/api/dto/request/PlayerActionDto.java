package com.pokerapp.api.dto.request;

import com.pokerapp.domain.game.session.GameSession;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerActionDto {
    private GameSession.PlayerAction action;
    private Integer amount;
}