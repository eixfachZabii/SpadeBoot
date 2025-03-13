// src/main/java/com/pokerapp/service/TableService.java
package com.pokerapp.service;

import com.pokerapp.api.dto.request.TableSettingsDto;
import com.pokerapp.api.dto.response.TableDto;
import com.pokerapp.domain.game.PokerTable;
import com.pokerapp.domain.user.User;

import java.util.List;

public interface TableService {
    PokerTable createTable(TableSettingsDto settings, User owner);
    PokerTable getTableById(Long id);
    List<TableDto> getAllTables();
    List<TableDto> getPublicTables();
    TableDto joinTable(Long tableId, Long userId, Double buyIn);
    TableDto joinTableAsSpectator(Long tableId, Long userId);
    TableDto leaveTable(Long tableId, Long userId);
    TableDto removeSpectator(Long tableId, Long userId);
}
