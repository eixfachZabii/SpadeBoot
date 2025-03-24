package com.pokerapp.domain.game;

import java.util.List;
import java.util.Map;
import org.springframework.data.util.Pair;

import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.card.Card;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rounds")
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private Stage pre_flop;
    @OneToOne(cascade = CascadeType.ALL)
    private Stage flop;
    @OneToOne(cascade = CascadeType.ALL)
    private Stage turn;
    @OneToOne(cascade = CascadeType.ALL)
    private Stage river;
    @ManyToOne
    private Game game;
    @ManyToOne
    private List<Player> winner;
    @ManyToMany 
    private List<Player> players;

    private Map<Player, Pair<Card, Card>> playerHands;

    private List<Card> communityCards;

    private int playerCount;
}
