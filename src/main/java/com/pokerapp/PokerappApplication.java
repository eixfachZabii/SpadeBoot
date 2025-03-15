// src/main/java/com/pokerapp/PokerApplication.java
package com.pokerapp;

import com.pokerapp.api.dto.request.InvitationRequestDto;
import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.api.dto.request.TableSettingsDto;
import com.pokerapp.api.dto.response.InvitationDto;
import com.pokerapp.api.dto.response.StatisticsDto;
import com.pokerapp.api.dto.response.TableDto;
import com.pokerapp.domain.game.*;
import com.pokerapp.domain.statistics.GameResult;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.Spectator;
import com.pokerapp.domain.user.User;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.service.*;
import com.pokerapp.service.impl.UserRoleServiceImpl;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.pokerapp")
@EnableJpaRepositories(basePackages = "com.pokerapp.repository")
@EntityScan(basePackages = "com.pokerapp.domain")
public class PokerappApplication {

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
    private ReplayService replayService;

    @Autowired
    private UserRoleServiceImpl userRoleService;

    @Autowired
    private PlayerRepository playerRepository; // Still needed for some operations

    public static void main(String[] args) {
        SpringApplication.run(PokerappApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void execCodeAfterStartup() {
        System.out.println("🎮 Initializing poker test environment...");

        try {
            // 1. Create multiple users with different roles
            System.out.println("📝 Creating test users...");

            // Admin user
            RegisterDto adminUser = new RegisterDto();
            adminUser.setUsername("THOMAS_NEUMANN");
            adminUser.setPassword("ICHBINGDBBOSS");
            adminUser.setEmail("THOMAS.NEUMANN2@TUM.de");
            User admin = userService.register(adminUser);
            admin.addRole("ADMIN");
            System.out.println("✅ Admin user created: " + admin.getUsername());

            // Regular users
            String[] playerNames = {"Poker_Pro", "CardShark", "RiverRunner", "BluffMaster", "AllInAndy"};
            List<User> users = new ArrayList<>();
            List<Player> players = new ArrayList<>();

            for (int i = 0; i < playerNames.length; i++) {
                RegisterDto playerDto = new RegisterDto();
                playerDto.setUsername(playerNames[i]);
                playerDto.setPassword("password" + i);
                playerDto.setEmail(playerNames[i].toLowerCase() + "@poker.com");
                User user = userService.register(playerDto);
                user.addRole("USER");

                // Add different starting balances
                userService.updateBalance(user.getId(), 100.0 * (i + 1));
                users.add(user);

                // Create player linked to user
                Player player = userRoleService.convertToPlayer(user);
                players.add(player);
                System.out.println("✅ Player created: " + player.getUser().getUsername() + " with balance $" + player.getUser().getBalance());
            }

            // 2. Create spectator user
            RegisterDto spectatorDto = new RegisterDto();
            spectatorDto.setUsername("Observer");
            spectatorDto.setPassword("watchOnly");
            spectatorDto.setEmail("observer@poker.com");
            User spectatorUser = userService.register(spectatorDto);
            System.out.println("✅ Spectator user created: " + spectatorUser.getUsername());

            // 3. Create poker tables with different settings
            System.out.println("\n🎲 Creating poker tables...");

            // Beginner table
            TableSettingsDto beginnerTableSettings = new TableSettingsDto();
            beginnerTableSettings.setName("Beginner's Table");
            beginnerTableSettings.setDescription("Low stakes, perfect for beginners");
            beginnerTableSettings.setMaxPlayers(6);
            beginnerTableSettings.setMinBuyIn(10.0);
            beginnerTableSettings.setMaxBuyIn(100.0);
            beginnerTableSettings.setPrivate(false);

            TableDto beginnerTableDto = tableService.createTable(beginnerTableSettings, users.get(0));
            System.out.println("✅ Created table: " + beginnerTableDto.getName());

            // Pro table
            TableSettingsDto proTableSettings = new TableSettingsDto();
            proTableSettings.setName("High Rollers");
            proTableSettings.setDescription("High stakes for experienced players");
            proTableSettings.setMaxPlayers(8);
            proTableSettings.setMinBuyIn(500.0);
            proTableSettings.setMaxBuyIn(5000.0);
            proTableSettings.setPrivate(false);

            TableDto proTableDto = tableService.createTable(proTableSettings, users.get(1));
            System.out.println("✅ Created table: " + proTableDto.getName());

            // Private table
            TableSettingsDto privateTableSettings = new TableSettingsDto();
            privateTableSettings.setName("VIP Room");
            privateTableSettings.setDescription("Invitation only");
            privateTableSettings.setMaxPlayers(4);
            privateTableSettings.setMinBuyIn(200.0);
            privateTableSettings.setMaxBuyIn(1000.0);
            privateTableSettings.setPrivate(true);

            TableDto privateTableDto = tableService.createTable(privateTableSettings, users.get(2));
            System.out.println("✅ Created table: " + privateTableDto.getName());

            // 4. Add players to tables
            System.out.println("\n🧑‍🤝‍🧑 Adding players to tables...");

            // Add players to beginner table
            tableService.joinTable(beginnerTableDto.getId(), users.get(0).getId(), 50.0);
            tableService.joinTable(beginnerTableDto.getId(), users.get(3).getId(), 75.0);
            System.out.println("✅ Added players to " + beginnerTableDto.getName());

            // Add players to pro table
            tableService.joinTable(proTableDto.getId(), users.get(1).getId(), 1000.0);
            tableService.joinTable(proTableDto.getId(), users.get(4).getId(), 1500.0);
            System.out.println("✅ Added players to " + proTableDto.getName());

            // 5. Create and start games
            System.out.println("\n🃏 Creating poker games...");

            // Create games for tables
            Game beginnerGame = gameService.createGame(beginnerTableDto.getId());
            Game proGame = gameService.createGame(proTableDto.getId());

            // Start the beginner game
            gameService.startGame(beginnerGame.getId());
            System.out.println("✅ Created and started game for " + beginnerTableDto.getName());

            // For demo purposes, we'll end the pro game immediately
            proGame = gameService.endGame(proGame.getId());
            System.out.println("✅ Created and finished game for " + proTableDto.getName());

            // 6. Generate statistics for players
            System.out.println("\n📊 Generating player statistics...");

            // Apply random statistics for all players
            for (User user : users) {
                Random random = new Random();
                int gamesPlayed = random.nextInt(20) + 1;
                int gamesWon = random.nextInt(gamesPlayed);
                double totalWinnings = random.nextInt(5000);

                // Get or create statistics for player
                StatisticsDto stats = statisticsService.getUserStatistics(user.getId());
                // We'd ideally update stats through the service, but using repository for demo
                Player player = playerRepository.findByUserId(user.getId()).orElseThrow();

                // Build statistics record for this player
                GameResult mockResult = new GameResult();
                mockResult.setGame(proGame);
                Map<Player, Double> winnings = new HashMap<>();
                winnings.put(player, totalWinnings);

                // Add other players with zero winnings
                for (User otherUser : users) {
                    if (!otherUser.getId().equals(user.getId())) {
                        Player otherPlayer = playerRepository.findByUserId(otherUser.getId()).orElseThrow();
                        winnings.put(otherPlayer, 0.0);
                    }
                }

                mockResult.setWinnings(winnings);
                statisticsService.recordGameResult(proGame, winnings);

                // Update some user statistics manually to simulate multiple games
                if (user.getId().equals(users.get(1).getId())) {
                    System.out.println("✅ Recorded major win for " + user.getUsername() + " ($500)");
                }
            }

            System.out.println("✅ Generated player statistics");

            // 7. Create invitations
            System.out.println("\n📨 Creating invitations...");

            InvitationRequestDto invitationRequest = new InvitationRequestDto();
            invitationRequest.setRecipientId(users.get(3).getId());
            invitationRequest.setTableId(privateTableDto.getId());
            invitationRequest.setMessage("Join my exclusive table for a high-stakes game!");

            invitationService.createInvitation(invitationRequest, users.get(2));

            System.out.println("✅ Created invitation to private table");

            System.out.println("\n🚀 Test environment initialization complete!");
            System.out.println("- Users created: " + (users.size() + 2));
            System.out.println("- Players created: " + players.size());
            System.out.println("- Tables created: 3");
            System.out.println("- Games created: 2");

            // =================================================================
            // EXTENDED TESTS FOR COMPOSITION PATTERN
            // =================================================================
            System.out.println("\n🧪 Running extended tests for composition pattern...");

            try {
                // ====== 1. SPECTATOR FUNCTIONALITY TESTING ======
                System.out.println("\n👁️ Testing spectator functionality...");

                // Create additional spectator users
                RegisterDto spectator2Dto = new RegisterDto();
                spectator2Dto.setUsername("Watcher");
                spectator2Dto.setPassword("justWatching");
                spectator2Dto.setEmail("watcher@poker.com");
                User spectator2User = userService.register(spectator2Dto);

                // Convert users to spectators using the role service
                spectatorUser = userService.getUserById(spectatorUser.getId()); // Refresh from DB
                spectator2User = userService.getUserById(spectator2User.getId()); // Refresh from DB

                // Create spectator entities for both spectator users
                Spectator spectator1 = userRoleService.convertToSpectator(spectatorUser);
                Spectator spectator2 = userRoleService.convertToSpectator(spectator2User);
                System.out.println("✅ Created spectator entities for: " + spectator1.getUsername() + ", " + spectator2.getUsername());

                // Have spectators watch different tables
                TableDto beginnerTableRef = tableService.getTableById(beginnerTableDto.getId());
                TableDto proTableRef = tableService.getTableById(proTableDto.getId());

                tableService.joinTableAsSpectator(beginnerTableRef.getId(), spectator1.getUserId());
                tableService.joinTableAsSpectator(proTableRef.getId(), spectator2.getUserId());
                System.out.println("✅ Spectators watching tables: " +
                        spectator1.getUsername() + " → " + beginnerTableRef.getName() + ", " +
                        spectator2.getUsername() + " → " + proTableRef.getName());

                // Verify spectator assignments by getting fresh table references
                beginnerTableRef = tableService.getTableById(beginnerTableDto.getId());
                proTableRef = tableService.getTableById(proTableDto.getId());
                System.out.println("✅ Spectators successfully assigned to tables");

                // Test spectator leaving a table
                tableService.removeSpectator(proTableRef.getId(), spectator2User.getId());
                proTableRef = tableService.getTableById(proTableDto.getId());
                System.out.println("✅ Spectator successfully left table");

                // ====== 2. ROLE TRANSITION TESTING ======
                System.out.println("\n🔄 Testing role transitions...");

                // Convert a spectator to a player (dual role)
                Player convertedPlayer = userRoleService.convertToPlayer(spectator2User);
                System.out.println("✅ Converted spectator to player: " + convertedPlayer.getUsername());

                // Have this player join a table
                tableService.joinTable(privateTableDto.getId(), convertedPlayer.getUserId(), 300.0);
                System.out.println("✅ Former spectator joined table as player: " + privateTableDto.getName());

                // ====== 3. COMPLEX INTERACTION PATTERNS ======
                System.out.println("\n🔀 Testing complex interaction patterns...");

                // Create a user who will be both player and spectator
                RegisterDto dualRoleDto = new RegisterDto();
                dualRoleDto.setUsername("DualRole");
                dualRoleDto.setPassword("twoHats");
                dualRoleDto.setEmail("dual@poker.com");
                User dualRoleUser = userService.register(dualRoleDto);
                userService.updateBalance(dualRoleUser.getId(), 1000.0);

                // Make this user both a player and spectator
                Player dualRolePlayer = userRoleService.convertToPlayer(dualRoleUser);
                Spectator dualRoleSpectator = userRoleService.convertToSpectator(dualRoleUser);

                // Have this user join one table as player and another as spectator
                tableService.joinTable(beginnerTableRef.getId(), dualRoleUser.getId(), 50.0);
                tableService.joinTableAsSpectator(proTableRef.getId(), dualRoleUser.getId());

                // Refresh tables from services
                beginnerTableRef = tableService.getTableById(beginnerTableDto.getId());
                proTableRef = tableService.getTableById(proTableDto.getId());

                System.out.println("✅ User " + dualRoleUser.getUsername() + " is playing at " +
                        beginnerTableRef.getName() + " and watching " + proTableRef.getName());

                // ====== 4. INVITATION TESTING WITH COMPOSITION ======
                System.out.println("\n✉️ Testing invitations with composition pattern...");

                // Create an invitation from one player to another using the service
                InvitationRequestDto testInvitationRequest = new InvitationRequestDto();
                testInvitationRequest.setRecipientId(dualRoleUser.getId());
                testInvitationRequest.setTableId(privateTableDto.getId());
                testInvitationRequest.setMessage("Let's play in the VIP room!");

                invitationService.createInvitation(testInvitationRequest, users.get(0));

                // Test retrieving pending invitations for a user
                List<InvitationDto> pendingInvitations = invitationService.getPendingInvitationsForUser(dualRoleUser);

                System.out.println("✅ User " + dualRoleUser.getUsername() +
                        " has " + pendingInvitations.size() + " pending invitation(s)");

                // Accept the invitation if there are any
                if (!pendingInvitations.isEmpty()) {
                    InvitationDto invitation = invitationService.acceptInvitation(pendingInvitations.get(0).getId(), dualRoleUser);
                    System.out.println("✅ Invitation accepted: " + invitation.getMessage());
                }

                // ====== 5. GAME PLAY SIMULATION ======
                System.out.println("\n🎮 Simulating game play with composition pattern...");

                // Create a new game for testing
                Game testGame = gameService.createGame(beginnerTableDto.getId());
                gameService.startGame(testGame.getId());
                System.out.println("✅ Game started at table: " + beginnerTableDto.getName());

                // Simulate game end
                Game completedGame = gameService.endGame(testGame.getId());
                System.out.println("✅ Game completed");

                // Record game statistics for our dual-role player
                Map<Player, Double> finalWinnings = new HashMap<>();
                Player winner = dualRolePlayer;
                finalWinnings.put(winner, 200.0);

                // Add other players with zero winnings (using our player list from earlier)
                for (Player p : players) {
                    if (!p.getUserId().equals(winner.getUserId())) {
                        finalWinnings.put(p, 0.0);
                    }
                }

                // Record the game result using the statistics service
                GameResult gameResult = statisticsService.recordGameResult(completedGame, finalWinnings);
                statisticsService.updateUserStatistics(gameResult);

                // Get updated statistics for the winner
                StatisticsDto winnerStats = statisticsService.getUserStatistics(winner.getUserId());

                System.out.println("✅ Game results recorded and statistics updated");
                System.out.println("   Winner: " + winner.getUsername() + " with updated statistics");

                System.out.println("\n🎯 Extended composition pattern tests completed successfully!");

            } catch (Exception e) {
                System.err.println("❌ Error in extended tests: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("❌ Error initializing test data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}