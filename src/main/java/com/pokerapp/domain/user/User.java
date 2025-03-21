package com.pokerapp.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email;

    private Integer balance = 1000; // Default starting balance

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] avatar;

    @Column(name = "role")
    private String role = "ROLE_USER"; // Default role

    // Method to check if user has admin role
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(this.role);
    }

    // Method to promote to admin
    public void promoteToAdmin() {
        this.role = "ROLE_ADMIN";
    }

    // Method to demote to user
    public void demoteToUser() {
        this.role = "ROLE_USER";
    }
}