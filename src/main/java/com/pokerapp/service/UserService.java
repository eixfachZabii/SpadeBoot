package com.pokerapp.service;

import com.pokerapp.api.dto.request.LoginDto;
import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.domain.user.User;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public User register(RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create the base User entity
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setBalance(1000.0); // Default starting balance
        user.addRole("USER");

        return userRepository.save(user);
    }

    @Override
    public String authenticate(LoginDto loginDto) {
        // Authentication logic
        //TODO
        return "";
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    @Transactional
    public User updateBalance(Long userId, Double amount) {
        User user = getUserById(userId);
        user.setBalance(user.getBalance() + amount);
        return userRepository.save(user);
    }
}