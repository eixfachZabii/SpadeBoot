// src/main/java/com/pokerapp/api/dto/response/UserDto.java
package com.pokerapp.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Double balance;
    private String role;
    private Long currentTableId;
}
