package com.pokerapp.api.controller;

import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.service.UserService;
import com.pokerapp.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TableService tableService;

    /**
     * Check if the current user is at a table
     * @return TableStatus object with tableId if at table, or null if not
     */
    @GetMapping("/current-table")
    public ResponseEntity<Map<String, Object>> getCurrentTable() {
        User currentUser = userService.getCurrentUser();
        Map<String, Object> response = new HashMap<>();

        if (currentUser != null) {
            Optional<Player> playerOpt = playerRepository.findByUserId(currentUser.getId());

            if (playerOpt.isPresent()) {
                Player player = playerOpt.get();
                Long tableId = player.getCurrentTableId();

                response.put("isAtTable", tableId != null);
                response.put("tableId", tableId);

                // If at a table, include basic table info
                if (tableId != null) {
                    try {
                        response.put("table", tableService.getTableById(tableId));
                    } catch (Exception e) {
                        // Table might have been deleted or other error
                        response.put("table", null);
                    }
                }
            } else {
                response.put("isAtTable", false);
                response.put("tableId", null);
            }
        } else {
            response.put("isAtTable", false);
            response.put("tableId", null);
        }

        return ResponseEntity.ok(response);
    }
}