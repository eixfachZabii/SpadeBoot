// src/main/java/com/pokerapp/domain/card/Deck.java
package com.pokerapp.domain.card;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "decks")
public class Deck {

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
                    card.setCRank(cRank);
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
