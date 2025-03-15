package com.pokerapp.service;

import com.pokerapp.api.dto.request.TableSettingsDto;
import com.pokerapp.api.dto.response.TableDto;
import com.pokerapp.domain.game.PokerTable;
import com.pokerapp.domain.user.User;

import java.util.List;

/**
 * Service interface for managing poker tables
 */
public interface TableService {
    /**
     * Creates a new poker table
     * @param settings Table settings
     * @param owner Table owner
     * @return TableDto representation of the created table
     */
    TableDto createTable(TableSettingsDto settings, User owner);

    /**
     * Gets a table by ID
     * @param id Table ID
     * @return TableDto representation of the table
     */
    TableDto getTableById(Long id);

    /**
     * Gets the raw PokerTable entity by ID (for internal use)
     * @param id Table ID
     * @return PokerTable entity
     */
    PokerTable getTableEntityById(Long id);

    /**
     * Gets all tables
     * @return List of all tables
     */
    List<TableDto> getAllTables();

    /**
     * Gets all public tables
     * @return List of public tables
     */
    List<TableDto> getPublicTables();

    /**
     * Joins a table as a player
     * @param tableId Table ID
     * @param userId User ID
     * @param buyIn Buy-in amount
     * @return Updated table
     */
    TableDto joinTable(Long tableId, Long userId, Double buyIn);

    /**
     * Joins a table as a spectator
     * @param tableId Table ID
     * @param userId User ID
     * @return Updated table
     */
    TableDto joinTableAsSpectator(Long tableId, Long userId);

    /**
     * Leaves a table as a player
     * @param tableId Table ID
     * @param userId User ID
     * @return Updated table
     */
    TableDto leaveTable(Long tableId, Long userId);

    /**
     * Removes a spectator from a table
     * @param tableId Table ID
     * @param userId User ID
     * @return Updated table
     */
    TableDto removeSpectator(Long tableId, Long userId);
}