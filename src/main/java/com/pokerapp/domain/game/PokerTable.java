// src/main/java/com/pokerapp/domain/game/Table.java
package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import com.pokerapp.domain.user.Spectator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "poker_tables")
public class PokerTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private Integer maxPlayers;

    private Double minBuyIn;

    private Double maxBuyIn;

    private Boolean isPrivate = false;

    @ManyToOne
    private Player owner;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "poker_tables_players",
            joinColumns = @JoinColumn(name = "poker_table_id"),
            inverseJoinColumns = @JoinColumn(name = "players_id")
    )
    private Set<Player> players = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "table_spectators",
            joinColumns = @JoinColumn(name = "table_id"),
            inverseJoinColumns = @JoinColumn(name = "spectator_id")
    )
    private Set<Spectator> spectators = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    private Game currentGame;

    public Game startNewGame() {
        Game game = new Game();
        game.setPokerTable(this);
        game.setSmallBlind(minBuyIn / 100);
        game.setBigBlind(minBuyIn / 50);

        currentGame = game;
        currentGame.start();

        return currentGame;
    }

    public boolean addPlayer(Player player, Double buyIn) {

        if(players.stream().anyMatch(p -> p.getUserId().equals(player.getUserId()))) {
            return true;
        }

        if (players.size() >= maxPlayers) {
            return false;
        }

        if (buyIn < minBuyIn || buyIn > maxBuyIn) {
            return false;
        }

        if (buyIn > player.getBalance()) {
            return false;
        }

        player.setChips(buyIn);
        player.setBalance(player.getBalance() - buyIn);
        player.setCurrentTableId(this.id);
        player.setStatus(PlayerStatus.ACTIVE);
        players.add(player);

        return true;
    }

    public boolean addSpectator(Spectator spectator) {
        spectator.setWatchingTableId(this.id);
        return spectators.add(spectator);
    }

    public boolean removeSpectator(Spectator spectator) {
        spectator.setWatchingTableId(null);
        return spectators.remove(spectator);
    }

    public boolean removePlayer(Player player) {
        if (!players.contains(player)) {
            return false;
        }

        Double remainingChips = player.getChips();
        player.leaveTable(remainingChips);
        return players.remove(player);
    }

}
