// src/main/java/com/pokerapp/config/ApplicationConfig.java
package com.pokerapp.config;

import com.pokerapp.domain.poker.HandEvaluator;
import com.pokerapp.domain.poker.WinnerDeterminer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ApplicationConfig {

    @Bean
    public HandEvaluator handEvaluator() {
        return new HandEvaluator();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }
}
