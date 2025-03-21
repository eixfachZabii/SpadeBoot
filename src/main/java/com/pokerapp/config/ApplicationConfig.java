package com.pokerapp.config;


import com.pokerapp.domain.card.CardHelper;
import com.pokerapp.domain.game.HandEvaluation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public CardHelper cardHelper() {
        return new CardHelper();
    }

    @Bean
    public HandEvaluation handEvaluation() {
        return new HandEvaluation();
    }

}