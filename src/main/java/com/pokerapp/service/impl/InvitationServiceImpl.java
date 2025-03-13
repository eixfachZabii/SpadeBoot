// src/main/java/com/pokerapp/service/impl/InvitationServiceImpl.java
package com.pokerapp.service.impl;

import com.pokerapp.api.dto.request.InvitationRequestDto;
import com.pokerapp.api.dto.response.InvitationDto;
import com.pokerapp.domain.invitation.Invitation;
import com.pokerapp.domain.invitation.InvitationStatus;
import com.pokerapp.domain.user.User;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.InvitationRepository;
import com.pokerapp.repository.UserRepository;
import com.pokerapp.service.InvitationService;
import com.pokerapp.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvitationServiceImpl implements InvitationService {

    @Autowired
    private final InvitationRepository invitationRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final TableService tableService;

    @Autowired
    public InvitationServiceImpl(
            InvitationRepository invitationRepository,
            UserRepository userRepository,
            TableService tableService) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.tableService = tableService;
    }

    @Override
    //@Transactional
    public Invitation createInvitation(InvitationRequestDto requestDto, User sender) {
        User recipient = userRepository.findById(requestDto.getRecipientId())
                .orElseThrow(() -> new NotFoundException("Recipient not found"));

        // Verify the table exists
        tableService.getTableById(requestDto.getTableId());

        Invitation invitation = new Invitation();
        invitation.setSender(sender);
        invitation.setRecipient(recipient);
        invitation.setTableId(requestDto.getTableId());
        invitation.setMessage(requestDto.getMessage());

        return invitationRepository.save(invitation);
    }

    @Override
    public List<InvitationDto> getPendingInvitationsForUser(User user) {
        return invitationRepository.findByRecipientAndStatus(user, InvitationStatus.PENDING)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvitationDto> getSentInvitationsForUser(User user) {
        return invitationRepository.findBySenderAndStatus(user, InvitationStatus.PENDING)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    //@Transactional
    public InvitationDto acceptInvitation(Long invitationId, User user) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (!invitation.getRecipient().getId().equals(user.getId())) {
            throw new IllegalStateException("This invitation is not for you");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is not pending");
        }

        if (invitation.isExpired()) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            return convertToDto(invitationRepository.save(invitation));
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        return convertToDto(invitationRepository.save(invitation));
    }

    @Override
    //@Transactional
    public InvitationDto declineInvitation(Long invitationId, User user) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (!invitation.getRecipient().getId().equals(user.getId())) {
            throw new IllegalStateException("This invitation is not for you");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is not pending");
        }

        invitation.setStatus(InvitationStatus.DECLINED);
        return convertToDto(invitationRepository.save(invitation));
    }

    private InvitationDto convertToDto(Invitation invitation) {
        InvitationDto dto = new InvitationDto();
        dto.setId(invitation.getId());
        dto.setSenderId(invitation.getSender().getId());
        dto.setSenderName(invitation.getSender().getUsername());
        dto.setRecipientId(invitation.getRecipient().getId());
        dto.setRecipientName(invitation.getRecipient().getUsername());
        dto.setTableId(invitation.getTableId());
        dto.setStatus(invitation.getStatus().toString());
        dto.setMessage(invitation.getMessage());
        dto.setCreatedAt(invitation.getCreatedAt().toString());
        dto.setExpiresAt(invitation.getExpiresAt().toString());
        return dto;
    }
}
