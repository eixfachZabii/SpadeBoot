package com.pokerapp.service.impl;

import com.pokerapp.api.dto.request.TableSettingsDto;
import com.pokerapp.api.dto.response.TableDto;
import com.pokerapp.domain.game.PokerTable;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.Spectator;
import com.pokerapp.domain.user.User;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.repository.SpectatorRepository;
import com.pokerapp.repository.TableRepository;
import com.pokerapp.repository.UserRepository;
import com.pokerapp.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableServiceImpl implements TableService {

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private SpectatorRepository spectatorRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public PokerTable createTable(TableSettingsDto settings, User owner) {
        // Find the player associated with this user
        Player ownerAsPlayer = playerRepository.findByUserId(owner.getId())
                .orElseThrow(() -> new NotFoundException("Player not found for user: " + owner.getUsername()));

        PokerTable pokerTable = new PokerTable();
        pokerTable.setName(settings.getName());
        pokerTable.setDescription(settings.getDescription());
        pokerTable.setMaxPlayers(settings.getMaxPlayers());
        pokerTable.setMinBuyIn(settings.getMinBuyIn());
        pokerTable.setMaxBuyIn(settings.getMaxBuyIn());
        pokerTable.setPrivate(settings.getPrivate());
        pokerTable.setOwner(ownerAsPlayer);

        return tableRepository.save(pokerTable);
    }

    @Override
    public PokerTable getTableById(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Table not found"));
    }

    @Override
    public List<TableDto> getAllTables() {
        return tableRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TableDto> getPublicTables() {
        return tableRepository.findAll().stream()
                .filter(table -> !table.getPrivate())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TableDto joinTable(Long tableId, Long userId, Double buyIn) {
        PokerTable pokerTable = getTableById(tableId);

        // Find player by user ID, not by player ID
        Player player = playerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Player not found for user ID: " + userId));

        if (player.getCurrentTableId() != null) {
            throw new IllegalStateException("Player is already at a table");
        }

        boolean joined = pokerTable.addPlayer(player, buyIn);
        if (!joined) {
            throw new IllegalStateException("Could not join table");
        }

        playerRepository.save(player);
        pokerTable = tableRepository.save(pokerTable);

        return convertToDto(pokerTable);
    }

    @Override
    @Transactional
    public TableDto joinTableAsSpectator(Long tableId, Long userId) {
        PokerTable pokerTable = getTableById(tableId);

        // Find spectator by user ID or create one if it doesn't exist
        Spectator spectator = spectatorRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

                    Spectator newSpectator = new Spectator();
                    newSpectator.setUser(user);
                    return spectatorRepository.save(newSpectator);
                });

        if (spectator.getWatchingTableId() != null) {
            throw new IllegalStateException("Spectator is already watching a table");
        }

        boolean added = pokerTable.addSpectator(spectator);
        if (!added) {
            throw new IllegalStateException("Could not join as spectator");
        }

        spectatorRepository.save(spectator);
        pokerTable = tableRepository.save(pokerTable);

        return convertToDto(pokerTable);
    }

    @Override
    @Transactional
    public TableDto leaveTable(Long tableId, Long userId) {
        PokerTable pokerTable = getTableById(tableId);

        // Find player by user ID, not by player ID
        Player player = playerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Player not found for user ID: " + userId));

        boolean removed = pokerTable.removePlayer(player);
        if (!removed) {
            throw new IllegalStateException("Player not at this table");
        }

        playerRepository.save(player);
        pokerTable = tableRepository.save(pokerTable);

        return convertToDto(pokerTable);
    }

    @Override
    @Transactional
    public TableDto removeSpectator(Long tableId, Long userId) {
        PokerTable pokerTable = getTableById(tableId);

        // Find spectator by user ID, not by spectator ID
        Spectator spectator = spectatorRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Spectator not found for user ID: " + userId));

        boolean removed = pokerTable.removeSpectator(spectator);
        if (!removed) {
            throw new IllegalStateException("Spectator not watching this table");
        }

        spectatorRepository.save(spectator);
        pokerTable = tableRepository.save(pokerTable);

        return convertToDto(pokerTable);
    }

    private TableDto convertToDto(PokerTable pokerTable) {
        TableDto dto = new TableDto();
        dto.setId(pokerTable.getId());
        dto.setName(pokerTable.getName());
        dto.setDescription(pokerTable.getDescription());
        dto.setMaxPlayers(pokerTable.getMaxPlayers());
        dto.setCurrentPlayers(pokerTable.getPlayers().size());
        dto.setMinBuyIn(pokerTable.getMinBuyIn());
        dto.setMaxBuyIn(pokerTable.getMaxBuyIn());
        dto.setPrivate(pokerTable.getPrivate());
        dto.setOwnerId(pokerTable.getOwner().getUserId());
        dto.setHasActiveGame(pokerTable.getCurrentGame() != null);
        return dto;
    }
}