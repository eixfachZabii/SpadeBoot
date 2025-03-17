// src/main/java/com/pokerapp/domain/user/User.java
package com.pokerapp.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

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

    private Double balance = 1000.0; // Default starting balance

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] avatar;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles")
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    public User() {
    }

    public void addRole(String role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }
}