package com.pokerapp;

import com.pokerapp.api.dto.request.InvitationRequestDto;
import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.api.dto.request.TableSettingsDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.api.dto.response.PlayerStateDto;
import com.pokerapp.api.dto.response.TableDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.GameStatus;
import com.pokerapp.domain.game.MoveType;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

            // New addition - run a complete poker round simulation
            //simulateCompletePokerRound(users);

            logInfo("\n🚀 Test environment initialization complete!");
        } catch (Exception e) {
            logError("❌ Error initializing test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Simulates a complete poker round with 4 players taking actions
     */
    private void simulateCompletePokerRound(List<User> users) {
        logInfo("\n\n🎲 STARTING POKER ROUND SIMULATION WITH 4 PLAYERS 🎲");
        logInfo("============================================================");

        // 1. Create a poker table specifically for this simulation
        TableSettingsDto tableSettings = new TableSettingsDto();
        tableSettings.setName("Simulation Table");
        tableSettings.setDescription("Table for a 4-player simulation round");
        tableSettings.setMaxPlayers(6);  // Allow up to 6 players, but we'll use 4
        tableSettings.setMinBuyIn(100.0);
        tableSettings.setMaxBuyIn(1000.0);
        tableSettings.setPrivate(false);

        logInfo("🏗️ Creating simulation poker table...");
        TableDto simulationTable = tableService.createTable(tableSettings, users.get(0));
        logInfo("✅ Created table: " + simulationTable.getName() + " (ID: " + simulationTable.getId() + ")");

        // 2. Get first 4 users from our test users and have them join the table
        List<User> pokePlayers = users.subList(0, Math.min(4, users.size()));

        logInfo("\n👥 Adding 4 players to the table...");
        for (int i = 0; i < pokePlayers.size(); i++) {
            User user = pokePlayers.get(i);
            double buyIn = 500.0 + (i * 100); // Different buy-ins for each player

            // Set the current security context to this user so the service knows who's acting
            setSecurityContext(user);

            tableService.joinTable(simulationTable.getId(), user.getId(), buyIn);
            logInfo("✅ Player " + user.getUsername() + " joined with $" + buyIn + " buy-in");
        }

        // 3. Create a new game at the table
        logInfo("\n🃏 Creating a new poker game...");
        setSecurityContext(pokePlayers.get(0)); // Use the first player as the game creator
        Game game = gameService.createGame(simulationTable.getId());
        logInfo("✅ Created game with ID: " + game.getId());

        // 4. Start the game
        logInfo("\n🎬 Starting the poker game...");
        gameService.startGame(game.getId());
        GameStateDto gameState = gameService.getGameState(game.getId());
        logGameState(gameState);

        // 5. Simulate playing through the hand with each player taking actions
        logInfo("\n🎯 BEGINNING GAMEPLAY SIMULATION");
        logInfo("============================================================");

        // Track moves made to prevent infinite loops
        int movesMade = 0;
        int maxMoves = 40; // Safety limit

        try {
            // Continue playing until the game is finished or we've made too many moves
            while (gameState.getStatus().equals(GameStatus.IN_PROGRESS.toString()) && movesMade < maxMoves) {
                // Get the current player
                Long currentPlayerId = gameState.getCurrentPlayerId();
                if (currentPlayerId == null) {
                    logInfo("No current player found, game might be between stages.");
                    break;
                }

                // Find the user corresponding to the current player
                User currentUser = findUserForPlayer(currentPlayerId, gameState);
                if (currentUser == null) {
                    logInfo("⚠️ Couldn't find user for player ID: " + currentPlayerId);
                    break;
                }

                // Set the security context to the current player
                setSecurityContext(currentUser);

                // Determine possible actions
                List<String> possibleActions = gameState.getPossibleActions();
                if (possibleActions == null || possibleActions.isEmpty()) {
                    logInfo("⚠️ No possible actions for player: " + currentUser.getUsername());
                    break;
                }

                // Make a decision based on possible actions
                MoveDto move = decideMoveForPlayer(possibleActions, gameState);

                // Log the intended action
                logInfo("🎮 Player " + currentUser.getUsername() + " is making a " + move.getType() +
                        (move.getAmount() != null ? " of $" + move.getAmount() : ""));

                // Execute the move
                gameState = gameService.makeMove(game.getId(), currentUser.getId(), move);
                movesMade++;

                // Log the updated game state
                logGameState(gameState);

                // Add a small delay to make logs easier to read
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Check if game finished naturally or hit the safety limit
            if (movesMade >= maxMoves) {
                logInfo("⚠️ Simulation stopping after " + maxMoves + " moves for safety");
            }

            // End the game if it's not already finished
            if (!gameState.getStatus().equals(GameStatus.FINISHED.toString())) {
                logInfo("\n🏁 Ending the game...");
                game = gameService.endGame(game.getId());
                logInfo("✅ Game ended with status: " + game.getStatus());

                // Get final game state
                gameState = gameService.getGameState(game.getId());
                logGameState(gameState);
            }

            // Check statistics
            logInfo("\n📊 Final player statistics after the round:");
            for (User user : pokePlayers) {
                try {
                    logInfo("Player " + user.getUsername() + ": Balance = $" + user.getBalance());
                } catch (Exception e) {
                    logInfo("⚠️ Couldn't retrieve statistics for " + user.getUsername());
                }
            }

        } catch (Exception e) {
            logError("❌ Error during gameplay simulation: " + e.getMessage());
            e.printStackTrace();
        }

        logInfo("\n🏆 POKER ROUND SIMULATION COMPLETE");
        logInfo("============================================================");
    }

    /**
     * Makes a strategic decision for the player based on possible actions
     */
    private MoveDto decideMoveForPlayer(List<String> possibleActions, GameStateDto gameState) {
        MoveDto move = new MoveDto();
        Random random = new Random();

        // Find the player in the game state
        PlayerStateDto playerState = null;
        for (PlayerStateDto player : gameState.getPlayers()) {
            if (player.isTurn()) {
                playerState = player;
                break;
            }
        }

        if (playerState == null) {
            // Fallback if we can't find the player
            String action = possibleActions.get(random.nextInt(possibleActions.size()));
            move.setType(action);
            if (action.equals("RAISE")) {
                move.setAmount(gameState.getCurrentBet() * 2); // Default to 2x current bet
            }
            return move;
        }

        double playerChips = playerState.getChips();
        double currentBet = gameState.getCurrentBet() != null ? gameState.getCurrentBet() : 0;

        // Simple strategy:
        // 1. If CHECK is possible, 70% chance to CHECK, 30% to RAISE
        if (possibleActions.contains("CHECK")) {
            if (random.nextDouble() < 0.7) {
                move.setType("CHECK");
            } else {
                move.setType("RAISE");
                // Raise between 1-3x the big blind or 1/10 of chips, whichever is smaller
                double raiseAmount = Math.min(
                        Math.max(10, random.nextInt(30) + 10),
                        playerChips / 10
                );
                move.setAmount(raiseAmount);
            }
        }
        // 2. If CALL is possible, 60% chance to CALL, 20% to RAISE, 20% to FOLD
        else if (possibleActions.contains("CALL")) {
            double callRatio = currentBet / playerChips; // How much of our stack is the call
            double rand = random.nextDouble();

            if (callRatio > 0.5) {
                // Big call relative to our stack - more likely to fold
                if (rand < 0.4) {
                    move.setType("FOLD");
                } else if (rand < 0.9) {
                    move.setType("CALL");
                } else {
                    move.setType("RAISE");
                    move.setAmount(currentBet * 2);
                }
            } else {
                // Reasonable call
                if (rand < 0.6) {
                    move.setType("CALL");
                } else if (rand < 0.8) {
                    move.setType("RAISE");
                    // Raise between 2-3x the current bet
                    move.setAmount(currentBet * (2 + random.nextDouble()));
                } else {
                    move.setType("FOLD");
                }
            }
        }
        // 3. If neither CHECK nor CALL is possible, use other available actions
        else {
            // Fallback: choose randomly from possible actions
            String action = possibleActions.get(random.nextInt(possibleActions.size()));
            move.setType(action);

            if (action.equals("RAISE")) {
                // Default raise to 2x current bet
                move.setAmount(Math.max(currentBet * 2, 10));
            } else if (action.equals("ALL_IN")) {
                move.setAmount(playerChips);
            }
        }

        // Final safety checks for move amounts
        if (move.getType().equals("RAISE") && (move.getAmount() == null || move.getAmount() <= currentBet)) {
            // Make sure RAISE is valid
            move.setAmount(Math.max(currentBet * 2, Math.min(20, playerChips / 2)));
        }

        return move;
    }

    /**
     * Logs the current game state in a readable format
     */
    private void logGameState(GameStateDto gameState) {
        logInfo("\n📊 GAME STATE UPDATE:");
        logInfo("Game ID: " + gameState.getGameId() + " | Status: " + gameState.getStatus());
        logInfo("Current Stage: " + gameState.getStage() + " | Pot: $" + gameState.getPot());

        // Log community cards if any
        if (gameState.getCommunityCards() != null && !gameState.getCommunityCards().isEmpty()) {
            StringBuilder communityCards = new StringBuilder("Community Cards: ");
            for (int i = 0; i < gameState.getCommunityCards().size(); i++) {
                if (!gameState.getCommunityCards().get(i).isHidden()) {
                    communityCards.append(gameState.getCommunityCards().get(i).getRank())
                            .append(" of ")
                            .append(gameState.getCommunityCards().get(i).getSuit());
                    if (i < gameState.getCommunityCards().size() - 1) {
                        communityCards.append(", ");
                    }
                }
            }
            logInfo(communityCards.toString());
        }

        // Log each player's state
        logInfo("\nPlayers:");
        for (PlayerStateDto player : gameState.getPlayers()) {
            StringBuilder playerInfo = new StringBuilder();
            playerInfo.append(player.getUsername())
                    .append(" - Chips: $").append(player.getChips())
                    .append(" | Status: ").append(player.getStatus());

            if (player.isTurn()) {
                playerInfo.append(" 👈 CURRENT PLAYER");
            }

            // Add player's cards if visible
            if (player.getCards() != null && !player.getCards().isEmpty() &&
                    !player.getCards().get(0).isHidden()) {

                playerInfo.append(" | Cards: ");
                for (int i = 0; i < player.getCards().size(); i++) {
                    if (!player.getCards().get(i).isHidden()) {
                        playerInfo.append(player.getCards().get(i).getRank())
                                .append(" of ")
                                .append(player.getCards().get(i).getSuit());
                        if (i < player.getCards().size() - 1) {
                            playerInfo.append(", ");
                        }
                    } else {
                        playerInfo.append("[Hidden]");
                    }
                }
            }

            logInfo(playerInfo.toString());
        }

        // Log current bet and possible actions
        if (gameState.getCurrentBet() != null && gameState.getCurrentBet() > 0) {
            logInfo("\nCurrent Bet: $" + gameState.getCurrentBet());
        }

        if (gameState.getPossibleActions() != null && !gameState.getPossibleActions().isEmpty()) {
            logInfo("Possible Actions: " + String.join(", ", gameState.getPossibleActions()));
        }

        logInfo("------------------------------------------------------------");
    }

    /**
     * Helper method to find the User entity that corresponds to a Player ID in the game
     */
    private User findUserForPlayer(Long playerId, GameStateDto gameState) {
        // Find the player in the game state
        for (PlayerStateDto player : gameState.getPlayers()) {
            if (player.getId().equals(playerId)) {
                // Return the user with this ID
                try {
                    return userService.getUserById(player.getUserId());
                } catch (Exception e) {
                    logError("Error finding user for player ID " + playerId + ": " + e.getMessage());
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Sets the security context to the specified user
     */
    private void setSecurityContext(User user) {
        // Create authentication with the user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                null // No authorities needed for this simulation
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
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