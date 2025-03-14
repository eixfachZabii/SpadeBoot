// src/main/java/com/pokerapp/domain/user/User.java
package com.pokerapp.domain.user;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

//@Data
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email;

    private Double balance = 1000.0; // Default starting balance


    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    //@Column(length = 16777215)
    private byte[] avatar;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles")
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.REGULAR;

    public User() {}

    public void addRole(String role) {
        roles.add(role);
    }
}