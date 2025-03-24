package com.pokerapp.session;

import java.util.ArrayList;
import java.util.List;

import com.pokerapp.domain.game.Stage;
import com.pokerapp.domain.game.StageType;
import com.pokerapp.domain.game.Turn;
import com.pokerapp.domain.user.Player;

public class RoundSession extends Thread {
    GameSession gameSession;
    List<Player> players;
    
    public void startRound(GameSession gameSession, List<Player> players) {
        this.gameSession = gameSession; 
        this.players = players;
    }   

    @Override
    public void run() {
        stage(StageType.PRE_FLOP);
        stage(StageType.FLOP);
        stage(StageType.TURN);
        stage(StageType.RIVER);

        //Ende:
        gameSession.setRoundRunning(false);
    }

    public Stage stage(StageType type) {
        Stage stage = new Stage();
        stage.setType(type);
        List<Turn> turns = new ArrayList();
        for (Player player: players) {
            turns.add(turn(player));
        }
        
        return stage;
    }

    public Turn turn(Player player) {

    }

    /*    private Stage getNextStage() {
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
    } */
}
    

