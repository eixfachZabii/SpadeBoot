// src/main/java/com/pokerapp/domain/user/Player.java
package com.pokerapp.domain.user;

import com.pokerapp.domain.game.Move;
import com.pokerapp.domain.card.Hand;
import jakarta.persistence.*;
import lombok.Data;


//@Data
@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double chips = 0.0;

    @Enumerated(EnumType.STRING)
    private PlayerStatus status = PlayerStatus.SITTING_OUT;

    @OneToOne(cascade = CascadeType.ALL)
    private Hand hand;

    @Column(name = "current_table_id")
    private Long currentTableId;

    // Methods that delegate to user
    public String getUsername() {
        return user.getUsername();
    }
    
    public String getEmail() {
        return user.getEmail();
    }
    
    public Double getBalance() {
        return user.getBalance();
    }
    
    public void setBalance(Double balance) {
        user.setBalance(balance);
    }
    
    public Long getUserId() {
        return user.getId();
    }

    // Player-specific methods
    public void makeMove(Move move) {
        // Logic for processing a player's move
    }

    public void rebuy(Double amount) {
        if (amount <= getBalance()) {
            this.chips += amount;
            setBalance(getBalance() - amount);
        } else {
            throw new IllegalArgumentException("Insufficient balance for rebuy");
        }
    }

    public void leaveTable(Double remainingChips) {
        setBalance(getBalance() + remainingChips);
        this.chips = 0.0;
        this.currentTableId = null;
        this.status = PlayerStatus.SITTING_OUT;
    }

    public Player() {
    }

    public User getUser() {
        return user;
    }   

    public void setUser(User user) {
        this.user = user;
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