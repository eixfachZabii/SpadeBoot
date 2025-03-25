package com.pokerapp.config;

import com.pokerapp.domain.user.User;
import com.pokerapp.repository.UserRepository;
import com.pokerapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) {
        // Check if admin user exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            // Create admin user
            User adminUser = new User();
            adminUser.setUsername("Hoerter");
            adminUser.setEmail("Hoerter@pokerapp.com");
            adminUser.setPassword(passwordEncoder.encode("admin123")); // Change in production!
            adminUser.setRole("ROLE_ADMIN");
            adminUser.setBalance(50000); // Give admin a higher starting balance

            User user = userRepository.save(adminUser);
            userService.createPlayer(user.getId());

            System.out.println("Admin user created with username: Hoerter and password: admin123");
        }
    }
}