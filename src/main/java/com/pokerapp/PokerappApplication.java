package com.pokerapp;

import com.pokerapp.api.dto.request.InvitationRequestDto;
import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.api.dto.request.TableSettingsDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.api.dto.response.TableDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.PokerTable;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import com.pokerapp.service.GameService;
import com.pokerapp.service.InvitationService;
import com.pokerapp.service.StatisticsService;
import com.pokerapp.service.TableService;
import com.pokerapp.service.UserRoleService;
import com.pokerapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = "com.pokerapp")
@EnableJpaRepositories(basePackages = "com.pokerapp.repository")
@EntityScan(basePackages = "com.pokerapp.domain")
public class PokerappApplication {

    private static final Logger logger = Logger.getLogger(PokerappApplication.class.getName());

    @Autowired
    private UserService userService;

    @Autowired
    private TableService tableService;

    @Autowired
    private GameService gameService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private UserRoleService userRoleService;

    public static void main(String[] args) {
        SpringApplication.run(PokerappApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void execCodeAfterStartup() {
        logInfo("🎮 Initializing poker test environment...");

        try {
            List<User> users = createTestUsers();
            List<TableDto> tables = createPokerTables(users);
            createAndRunTestGames(tables);
            testSpectatorFunctionality(users, tables);
            testInvitations(users, tables);

            logInfo("\n🚀 Test environment initialization complete!");
        } catch (Exception e) {
            logError("❌ Error initializing test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates test users with various roles
     */
    private List<User> createTestUsers() {
        logInfo("📝 Creating test users...");

        List<User> users = new ArrayList<>();

        // Create admin user
        RegisterDto adminUser = new RegisterDto();
        adminUser.setUsername("THOMAS_NEUMANN");
        adminUser.setPassword("ICHBINGDBBOSS");
        adminUser.setEmail("THOMAS.NEUMANN2@TUM.de");
        User admin = userService.register(adminUser);
        admin.addRole("ADMIN");
        logInfo("✅ Admin user created: " + admin.getUsername());

        // Create regular players
        String[] playerNames = {"Poker_Pro", "CardShark", "RiverRunner", "BluffMaster", "AllInAndy"};
        for (int i = 0; i < playerNames.length; i++) {
            RegisterDto playerDto = new RegisterDto();
            playerDto.setUsername(playerNames[i]);
            playerDto.setPassword("password" + i);
            playerDto.setEmail(playerNames[i].toLowerCase() + "@poker.com");

            User user = userService.register(playerDto);
            user.addRole("USER");

            // Add different starting balances
            userService.updateBalance(user.getId(), 1000.0 * (i + 1));
            users.add(user);

            // Convert user to player
            Player player = userRoleService.convertToPlayer(user);
            logInfo("✅ Player created: " + player.getUsername() + " with balance $" + player.getUser().getBalance());
        }

        // Create spectator user
        RegisterDto spectatorDto = new RegisterDto();
        spectatorDto.setUsername("Observer");
        spectatorDto.setPassword("watchOnly");
        spectatorDto.setEmail("observer@poker.com");
        User spectatorUser = userService.register(spectatorDto);
        users.add(spectatorUser);
        logInfo("✅ Spectator user created: " + spectatorUser.getUsername());

        return users;
    }

    /**
     * Creates poker tables with different settings
     */
    private List<TableDto> createPokerTables(List<User> users) {
        logInfo("\n🎲 Creating poker tables...");

        List<TableDto> tables = new ArrayList<>();

        // Beginner table
        TableSettingsDto beginnerTableSettings = new TableSettingsDto();
        beginnerTableSettings.setName("Beginner's Table");
        beginnerTableSettings.setDescription("Low stakes, perfect for beginners");
        beginnerTableSettings.setMaxPlayers(6);
        beginnerTableSettings.setMinBuyIn(10.0);
        beginnerTableSettings.setMaxBuyIn(100.0);
        beginnerTableSettings.setPrivate(false);

        TableDto beginnerTable = tableService.createTable(beginnerTableSettings, users.get(0));
        tables.add(beginnerTable);
        logInfo("✅ Created table: " + beginnerTable.getName());

        // Pro table
        TableSettingsDto proTableSettings = new TableSettingsDto();
        proTableSettings.setName("High Rollers");
        proTableSettings.setDescription("High stakes for experienced players");
        proTableSettings.setMaxPlayers(8);
        proTableSettings.setMinBuyIn(500.0);
        proTableSettings.setMaxBuyIn(5000.0);
        proTableSettings.setPrivate(false);

        TableDto proTable = tableService.createTable(proTableSettings, users.get(1));
        tables.add(proTable);
        logInfo("✅ Created table: " + proTable.getName());

        // Private table
        TableSettingsDto privateTableSettings = new TableSettingsDto();
        privateTableSettings.setName("VIP Room");
        privateTableSettings.setDescription("Invitation only");
        privateTableSettings.setMaxPlayers(4);
        privateTableSettings.setMinBuyIn(200.0);
        privateTableSettings.setMaxBuyIn(1000.0);
        privateTableSettings.setPrivate(true);

        TableDto privateTable = tableService.createTable(privateTableSettings, users.get(2));
        tables.add(privateTable);
        logInfo("✅ Created table: " + privateTable.getName());

        // Add players to tables
        logInfo("\n🧑‍🤝‍🧑 Adding players to tables...");

        // Add players to beginner table
        tableService.joinTable(beginnerTable.getId(), users.get(0).getId(), 50.0);
        tableService.joinTable(beginnerTable.getId(), users.get(3).getId(), 75.0);
        logInfo("✅ Added players to " + beginnerTable.getName());

        // Add players to pro table
        tableService.joinTable(proTable.getId(), users.get(1).getId(), 1000.0);
        tableService.joinTable(proTable.getId(), users.get(4).getId(), 1500.0);
        logInfo("✅ Added players to " + proTable.getName());

        return tables;
    }

    /**
     * Creates and runs test games at the tables
     */
    private void createAndRunTestGames(List<TableDto> tables) {
        logInfo("\n🃏 Creating poker games...");

        // Create and start game for beginner table
        Game beginnerGame = gameService.createGame(tables.get(0).getId());
        gameService.startGame(beginnerGame.getId());
        logInfo("✅ Created and started game for " + tables.get(0).getName());

        // Get the game state - wrap in try-catch to handle potential exceptions
        try {
            GameStateDto beginnerGameState = gameService.getGameState(beginnerGame.getId());
            logInfo("   Game state: " + beginnerGameState.getStatus() +
                    " | Pot: $" + beginnerGameState.getPot() +
                    " | Players: " + beginnerGameState.getPlayers().size());
        } catch (Exception e) {
            logError("   Error getting game state: " + e.getMessage());
        }

        // Create and end game for pro table
        Game proGame = gameService.createGame(tables.get(1).getId());
        gameService.startGame(proGame.getId());
        proGame = gameService.endGame(proGame.getId());
        logInfo("✅ Created and finished game for " + tables.get(1).getName());
    }

    /**
     * Tests spectator functionality
     */
    private void testSpectatorFunctionality(List<User> users, List<TableDto> tables) {
        logInfo("\n👁️ Testing spectator functionality...");

        User spectatorUser = users.get(users.size() - 1); // The last user is our spectator
        userRoleService.convertToSpectator(spectatorUser);

        // Have spectator watch a table
        tableService.joinTableAsSpectator(tables.get(0).getId(), spectatorUser.getId());
        logInfo("✅ Spectator " + spectatorUser.getUsername() + " is watching " + tables.get(0).getName());
    }

    /**
     * Converts a PokerTable entity to a TableDto
     */
    private TableDto convertToTableDto(PokerTable pokerTable) {
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

    /**
     * Tests invitation functionality
     */
    private void testInvitations(List<User> users, List<TableDto> tables) {
        logInfo("\n📨 Testing invitation functionality...");

        // Invite a player to the private table
        InvitationRequestDto invitationRequest = new InvitationRequestDto();
        invitationRequest.setRecipientId(users.get(3).getId());  // Invite the fourth user
        invitationRequest.setTableId(tables.get(2).getId());     // To the private table
        invitationRequest.setMessage("Join my exclusive table for a high-stakes game!");

        invitationService.createInvitation(invitationRequest, users.get(2));
        logInfo("✅ Created invitation to private table");
    }

    private void logInfo(String message) {
        logger.info(message);
        System.out.println(message);
    }

    private void logError(String message) {
        logger.severe(message);
        System.err.println(message);
    }
}