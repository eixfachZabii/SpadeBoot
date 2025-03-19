package com.pokerapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = "com.pokerapp")
@EnableJpaRepositories(basePackages = "com.pokerapp.repository")
@EntityScan(basePackages = "com.pokerapp.domain")
public class PokerappApplication {

    private static final Logger logger = Logger.getLogger(PokerappApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(PokerappApplication.class, args);
    }

}