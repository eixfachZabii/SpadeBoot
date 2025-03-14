// src/main/java/com/pokerapp/PokerApplication.java
package com.pokerapp;

import com.pokerapp.api.dto.request.RegisterDto;
import com.pokerapp.domain.card.Hand;
import com.pokerapp.domain.game.Game;
import com.pokerapp.domain.game.GameStatus;
import com.pokerapp.domain.invitation.Invitation;
import com.pokerapp.domain.invitation.InvitationStatus;
import com.pokerapp.domain.statistics.GameResult;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
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
import com.pokerapp.domain.game.Table;
import com.pokerapp.domain.invitation.Invitation;
import com.pokerapp.domain.invitation.InvitationStatus;
import com.pokerapp.domain.statistics.GameResult;
import com.pokerapp.domain.statistics.Statistics;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import com.pokerapp.domain.user.UserType;
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
    
    // 1. Create multiple users with different roles
    System.out.println("📝 Creating test users...");
    
    // Admin user
    RegisterDto adminUser = new RegisterDto();
    adminUser.setUsername("THOMAS_NEUMANN");
    adminUser.setPassword("ICHBINGDBBOSS");
    adminUser.setEmail("THOMAS.NEUMANN@TUM.de");
    User admin = userService.register(adminUser);
    admin.addRole("ADMIN");
    userRepository.save(admin);
    System.out.println("✅ Admin user created: " + admin.getUsername());
    
    // Regular players
    String[] playerNames = {"Poker_Pro", "CardShark", "RiverRunner", "BluffMaster", "AllInAndy"};
    List<User> players = new ArrayList<>();
    
    for (int i = 0; i < playerNames.length; i++) {
        RegisterDto playerDto = new RegisterDto();
        playerDto.setUsername(playerNames[i]);
        playerDto.setPassword("password" + i);
        playerDto.setEmail(playerNames[i].toLowerCase() + "@poker.com");
        User player = userService.register(playerDto);
        
        // Add different starting balances
        userService.updateBalance(player.getId(), 100.0 * (i + 1));
        players.add(player);
        System.out.println("✅ Player created: " + player.getUsername() + " with balance $" + player.getBalance());
    }
    
    // 2. Create spectators
    RegisterDto spectatorDto = new RegisterDto();
    spectatorDto.setUsername("Observer");
    spectatorDto.setPassword("watchOnly");
    spectatorDto.setEmail("observer@poker.com");
    User spectator = userService.register(spectatorDto);
    System.out.println("✅ Spectator created: " + spectator.getUsername());
    
    try {
        // 3. Create poker tables with different settings
        System.out.println("\n🎲 Creating poker tables...");
        
        // Beginner table
        Table beginnerTable = new Table();
        beginnerTable.setName("Beginner's Table");
        beginnerTable.setDescription("Low stakes, perfect for beginners");
        beginnerTable.setMaxPlayers(6);
        beginnerTable.setMinBuyIn(10.0);
        beginnerTable.setMaxBuyIn(100.0);
        beginnerTable.setIsPrivate(false);
        beginnerTable.setOwner(convertToPlayer(players.get(0)));
        tableRepository.save(beginnerTable);
        System.out.println("✅ Created table: " + beginnerTable.getName());
        
        // Pro table
        Table proTable = new Table();
        proTable.setName("High Rollers");
        proTable.setDescription("High stakes for experienced players");
        proTable.setMaxPlayers(8);
        proTable.setMinBuyIn(500.0);
        proTable.setMaxBuyIn(5000.0);
        proTable.setIsPrivate(false);
        proTable.setOwner(convertToPlayer(players.get(1)));
        tableRepository.save(proTable);
        System.out.println("✅ Created table: " + proTable.getName());
        
        // Private table
        Table privateTable = new Table();
        privateTable.setName("VIP Room");
        privateTable.setDescription("Invitation only");
        privateTable.setMaxPlayers(4);
        privateTable.setMinBuyIn(200.0);
        privateTable.setMaxBuyIn(1000.0);
        privateTable.setIsPrivate(true);
        privateTable.setOwner(convertToPlayer(players.get(2)));
        tableRepository.save(privateTable);
        System.out.println("✅ Created table: " + privateTable.getName());
        
        // 4. Add players to tables
        System.out.println("\n🧑‍🤝‍🧑 Adding players to tables...");
        
        // Add players to beginner table
        Player player1 = convertToPlayer(players.get(0));
        Player player2 = convertToPlayer(players.get(3));
        beginnerTable.addPlayer(player1, 50.0);
        beginnerTable.addPlayer(player2, 75.0);
        tableRepository.save(beginnerTable);
        System.out.println("✅ Added players to " + beginnerTable.getName());
        
        // Add players to pro table
        Player player3 = convertToPlayer(players.get(1));
        Player player4 = convertToPlayer(players.get(4));
        proTable.addPlayer(player3, 1000.0);
        proTable.addPlayer(player4, 2000.0);
        tableRepository.save(proTable);
        System.out.println("✅ Added players to " + proTable.getName());
        
        // 5. Create and start games
        System.out.println("\n🃏 Creating poker games...");
        
        // Create a game for the beginner table
        Game beginnerGame = new Game();
        beginnerGame.setTable(beginnerTable);
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
        finishedGame.setTable(proTable);
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
        winnings.put(player3, 500.0);
        winnings.put(player4, 0.0);
        result.setWinnings(winnings);
        gameResultRepository.save(result);
        System.out.println("✅ Recorded game results: " + player3.getUsername() + " won $500");
        
        // 7. Record player statistics
        for (User userPlayer : players) {
            Player player = convertToPlayer(userPlayer);
            Statistics stats = new Statistics();
            stats.setUser(player);
            stats.setGamesPlayed(new Random().nextInt(20) + 1);
            stats.setGamesWon(new Random().nextInt(stats.getGamesPlayed()));
            stats.setTotalWinnings((double) (new Random().nextInt(5000)));
            stats.setWinRate((double) stats.getGamesWon() / stats.getGamesPlayed());
            statisticsRepository.save(stats);
        }
        System.out.println("✅ Generated player statistics");
        
        // 8. Create invitations
        Invitation invitation = new Invitation();
        invitation.setSender(convertToPlayer(players.get(2)));
        invitation.setRecipient(players.get(3));
        invitation.setTableId(privateTable.getId());
        invitation.setMessage("Join my exclusive table for a high-stakes game!");
        invitation.setStatus(InvitationStatus.PENDING);
        invitationRepository.save(invitation);
        System.out.println("✅ Created invitation to private table");
        
        System.out.println("\n🚀 Test environment initialization complete!");
        System.out.println("- Users created: " + (players.size() + 2));
        System.out.println("- Tables created: 3");
        System.out.println("- Games created: 2");
        System.out.println("- Statistics generated for " + players.size() + " players");
        
    } catch (Exception e) {
        System.err.println("❌ Error initializing test data: " + e.getMessage());
        e.printStackTrace();
    }
}

// Helper method to convert User to Player
private Player convertToPlayer(User user) {
    // Check if already a Player
    if (user instanceof Player) {
        return (Player) user;
    }
    
    // Otherwise create a new Player
    Player player = new Player();
    player.setId(user.getId());
    player.setUsername(user.getUsername());
    player.setEmail(user.getEmail());
    player.setPassword(user.getPassword());
    player.setBalance(user.getBalance());
    player.setRoles(user.getRoles());
    player.setUserType(UserType.PLAYER);
    player.addRole("PLAYER");
    player.setHand(new Hand());
    
    return playerRepository.save(player);
}

}