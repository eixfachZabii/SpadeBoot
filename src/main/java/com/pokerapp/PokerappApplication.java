// src/main/java/com/pokerapp/PokerApplication.java
package com.pokerapp;

import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.domain.user.User;
import com.pokerapp.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;


@SpringBootApplication(scanBasePackages = "com.pokerapp")
@EnableJpaRepositories(basePackages = "com.pokerapp.repository")
@EntityScan(basePackages = "com.pokerapp.domain")
public class PokerappApplication {

    @Autowired
    private UserServiceImpl userService;


    public static void main(String[] args) {
        SpringApplication.run(PokerappApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void execCodeAfterStartup() {
//        User Markus = new User();
//        Markus.setUsername("ALFONSKEMPERISTTOLL5");
//        Markus.setPassword("ALFONSKEMPERISTTOLL5");
//        Markus.setEmail("ALFONSKEMPERISTTOLL5@gmail.com");
//        Markus.addRole("User");

        RegisterDto Hubsi = new RegisterDto();

        Hubsi.setUsername("THOMAS_NEUMANN");
        Hubsi.setPassword("ICHBINGDBBOSS");
        Hubsi.setEmail("THOMAS.NEUMANN@TUM.de");



        userService.register(Hubsi);

        System.out.println("successfully registered");
    }

}