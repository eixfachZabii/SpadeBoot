package com.pokerapp.session;

import java.util.List;

import com.pokerapp.domain.game.Round;
import com.pokerapp.domain.user.Player;

import ch.qos.logback.core.util.Duration;

public class GameSession extends Thread {

    private int bigBlind;
    private RoundSession currentRound;
    private List<Player> currentPlayers;
    private boolean roundRunning;
   
    @Override
    public void run() {
        startRound();
        while (roundRunning) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO: missing error Handling
                e.printStackTrace();
            }
            
        }
        startRound();
    }
    
    public void startGame(int bigBlind) {
        this.bigBlind = bigBlind;
        this.start();
    }   

    public void startRound() {
        if (currentRound != null) {
            try {
                currentRound.join();
            } catch (InterruptedException e) {
                // TODO: missing error Handling
                e.printStackTrace();
            }
        }
        currentRound = new RoundSession();
        currentRound.startRound(this, currentPlayers);
        roundRunning = true;
    }

    public void setRoundRunning(boolean state) {
        roundRunning = state;
    }
}