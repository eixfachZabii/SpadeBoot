// src/main/java/com/pokerapp/api/controller/TableController.java
package com.pokerapp.api.controller;

import com.pokerapp.api.dto.request.TableSettingsDto;
import com.pokerapp.api.dto.response.TableDto;
import com.pokerapp.domain.game.PokerTable;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import com.pokerapp.service.TableService;
import com.pokerapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    @Autowired
    private TableService tableService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<TableDto> createTable(@Valid @RequestBody TableSettingsDto settings) {
        User currentUser = userService.getCurrentUser();
        userService.createPlayer(currentUser.getId());
        TableDto table = tableService.createTable(settings, currentUser);
        return ResponseEntity.ok(table);
    }

    @GetMapping
    public ResponseEntity<List<TableDto>> getAllTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    @GetMapping("/public")
    public ResponseEntity<List<TableDto>> getPublicTables() {
        return ResponseEntity.ok(tableService.getPublicTables());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableDto> getTableById(@PathVariable Long id) {
        return ResponseEntity.ok(tableService.getTableById(id));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<TableDto> joinTable(@PathVariable Long id, @RequestParam Integer buyIn) {
        // Pass the User ID, not the Player ID - the service will handle conversion
        Long userId = userService.getCurrentUser().getId();
        userService.createPlayer(userId);
        return ResponseEntity.ok(tableService.joinTable(id, userId, buyIn));
    }


    @PostMapping("/{id}/leave")
    public ResponseEntity<TableDto> leaveTable(@PathVariable Long id) {
        // Pass the User ID, not the Player ID
        Long userId = userService.getCurrentUser().getId();
        return ResponseEntity.ok(tableService.leaveTable(id, userId));
    }
}