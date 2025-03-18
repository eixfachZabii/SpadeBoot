package com.pokerapp;

import com.pokerapp.api.dto.request.InvitationRequestDto;
import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.api.dto.request.TableSettingsDto;
import com.pokerapp.api.dto.response.GameStateDto;
import com.pokerapp.api.dto.response.PlayerStateDto;
import com.pokerapp.api.dto.response.StatisticsDto;
import com.pokerapp.api.dto.response.TableDto;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.GameStatus;
import com.pokerapp.domain.game.MoveType;
import com.pokerapp.domain.game.PokerTable;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import com.pokerapp.repository.GameRepository;
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
    @Autowired
    private GameRepository gameRepository;

    public static void main(String[] args) {
        SpringApplication.run(PokerappApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void execCodeAfterStartup() {
        logInfo("🎮 Initializing poker test environment...");

        try {
            List<User> users = createTestUsers();
 //           List<TableDto> tables = createPokerTables(users);
 //           createAndRunTestGames(tables);
 //           testSpectatorFunctionality(users, tables);
 //           testInvitations(users, tables);
//            simulateCompletePokerRound(users);
            testStatisticsTracking(users);

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

    /**
     * Tests the statistics tracking system by playing multiple game rounds
     */
    private void testStatisticsTracking(List<User> users) {
        logInfo("\n\n📊 TESTING STATISTICS TRACKING SYSTEM 📊");
        logInfo("============================================================");

        // 1. Create a poker table for statistics testing
        TableSettingsDto tableSettings = new TableSettingsDto();
        tableSettings.setName("Statistics Test Table");
        tableSettings.setDescription("Table for testing statistics");
        tableSettings.setMaxPlayers(6);
        tableSettings.setMinBuyIn(100.0);
        tableSettings.setMaxBuyIn(1000.0);
        tableSettings.setPrivate(false);

        logInfo("🏗️ Creating statistics test table...");
        TableDto statisticsTable = tableService.createTable(tableSettings, users.get(0));
        logInfo("✅ Created table: " + statisticsTable.getName() + " (ID: " + statisticsTable.getId() + ")");

        // 2. Display initial statistics for players
        logInfo("\n📊 INITIAL PLAYER STATISTICS:");
        for (int i = 0; i < 4; i++) {
            User user = users.get(i);
            try {
                displayPlayerStatistics(user);
            } catch (Exception e) {
                logInfo("⚠️ Could not get statistics for " + user.getUsername() + ": " + e.getMessage());
            }
        }

        // 3. Add players to the table
        logInfo("\n👥 Adding players to the statistics test table...");
        for (int i = 0; i < 4; i++) {
            User user = users.get(i);
            double buyIn = 500.0;
            try {
                // Set security context to this player
                setSecurityContext(user);

                tableService.joinTable(statisticsTable.getId(), user.getId(), buyIn);
                logInfo("✅ Player " + user.getUsername() + " joined with $" + buyIn);
            } catch (Exception e) {
                logInfo("❌ Error adding " + user.getUsername() + ": " + e.getMessage());
            }
        }

        // 4. Run multiple game rounds
        logInfo("\n🎮 Running multiple game rounds to generate statistics...");
        setSecurityContext(users.get(0));
        Game game = gameService.createGame(statisticsTable.getId());
        try {
            // Start the game (this initiates the first round)
            logInfo("\n🎲 Starting game with ID: " + game.getId());
            gameService.startGame(game.getId());

            // Run additional rounds
            for (int round = 2; round <= 3; round++) {
                logInfo("\n🎲 Starting round " + round + " of 3");

                // Use the new startNextRound method instead
                gameService.startNextRound(game.getId());

                logInfo("✅ Started round " + round);
            }

            // End the game after all rounds
            gameService.endGame(game.getId());
            logInfo("Game ended");
        } catch (Exception e) {
            logInfo("❌ Error during game play: " + e.getMessage());
            e.printStackTrace();
        }

        // 5. Display updated statistics
        logInfo("\n📊 FINAL PLAYER STATISTICS:");
        for (int i = 0; i < 4; i++) {
            User user = users.get(i);
            try {
                displayPlayerStatistics(user);
            } catch (Exception e) {
                logInfo("⚠️ Could not get statistics for " + user.getUsername() + ": " + e.getMessage());
            }
        }

        logInfo("\n📊 STATISTICS TESTING COMPLETE");
        logInfo("============================================================");
    }

    /**
     * Enhanced version of runGameToCompletion with improved debugging and retry logic
     */
    private void runGameToCompletion(Long gameId, List<User> users) {
        // Safety limits
        int moveCount = 0;
        int maxMoves = 60; // Increased to allow for a complete game
        int consecutiveErrors = 0;
        int maxConsecutiveErrors = 5;

        try {
            // Get initial game state
            GameStateDto gameState = gameService.getGameState(gameId);
            System.out.println("DEBUG: Starting game completion for game " + gameId);
            System.out.println("DEBUG: Initial game status: " + gameState.getStatus());

            // Continue until game is finished or we hit the move limit
            while (!gameState.getStatus().equals(GameStatus.FINISHED.toString()) && moveCount < maxMoves) {
                // Find the player whose turn it is by looking for isTurn flag
                PlayerStateDto currentPlayerState = null;
                for (PlayerStateDto player : gameState.getPlayers()) {
                    if (player.isTurn()) {
                        currentPlayerState = player;
                        break;
                    }
                }

                if (currentPlayerState == null) {
                    System.out.println("DEBUG: No player found with active turn. Current stage: " +
                            gameState.getStage() + ". Refreshing game state...");

                    // The game might be between betting rounds or stages, refresh the state
                    gameState = gameService.getGameState(gameId);
                    Thread.sleep(500); // Slightly longer delay

                    // Increment consecutive errors counter
                    consecutiveErrors++;

                    // If we've had too many consecutive errors, something is wrong
                    if (consecutiveErrors >= maxConsecutiveErrors) {
                        System.out.println("DEBUG: Too many consecutive errors. Trying to advance game...");
                        try {
                            // Try to end the game if we're stuck
                            gameService.endGame(gameId);
                            System.out.println("DEBUG: Forcibly ended the game after too many errors");
                        } catch (Exception e) {
                            System.out.println("DEBUG: Failed to forcibly end game: " + e.getMessage());
                        }
                        return;
                    }

                    continue;
                }

                // Reset consecutive errors counter since we found a player
                consecutiveErrors = 0;

                // Find the user for this player using the userId field
                User currentUser = null;
                if (currentPlayerState.getUserId() != null) {
                    try {
                        currentUser = userService.getUserById(currentPlayerState.getUserId());
                    } catch (Exception e) {
                        // Fall back to matching by username
                        for (User user : users) {
                            if (user.getUsername().equals(currentPlayerState.getUsername())) {
                                currentUser = user;
                                break;
                            }
                        }
                    }
                }

                if (currentUser == null) {
                    System.out.println("DEBUG: Couldn't find user for player: " + currentPlayerState.getUsername());
                    gameState = gameService.getGameState(gameId);
                    Thread.sleep(300);
                    continue;
                }

                // Set the security context to the current player
                setSecurityContext(currentUser);
                System.out.println("DEBUG: Set security context to " + currentUser.getUsername());

                // Determine possible actions
                List<String> possibleActions = gameState.getPossibleActions();
                if (possibleActions == null || possibleActions.isEmpty()) {
                    System.out.println("DEBUG: No possible actions for " + currentUser.getUsername() +
                            ". Current stage: " + gameState.getStage());
                    gameState = gameService.getGameState(gameId);
                    Thread.sleep(300);
                    continue;
                }

                // Make a decision based on possible actions
                MoveDto move = decideMoveForPlayer(possibleActions, gameState);

                // Log the intended action
                System.out.println("DEBUG: Move " + (moveCount + 1) + ": " + currentUser.getUsername() +
                        " - " + move.getType() + (move.getAmount() != null ? " $" + move.getAmount() : "") +
                        " at stage " + gameState.getStage());

                try {
                    // Execute the move
                    //gameState = gameService.makeMove(gameId, currentUser.getId(), move);
                    moveCount++;
                    System.out.println("DEBUG: Move " + moveCount + " executed successfully");

                    // Print the current game stage after each successful move
                    System.out.println("DEBUG: Current game stage after move: " + gameState.getStage() +
                            ", Pot: $" + gameState.getPot());

                    // Reset consecutive errors
                    consecutiveErrors = 0;
                } catch (Exception e) {
                    System.out.println("DEBUG: Error making move: " + e.getMessage() +
                            " - Refreshing game state and trying again");
                    gameState = gameService.getGameState(gameId);
                    Thread.sleep(500); // Longer delay after error

                    // Increment consecutive errors
                    consecutiveErrors++;
                    continue;
                }

                // Add a delay for readability and to let any background processing complete
                Thread.sleep(300);
            }

            if (moveCount >= maxMoves) {
                System.out.println("DEBUG: Reached maximum move limit. Ending game.");
                try {
                    gameService.endGame(gameId);
                } catch (Exception e) {
                    System.out.println("DEBUG: Error ending game: " + e.getMessage());
                }
            }

            System.out.println("DEBUG: Game completion finished. Total moves: " + moveCount);

            // Get final game state
            GameStateDto finalState = gameService.getGameState(gameId);
            System.out.println("DEBUG: Final game status: " + finalState.getStatus());

        } catch (Exception e) {
            System.out.println("DEBUG: Error running game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find the User entity that corresponds to a Player ID in the game
     */
    private User findUserForPlayer(Long playerId, GameStateDto gameState, List<User> users) {
        // Find player in the game state
        for (PlayerStateDto player : gameState.getPlayers()) {
            if (player.getId().equals(playerId)) {
                // If the player has a userId field, use that
                if (player.getUserId() != null) {
                    try {
                        return userService.getUserById(player.getUserId());
                    } catch (Exception e) {
                        // If that fails, try to find by username
                        String username = player.getUsername();
                        for (User user : users) {
                            if (user.getUsername().equals(username)) {
                                return user;
                            }
                        }
                    }
                }
            }
        }

        // As a fallback, try matching by username
        for (PlayerStateDto player : gameState.getPlayers()) {
            if (player.getId().equals(playerId)) {
                String username = player.getUsername();
                for (User user : users) {
                    if (user.getUsername().equals(username)) {
                        return user;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Makes a strategic decision for the player based on possible actions
     */
    private MoveDto decideMoveForPlayer(List<String> possibleActions, GameStateDto gameState) {
        MoveDto move = new MoveDto();

        // Always call if possible
        if (possibleActions.contains("CALL")) {
            move.setType("CALL");
        }
        // If can't call, check when possible
        else if (possibleActions.contains("CHECK")) {
            move.setType("CHECK");
        }
        // If can't call or check, fold
        else if (possibleActions.contains("FOLD")) {
            move.setType("FOLD");
        }
        // If fold isn't available but ALL_IN is, go all-in
        else if (possibleActions.contains("ALL_IN")) {
            move.setType("ALL_IN");
        }
        // Default to the first available action as fallback
        else if (!possibleActions.isEmpty()) {
            move.setType(possibleActions.get(0));
        }
        // Should never happen, but as a safety measure
        else {
            move.setType("FOLD");
        }

        return move;
    }

    /**
     * Display a player's statistics
     */
    private void displayPlayerStatistics(User user) {
        try {
            StatisticsDto stats = statisticsService.getUserStatistics(user.getId());
            logInfo(user.getUsername() + " (ID: " + stats.getUserId() + ")");
            logInfo("  Games Played:   " + stats.getGamesPlayed());
            logInfo("  Games Won:      " + stats.getGamesWon());
            logInfo("  Win Rate:       " + (stats.getWinRate() * 100) + "%");
            logInfo("  Total Winnings: $" + stats.getTotalWinnings());
        } catch (Exception e) {
            logInfo("⚠️ No statistics available for " + user.getUsername());
        }
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

            // Set the security context to this user so the service knows who's acting
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

        try {
            // Run the game to completion using our turn-aware method
            runGameToCompletion(game.getId(), pokePlayers);

            // End the game if it's not already finished
            gameState = gameService.getGameState(game.getId());
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
     * Logs the current game state in a readable format with enhanced player turn information
     */
    private void logGameState(GameStateDto gameState) {
        logInfo("\n📊 GAME STATE UPDATE:");
        logInfo("Game ID: " + gameState.getGameId() + " | Status: " + gameState.getStatus());
        logInfo("Current Stage: " + gameState.getStage() + " | Pot: $" + gameState.getPot());

        // Log who the current player is very clearly
        if (gameState.getCurrentPlayerId() != null) {
            logInfo("⏩ CURRENT PLAYER'S TURN: " +
                    gameState.getCurrentPlayerName() +
                    " (Player ID: " + gameState.getCurrentPlayerId() +
                    ", User ID: " + gameState.getCurrentUserId() + ")");
        } else {
            logInfo("⏸️ No current player - game may be between stages");
        }

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

        // Log each player's state with enhanced turn indicator
        logInfo("\nPlayers:");
        for (PlayerStateDto player : gameState.getPlayers()) {
            StringBuilder playerInfo = new StringBuilder();
            playerInfo.append(player.getUsername())
                    .append(" (Player ID: ").append(player.getId())
                    .append(", User ID: ").append(player.getUserId()).append(")")
                    .append(" - Chips: $").append(player.getChips())
                    .append(" | Status: ").append(player.getStatus());

            // Make it very obvious which player's turn it is
            if (player.isTurn()) {
                playerInfo.append(" 👈 CURRENT TURN");
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

    private void logInfo(String message) {
        logger.info(message);
        System.out.println(message);
    }

    private void logError(String message) {
        logger.severe(message);
        System.err.println(message);
    }
}