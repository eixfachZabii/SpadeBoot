package com.pokerapp.domain.game;

import java.util.ArrayList;
import java.util.List;

import com.pokerapp.domain.user.Player;

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
    @OneToMany(mappedBy = "Game", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Round> gameRounds = new ArrayList<>();

    private Player[] allPlayers;
    private Player[] currentPlayers;

    public void playRound() {
        Round newRound = new Round(currentPlayers);
    }
}
