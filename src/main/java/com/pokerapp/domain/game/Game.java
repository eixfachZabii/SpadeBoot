package com.pokerapp.domain.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.pokerapp.domain.card.Deck;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Round> gameRounds = new ArrayList<>();

    private Integer smallBlind;

    private Integer bigBlind;

    private Integer dealerIndex = 0;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private PokerTable pokerTable;
}
