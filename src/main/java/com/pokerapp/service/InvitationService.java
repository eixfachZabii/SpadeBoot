// src/main/java/com/pokerapp/service/InvitationService.java
package com.pokerapp.service;

import com.pokerapp.api.dto.request.InvitationRequestDto;
import com.pokerapp.api.dto.response.InvitationDto;
import com.pokerapp.domain.invitation.Invitation;
import com.pokerapp.domain.user.User;

import java.util.List;

public interface InvitationService {
    Invitation createInvitation(InvitationRequestDto requestDto, User sender);
    List<InvitationDto> getPendingInvitationsForUser(User user);
    List<InvitationDto> getSentInvitationsForUser(User user);
    InvitationDto acceptInvitation(Long invitationId, User user);
    InvitationDto declineInvitation(Long invitationId, User user);
}
