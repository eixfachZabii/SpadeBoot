// src/main/java/com/pokerapp/api/controller/UserController.java
package com.pokerapp.api.controller;

import com.pokerapp.api.dto.request.LoginDto;
import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.api.dto.response.UserDto;
import com.pokerapp.domain.user.User;
import com.pokerapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterDto registerDto) {
        User user = userService.register(registerDto);
        UserDto userDto = convertToDto(user);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginDto loginDto) {
        String token = userService.authenticate(loginDto);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        User user = userService.getCurrentUser();
        UserDto userDto = convertToDto(user);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserDto userDto = convertToDto(user);
        return ResponseEntity.ok(userDto);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        // Map user fields to DTO
        return dto;
    }
}
