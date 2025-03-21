package com.pokerapp.domain.game;

import com.pokerapp.domain.card.Deck;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer smallBlind;

    private Integer bigBlind;

    private Integer dealerIndex = 0;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private PokerTable pokerTable;

    @OneToOne(cascade = CascadeType.ALL)
    private Deck deck = new Deck();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<GameRound> gameRounds = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private GameRound currentRound;


    /**
     * Rotates the dealer position for the next hand
     */
    public void rotateDealerPosition() {
        List<Player> activePlayers = new ArrayList<>();
        for (Player player : pokerTable.getPlayers()) {
            if (player.getStatus() == PlayerStatus.ACTIVE) {
                activePlayers.add(player);
            }
            if (player.getStatus() == PlayerStatus.SITTING_OUT) {
                activePlayers.remove(player);
            }
        }

        if (!activePlayers.isEmpty()) {
            dealerIndex = (dealerIndex + 1) % activePlayers.size();
        }
    }

    public Set<Player> getPlayers() {
        return pokerTable != null ? pokerTable.getPlayers() : Collections.emptySet();
    }
}