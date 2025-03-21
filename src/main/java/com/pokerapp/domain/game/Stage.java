package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "stages")
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Round round;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Turn> turns = new ArrayList<>();

    private int playerCount;

    private int currentBet;

    public Stage() {
    }

    public Stage(Player[] players) {
        playerCount = players.length;
        for (int i = 0; i < playerCount; i++) {
            //turns[i] = new Turn(players[i], currentBet);
            //TODO: Arraylist statt [] :)
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
