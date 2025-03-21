package com.pokerapp.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Integer balance;
    private byte[] avatar;
    private Boolean isAdmin;
}