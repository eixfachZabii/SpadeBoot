package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stages")
public class Turn {

    private int startingBet;
    private int endingBet;

    public Turn(Player player, int cB) {
        startingBet = cB;
        
    }
}
