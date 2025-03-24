package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "turns")
public class Turn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Stage stage;

    private int startingBet;

    private int endingBet;

    public Turn(Player player, int cB) {
        startingBet = cB;
        player.setStatus(PlayerStatus.ACTIVE);
    }

    public Turn() {

    }
}
