// src/main/java/com/pokerapp/domain/card/Hand.java
package com.pokerapp.domain.card;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

//@Data
@Entity
//@Table(name = "hands")
public class Hand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Card> cards = new ArrayList<>();

    public void addCard(Card card) {
        cards.add(card);
    }

    public void clear() {
        cards.clear();
    }

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
}
