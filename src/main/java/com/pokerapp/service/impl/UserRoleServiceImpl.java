package com.pokerapp.service.impl;

import com.pokerapp.domain.card.Hand;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import com.pokerapp.domain.user.Spectator;
import com.pokerapp.domain.user.User;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.repository.SpectatorRepository;
import com.pokerapp.repository.UserRepository;
import com.pokerapp.service.UserRoleService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private SpectatorRepository spectatorRepository;

    @Override
    @Transactional
    public Player convertToPlayer(User user) {
        // First check if player already exists for this user
        Optional<Player> existingPlayer = findPlayerByUserId(user.getId());
        if (existingPlayer.isPresent()) {
            return existingPlayer.get();
        }

        // Create a new Player linked to this user
        Player player = new Player();
        player.setUser(user);
        player.setChips(0.0);
        player.setStatus(PlayerStatus.SITTING_OUT);
        player.setHand(new Hand());

        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public Spectator convertToSpectator(User user) {
        // First check if spectator already exists for this user
        Optional<Spectator> existingSpectator = findSpectatorByUserId(user.getId());
        if (existingSpectator.isPresent()) {
            return existingSpectator.get();
        }

        // Create a new Spectator linked to this user
        Spectator spectator = new Spectator();
        spectator.setUser(user);

        return spectatorRepository.save(spectator);
    }

    @Override
    public User getUserWithRoles(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    }

    // Helper methods to find Player/Spectator by User ID
    private Optional<Player> findPlayerByUserId(Long userId) {
        return playerRepository.findAll().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst();
    }

    private Optional<Spectator> findSpectatorByUserId(Long userId) {
        return spectatorRepository.findAll().stream()
                .filter(s -> s.getUser().getId().equals(userId))
                .findFirst();
    }
}