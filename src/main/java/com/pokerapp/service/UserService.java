// src/main/java/com/pokerapp/service/UserService.java
package com.pokerapp.service;

import com.pokerapp.api.dto.request.LoginDto;
import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.domain.user.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    User register(RegisterDto registerDto);
    String authenticate(LoginDto loginDto);
    User getCurrentUser();
    User getUserById(Long id);
    User updateBalance(Long userId, Double amount);
}
