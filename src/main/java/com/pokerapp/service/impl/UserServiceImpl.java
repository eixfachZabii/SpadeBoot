// src/main/java/com/pokerapp/service/impl/UserServiceImpl.java
package com.pokerapp.service.impl;

import com.pokerapp.api.dto.request.LoginDto;
import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import com.pokerapp.domain.user.UserType;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.UserRepository;
//import com.pokerapp.security.JwtTokenProvider;
import com.pokerapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService
{


    @Autowired
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    //private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager
        //    ,JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
   //     this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public User register(RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setBalance(100.0); // Default starting balance
        user.setUserType(UserType.REGULAR);
        user.addRole("USER");

        return userRepository.save(user);
    }

    @Override
    public String authenticate(LoginDto loginDto) {
        return "";
    }

//    @Override
//    public String authenticate(LoginDto loginDto) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        return jwtTokenProvider.generateToken(authentication);
//    }

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
