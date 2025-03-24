package com.pokerapp.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.util.Pair;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.HandEvaluation;
import com.pokerapp.domain.game.Round;
import com.pokerapp.domain.game.Stage;
import com.pokerapp.domain.game.StageType;
import com.pokerapp.domain.game.Turn;
import com.pokerapp.domain.user.Player;

public class RoundSession extends Thread {
    private GameSession gameSession;
    private Game game;
    private List<Player> players;
    private final int smalBlind; 
    private final int bigBlind; 
    private Map<Integer, Player> playerSeat;
    private int smalBlindPos;
    private Map<Player, Pair<Card, Card>> playerHands;
    
    public RoundSession(GameSession gameSession, Game game, List<Player> players, Map<Integer, Player> playerSeat, int smalBlindPos) {
        this.smalBlind = 0;
        this.bigBlind = 0;
        this.gameSession = gameSession; 
        this.players = players;
        this.playerSeat = playerSeat;
        this.smalBlindPos = smalBlindPos;
        this.game = game;
    }   

    @Override
    public void run() {
        
        round();
        gameSession.setRoundRunning(false);
    }

    public Round round() {
        Round round = new Round();
        round.setGame(game);
        playerSeat.get(smalBlindPos).pay(smalBlind);
        playerSeat.get(smalBlindPos+1).pay(bigBlind);

        //TODO: CARDSCAN!
        //Playercardmap ist gesetzt ab hier (oder sollte)

        List<Card> communityCards = new ArrayList<>();
        round.setPre_flop(stage(round, StageType.PRE_FLOP));
        round.setFlop(stage(round, StageType.FLOP));
        communityCards.addAll(round.getFlop().getNewCards());
        round.setTurn(stage(round, StageType.TURN));
        communityCards.addAll(round.getTurn().getNewCards());
        round.setRiver(stage(round, StageType.RIVER));
        communityCards.addAll(round.getRiver().getNewCards());
        
        round.setCommunityCards(communityCards);
        round.setPlayerHands(playerHands);

        Map<Player, Long> handRankNumbers = new HashMap<>();
        for (Player player: players) {
            List<Card> totalCards = new ArrayList<>();
            totalCards.add(playerHands.get(player).getFirst());
            totalCards.add(playerHands.get(player).getSecond());
            totalCards.addAll(communityCards);
            handRankNumbers.put(player, HandEvaluation.cardsToRankNumber(totalCards));
        }

        List<Player> winner = new ArrayList<>();
        for (Player player : handRankNumbers.keySet()) {
            if (winner.isEmpty()) {
                winner.add(player);
                break;
            }
            if (handRankNumbers.get(winner.get(0)) == handRankNumbers.get(player)) {
                winner.add(player);
                break;
            }
            if (handRankNumbers.get(winner.get(0)) < handRankNumbers.get(player)) {
                winner.clear();
                winner.add(player);
            }
        }
        round.setWinner(winner);
        //TODO: Save round
        return round;
    }

    public Stage stage(Round round, StageType type) {
        Stage stage = new Stage();
        stage.setRound(round);
        stage.setType(type);
        stage.setPlayerCount(players.size());
        
        stage.setNewCards(null); //TODO: New Cards Scann from Top Camera

        List<Turn> turns = new ArrayList();
        int currentBet = bigBlind;
        for (Player player: players) {
            Turn turn = turn(stage, player, currentBet);
            currentBet = turn.getEndingBet();
            turns.add(turn);
        }
        stage.setTurns(turns);
        //TODO: Save stage
        return stage;
    }

    public Turn turn(Stage stage, Player player, int startingBet) {
        Turn turn = new Turn();
        turn.setStage(stage);
        turn.setPlayer(player);
        turn.setStartingBet(startingBet);

        //TODO: PLAYER TURN COMM

        turn.setEndingBet(startingBet); //TODO: Add proper ending bet setter 
        //TODO: Save turn
        return turn;
    }
}