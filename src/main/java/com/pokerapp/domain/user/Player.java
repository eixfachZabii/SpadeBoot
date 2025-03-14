// src/main/java/com/pokerapp/domain/user/Player.java
package com.pokerapp.domain.user;

import com.pokerapp.domain.game.Move;
import com.pokerapp.domain.card.Hand;
import jakarta.persistence.*;
import lombok.Data;


//@Data
@Entity
@Table(name = "players")
public class Player extends User {
    private Double chips = 0.0;

    @Enumerated(EnumType.STRING)
    private PlayerStatus status = PlayerStatus.SITTING_OUT;

    @OneToOne(cascade = CascadeType.ALL)
    private Hand hand;

    @Column(name = "current_table_id")
    private Long currentTableId;

    public Player() {
        super();
        this.setUserType(UserType.PLAYER);
    }

    public void makeMove(Move move) {
        // Logic for processing a player's move
    }

    public void rebuy(Double amount) {
        if (amount <= this.getBalance()) {
            this.setChips(this.getChips() + amount);
            this.setBalance(this.getBalance() - amount);
        } else {
            throw new IllegalArgumentException("Insufficient balance for rebuy");
        }
    }

    public void leaveTable(Double remainingChips) {
        // Return chips to balance
        this.setBalance(this.getBalance() + remainingChips);
        this.setChips(0.0);
        this.setCurrentTableId(null);
        this.setStatus(PlayerStatus.SITTING_OUT);
    }

    public Double getChips() {
        return chips;
    }

    public void setChips(Double chips) {
        this.chips = chips;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public Long getCurrentTableId() {
        return currentTableId;
    }

    public void setCurrentTableId(Long currentTableId) {
        this.currentTableId = currentTableId;
    }
}