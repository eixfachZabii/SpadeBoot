// src/main/java/com/pokerapp/domain/game/Game.java
package com.pokerapp.domain.game;

import com.pokerapp.domain.card.Deck;
import com.pokerapp.domain.card.Hand;
import com.pokerapp.domain.user.Player;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Table;

//@Data
@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double smallBlind;

    private Double bigBlind;

    private Integer dealerPosition = 0;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.WAITING;

    @OneToOne(cascade = CascadeType.ALL)
    private PokerTable pokerTable;

    @OneToOne(cascade = CascadeType.ALL)
    private Deck deck = new Deck();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<GameRound> gameRounds = new ArrayList<>();

    @OneToOne
    private GameRound currentRound;

    public Game(){
    }

    public void start() {
        if (status != GameStatus.WAITING) {
            throw new IllegalStateException("Game already started");
        }

        status = GameStatus.STARTING;
        deck.initialize();
        deck.shuffle();

        // Deal cards to players
        dealPlayerCards();

        // Create first round
        GameRound firstRound = new GameRound();
        firstRound.setRoundNumber(1);
        firstRound.setGame(this);
        gameRounds.add(firstRound);
        currentRound = firstRound;

        // Start with preflop betting
        currentRound.advanceToNextBettingRound();

        status = GameStatus.IN_PROGRESS;
    }

    private void dealPlayerCards() {
        for (Player player : pokerTable.getPlayers()) {
            if (player.getHand() == null) {
                player.setHand(new Hand());
            } else {
                player.getHand().clear();
            }

            // Each player gets 2 cards
            player.getHand().addCard(deck.drawCard());
            player.getHand().addCard(deck.drawCard());
        }
    }

    public List<Player> determineWinner() {
        // Implement winner determination logic using HandEvaluator
        return new ArrayList<>(); // Placeholder
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getSmallBlind() {
        return smallBlind;
    }

    public void setSmallBlind(Double smallBlind) {
        this.smallBlind = smallBlind;
    }

    public Double getBigBlind() {
        return bigBlind;
    }

    public void setBigBlind(Double bigBlind) {
        this.bigBlind = bigBlind;
    }

    public Integer getDealerPosition() {
        return dealerPosition;
    }

    public void setDealerPosition(Integer dealerPosition) {
        this.dealerPosition = dealerPosition;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public PokerTable getPokerTable() {
        return pokerTable;
    }

    public void setPokerTable(PokerTable pokerTable) {
        this.pokerTable = pokerTable;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public List<GameRound> getGameRounds() {
        return gameRounds;
    }

    public void setGameRounds(List<GameRound> gameRounds) {
        this.gameRounds = gameRounds;
    }

    public GameRound getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(GameRound currentRound) {
        this.currentRound = currentRound;
    }
}