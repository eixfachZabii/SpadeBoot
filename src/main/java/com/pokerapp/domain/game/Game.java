package com.pokerapp.domain.game;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "games")
public class Game {
<<<<<<< HEAD
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Round> gameRounds = new ArrayList<>();
}
=======
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
>>>>>>> 917801751a1325376902092164e4dd9eca0de677
