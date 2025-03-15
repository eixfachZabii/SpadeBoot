// src/main/java/com/pokerapp/api/dto/request/RegisterDto.java
package com.pokerapp.api.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class RegisterDto {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 120)
    private String password;

    @NotBlank
    @Email
    private String email;

    public @NotBlank @Size(min = 3, max = 50) String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank @Size(min = 3, max = 50) String username) {
        this.username = username;
    }

    public @NotBlank @Size(min = 6, max = 120) String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank @Size(min = 6, max = 120) String password) {
        this.password = password;
    }

    public @NotBlank @Email String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank @Email String email) {
        this.email = email;
    }
}
