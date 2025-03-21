package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stages")
public class Stage {
    @OneToOne
    private Round round;

    @OneToMany(mappedBy = "Game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Turn[] turns;

    private int playerCount;

    private int currentBet;

    public Stage(Player[] players) {
        playerCount = players.length;
        turns = new Turn[playerCount];
        for (int i = 0; i < playerCount; i++) {
            turns[i] = new Turn(players[i], currentBet);
        }
    }

    public void raiseBy(int value) {
        currentBet += value;
    }

    public void raiseTo(int value) {
        if (currentBet >= value) {
            //Todo: Missing error handling
        }
        currentBet = value;
    }
}
