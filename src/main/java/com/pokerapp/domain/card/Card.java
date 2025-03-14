// src/main/java/com/pokerapp/domain/card/Card.java
package com.pokerapp.domain.card;

import jakarta.persistence.*;


//@Data
@Entity
//@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    public c_Rank getRank() {
        return cRank;
    }

    public void setRank(c_Rank cRank) {
        this.cRank = cRank;
    }

    @Enumerated(EnumType.STRING)
    private Suit suit;

    @Enumerated(EnumType.STRING)
    private c_Rank cRank;

    private boolean isShowing;

    @Override
    public String toString() {
        return cRank + " of " + suit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setShowing(boolean showing) {
        isShowing = showing;
    }
}
