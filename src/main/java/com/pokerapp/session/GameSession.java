package com.pokerapp.session;

import java.util.List;

import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.user.Player;


public class GameSession extends Thread {

    private int bigBlind;
    private RoundSession currentRound;
    private List<Player> currentPlayers;
    private boolean roundRunning;
   
    @Override
    public void run() {
        Game game = new Game();
        startRound(game);
        while (roundRunning) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO: missing error Handling
                e.printStackTrace();
            }
            
        }
        startRound(game);
    }
    
    public void startGame(int bigBlind) {
        this.bigBlind = bigBlind;
        this.start();
    }   

    public void startRound(Game game) {
        if (currentRound != null) {
            try {
                currentRound.join();
            } catch (InterruptedException e) {
                // TODO: missing error Handling
                e.printStackTrace();
            }
        }
        currentRound = new RoundSession(this, game, currentPlayers, null, 0);
        currentRound.start();
        roundRunning = true;
    }

    public void setRoundRunning(boolean state) {
        roundRunning = state;
    }

    
}