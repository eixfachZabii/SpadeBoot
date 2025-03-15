// src/main/java/com/pokerapp/domain/invitation/Invitation.java
package com.pokerapp.domain.invitation;

import com.pokerapp.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "invitations")
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User sender;

    @ManyToOne
    private User recipient;

    @Column(name = "table_id")
    private Long tableId;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status = InvitationStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

    @Column(length = 500)
    private String message;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

}