package com.pokerapp.api.dto.response;

import com.pokerapp.domain.user.PlayerStatus;
import com.pokerapp.domain.user.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerDto {
    private Long id;
    private User user;
    private Integer chips;
    private PlayerStatus status;
    private Long currentTableId;
    private Double winProbability;
    private Integer totalBet;
}
