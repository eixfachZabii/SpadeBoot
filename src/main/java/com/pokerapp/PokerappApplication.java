// src/main/java/com/pokerapp/PokerApplication.java
package com.pokerapp;

import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.domain.card.Hand;
import com.pokerapp.domain.game.*;
import com.pokerapp.domain.invitation.Invitation;
import com.pokerapp.domain.invitation.InvitationStatus;
import com.pokerapp.domain.statistics.GameResult;
import com.pokerapp.domain.user.*;
import com.pokerapp.repository.GameRepository;
import com.pokerapp.repository.GameResultRepository;
import com.pokerapp.repository.InvitationRepository;
import com.pokerapp.repository.PlayerRepository;
import com.pokerapp.repository.StatisticsRepository;
import com.pokerapp.repository.TableRepository;
import com.pokerapp.repository.UserRepository;
import com.pokerapp.service.UserService;
import com.pokerapp.service.impl.UserServiceImpl;
import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.domain.card.Hand;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.GameStatus;
import com.pokerapp.domain.invitation.Invitation;
import com.pokerapp.domain.invitation.InvitationStatus;
import com.pokerapp.domain.statistics.GameResult;
import com.pokerapp.domain.statistics.Statistics;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication(scanBasePackages = "com.pokerapp")
@EnableJpaRepositories(basePackages = "com.pokerapp.repository")
@EntityScan(basePackages = "com.pokerapp.domain")
public class PokerappApplication {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameResultRepository gameResultRepository;

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Autowired
    private InvitationRepository invitationRepository;

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
            adminUser.setUsername("THOMAS_NEUMANN2");
            adminUser.setPassword("ICHBINGDBBOSS");
            adminUser.setEmail("THOMAS.NEUMANN2@TUM.de");
            User admin = userService.register(adminUser);
            admin.addRole("ADMIN");
            userRepository.save(admin);
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
                Player player = createPlayerForUser(user);
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
            PokerTable beginnerTable = new PokerTable();
            beginnerTable.setName("Beginner's Table");
            beginnerTable.setDescription("Low stakes, perfect for beginners");
            beginnerTable.setMaxPlayers(6);
            beginnerTable.setMinBuyIn(10.0);
            beginnerTable.setMaxBuyIn(100.0);
            beginnerTable.setPrivate(false);
            beginnerTable.setOwner(players.get(0)); // Now using the Player directly
            tableRepository.save(beginnerTable);
            System.out.println("✅ Created table: " + beginnerTable.getName());
            
            // Pro table
            PokerTable proTable = new PokerTable();
            proTable.setName("High Rollers");
            proTable.setDescription("High stakes for experienced players");
            proTable.setMaxPlayers(8);
            proTable.setMinBuyIn(500.0);
            proTable.setMaxBuyIn(5000.0);
            proTable.setPrivate(false);
            proTable.setOwner(players.get(1));
            tableRepository.save(proTable);
            System.out.println("✅ Created table: " + proTable.getName());
            
            // Private table
            PokerTable privateTable = new PokerTable();
            privateTable.setName("VIP Room");
            privateTable.setDescription("Invitation only");
            privateTable.setMaxPlayers(4);
            privateTable.setMinBuyIn(200.0);
            privateTable.setMaxBuyIn(1000.0);
            privateTable.setPrivate(true);
            privateTable.setOwner(players.get(2));
            tableRepository.save(privateTable);
            System.out.println("✅ Created table: " + privateTable.getName());
            
            // 4. Add players to tables
            System.out.println("\n🧑‍🤝‍🧑 Adding players to tables...");
            
            // Add players to beginner table
            beginnerTable.addPlayer(players.get(0), 50.0);
            beginnerTable.addPlayer(players.get(3), 75.0);
            tableRepository.save(beginnerTable);
            System.out.println("✅ Added players to " + beginnerTable.getName());
            
            // Add players to pro table
            proTable.addPlayer(players.get(1), 1000.0);
            proTable.addPlayer(players.get(4), 2000.0);
            tableRepository.save(proTable);
            System.out.println("✅ Added players to " + proTable.getName());
            
            // 5. Create and start games
            System.out.println("\n🃏 Creating poker games...");
            
            // Create a game for the beginner table
            Game beginnerGame = new Game();
            beginnerGame.setPokerTable(beginnerTable);
            beginnerGame.setSmallBlind(5.0);
            beginnerGame.setBigBlind(10.0);
            beginnerGame.setStatus(GameStatus.WAITING);
            beginnerGame.setDealerPosition(0);
            gameRepository.save(beginnerGame);
            
            // Set as current game for the table
            beginnerTable.setCurrentGame(beginnerGame);
            tableRepository.save(beginnerTable);
            System.out.println("✅ Created game for " + beginnerTable.getName());
            
            // Create finished game with results
            Game finishedGame = new Game();
            finishedGame.setPokerTable(proTable);
            finishedGame.setSmallBlind(25.0);
            finishedGame.setBigBlind(50.0);
            finishedGame.setStatus(GameStatus.FINISHED);
            finishedGame.setDealerPosition(1);
            gameRepository.save(finishedGame);
            System.out.println("✅ Created finished game for " + proTable.getName());
            
            // 6. Record game results
            GameResult result = new GameResult();
            result.setGame(finishedGame);
            Map<Player, Double> winnings = new HashMap<>();
            winnings.put(players.get(1), 500.0); // player3
            winnings.put(players.get(4), 0.0);  // player4
            result.setWinnings(winnings);
            gameResultRepository.save(result);
            System.out.println("✅ Recorded game results: " + players.get(1).getUser().getUsername() + " won $500");
            
            // 7. Record player statistics
            for (Player player : players) {
                Statistics stats = new Statistics();
                stats.setUser(player.getUser()); // Change this depending on your Statistics model
                stats.setGamesPlayed(new Random().nextInt(20) + 1);
                stats.setGamesWon(new Random().nextInt(stats.getGamesPlayed()));
                stats.setTotalWinnings((double) (new Random().nextInt(5000)));
                stats.setWinRate((double) stats.getGamesWon() / stats.getGamesPlayed());
                statisticsRepository.save(stats);
            }
            System.out.println("✅ Generated player statistics");
            
            // 8. Create invitations
            Invitation invitation = new Invitation();
            invitation.setSender(players.get(2).getUser());
            invitation.setRecipient(users.get(3)); // Adjust based on your model - recipient may be User or Player
            invitation.setTableId(privateTable.getId());
            invitation.setMessage("Join my exclusive table for a high-stakes game!");
            invitation.setStatus(InvitationStatus.PENDING);
            invitationRepository.save(invitation);
            System.out.println("✅ Created invitation to private table");
            
            System.out.println("\n🚀 Test environment initialization complete!");
            System.out.println("- Users created: " + (users.size() + 2));
            System.out.println("- Players created: " + players.size());
            System.out.println("- Tables created: 3");
            System.out.println("- Games created: 2");
            System.out.println("- Statistics generated for " + players.size() + " players");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to create a Player for a User with composition
    private Player createPlayerForUser(User user) {
        // Create a new Player linked to this user
        Player player = new Player();
        player.setUser(user);
        player.setChips(0.0);
        player.setStatus(PlayerStatus.SITTING_OUT);
        player.setHand(new Hand());
        return playerRepository.save(player);
    }
}