// src/main/java/com/pokerapp/domain/card/Deck.java
package com.pokerapp.domain.card;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

//@Data
@Entity
@Table(name = "decks")
public class Deck {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Card> cards = new ArrayList<>();

    @PostLoad
    public void initialize() {
        if (cards.isEmpty()) {
            for (Suit suit : Suit.values()) {
                for (c_Rank cRank : c_Rank.values()) {
                    Card card = new Card();
                    card.setSuit(suit);
                    card.setRank(cRank);
                    card.setShowing(false);
                    cards.add(card);
                }
            }
            shuffle();
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty");
        }
        return cards.remove(0);
    }
}
