// src/main/java/com/pokerapp/domain/game/Table.java
package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.Spectator;
import com.pokerapp.domain.user.PlayerStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//@Data
@Entity
//@Table(name = "poker_tables")
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

    @OneToMany
    private List<Player> players = new ArrayList<>();

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
        game.start();

        return game;
    }

    public boolean addPlayer(Player player, Double buyIn) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Double getMinBuyIn() {
        return minBuyIn;
    }

    public void setMinBuyIn(Double minBuyIn) {
        this.minBuyIn = minBuyIn;
    }

    public Double getMaxBuyIn() {
        return maxBuyIn;
    }

    public void setMaxBuyIn(Double maxBuyIn) {
        this.maxBuyIn = maxBuyIn;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Set<Spectator> getSpectators() {
        return spectators;
    }

    public void setSpectators(Set<Spectator> spectators) {
        this.spectators = spectators;
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }
}
