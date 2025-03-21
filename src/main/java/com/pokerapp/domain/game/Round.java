package com.pokerapp.domain.game;

import java.util.ArrayList;
import java.util.List;

import com.pokerapp.domain.user.Player;

import jakarta.persistence.*;

@Entity
@Table(name = "rounds")
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private Stage pre_flop;
    @OneToOne(cascade = CascadeType.ALL)
    private Stage flop;
    @OneToOne(cascade = CascadeType.ALL)
    private Stage turn;
    @OneToOne(cascade = CascadeType.ALL)
    private Stage river;
    @ManyToOne
    private Game game;

    private Stage currentStage;

    public Round(Player[] players) {
        pre_flop = new Stage(players);
        

    }

    public Round() {

    }


    private Stage getNextStage() {
        if (currentStage.equals(pre_flop)) {
            currentStage = flop;
            return currentStage;
        } else if (currentStage.equals(flop)) {
            currentStage = turn;
            return currentStage;
        } else if (currentStage.equals(turn)) {
            currentStage = river;
            return currentStage;
        } else if (currentStage.equals(river)) {
            return null; // Todo: missing error handling
        } else {
            return null; // Todo: missing error handling
        }
    }
}
