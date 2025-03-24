package com.pokerapp.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageDto {
    private String type;
    private String content;
    private Long playerId;
    private String playerName;
    private String timestamp;
}