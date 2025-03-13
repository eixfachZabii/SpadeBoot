// src/main/java/com/pokerapp/api/dto/response/UserDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;

//@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Double balance;
    private String role;
    private Long currentTableId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getCurrentTableId() {
        return currentTableId;
    }

    public void setCurrentTableId(Long currentTableId) {
        this.currentTableId = currentTableId;
    }
}
