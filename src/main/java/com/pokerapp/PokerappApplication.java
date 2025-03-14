// src/main/java/com/pokerapp/PokerApplication.java
package com.pokerapp;

import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.domain.user.User;
import com.pokerapp.service.impl.UserServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages = "com.pokerapp")
@EnableJpaRepositories(basePackages = "com.pokerapp.repository")
@EntityScan(basePackages = "com.pokerapp.domain")
public class PokerappApplication {

//    @Autowired
//    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(PokerappApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void execCodeAfterStartup() {
        User Markus = new User();
        Markus.setUsername("Markus");
        Markus.setPassword("Markus");
        Markus.setEmail("Markus@gmail.com");
        Markus.addRole("User");

        UserServiceImpl userService = new UserServiceImpl();
        RegisterDto Hubsi = new RegisterDto();

        Hubsi.setUsername("Hubsi");
        Hubsi.setPassword("Hubsi");
        Hubsi.setEmail("Hubsi@gmail.com");


        userService.register(Hubsi);
    }

}