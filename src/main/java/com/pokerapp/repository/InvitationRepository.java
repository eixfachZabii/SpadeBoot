// src/main/java/com/pokerapp/repository/InvitationRepository.java
package com.pokerapp.repository;

import com.pokerapp.domain.invitation.Invitation;
import com.pokerapp.domain.invitation.InvitationStatus;
import com.pokerapp.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findByRecipientAndStatus(User recipient, InvitationStatus status);
    List<Invitation> findBySenderAndStatus(User sender, InvitationStatus status);
}