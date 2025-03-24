package com.pokerapp.domain.game;

import com.pokerapp.domain.user.Player;

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

    @Transient
    private Stage currentStage;


    public Round() {
    }

    public Round(Player[] players) {
    }


}
