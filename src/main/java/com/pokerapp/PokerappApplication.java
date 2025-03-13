// src/main/java/com/pokerapp/PokerApplication.java
package com.pokerapp;

import com.pokerapp.domain.card.Hand;
import com.pokerapp.domain.game.*;
import com.pokerapp.domain.invitation.Invitation;
import com.pokerapp.domain.invitation.InvitationStatus;
import com.pokerapp.domain.replay.GameAction;
import com.pokerapp.domain.replay.Replay;
import com.pokerapp.domain.statistics.GameResult;
import com.pokerapp.domain.statistics.Statistics;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.Spectator;
import com.pokerapp.domain.user.User;
import com.pokerapp.domain.user.UserType;
import com.pokerapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

@SpringBootApplication
public class PokerappApplication {

//    @Autowired
//    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(PokerappApplication.class, args);
    }

    @Bean
    public CommandLineRunner setupTestData(
            UserRepository userRepository,
            PlayerRepository playerRepository,
            SpectatorRepository spectatorRepository,
            TableRepository tableRepository,
            GameRepository gameRepository,
            ReplayRepository replayRepository,
            GameResultRepository gameResultRepository,
            StatisticsRepository statisticsRepository,
            InvitationRepository invitationRepository
    ) {
        return args -> {
            // Only run if user repository is empty (first run)
            if (userRepository.count() == 0) {
                System.out.println("Initializing test data...");

                // Create users
                List<User> users = createTestUsers(userRepository);

                // Create players
                List<Player> players = createTestPlayers(playerRepository);

                // Create spectators
                List<Spectator> spectators = createTestSpectators(spectatorRepository);

                // Create tables
                List<PokerTable> tables = createTestTables(tableRepository, players);

                // Create games
                List<Game> games = createTestGames(gameRepository, tables, players);

                // Create replays
                List<Replay> replays = createTestReplays(replayRepository, games, players);

                // Create game results
                List<GameResult> results = createTestGameResults(gameResultRepository, games, players);

                // Create statistics
                createTestStatistics(statisticsRepository, players, results);

                // Create invitations
                createTestInvitations(invitationRepository, users, players, tables);

                System.out.println("Test data initialization complete!");
            }
        };
    }

    private List<User> createTestUsers(UserRepository userRepository) {
        return null;
    }

    private List<Player> createTestPlayers(PlayerRepository playerRepository) {
        List<Player> players = new ArrayList<>();

        // Create 5 players
        for (int i = 1; i <= 5; i++) {
            Player player = new Player();
            player.setUsername("player" + i);
            player.setEmail("player" + i + "@example.com");
            player.setPassword("password");
            player.setBalance(500.0 * i);
            player.setChips(0.0);
            player.setUserType(UserType.PLAYER);
            player.addRole("USER");
            player.addRole("PLAYER");

            // Create a hand for the player
            Hand hand = new Hand();
            player.setHand(hand);

            players.add(playerRepository.save(player));
        }

        return players;
    }

    private List<Spectator> createTestSpectators(SpectatorRepository spectatorRepository) {
        List<Spectator> spectators = new ArrayList<>();

        // Create 3 spectators
        for (int i = 1; i <= 3; i++) {
            Spectator spectator = new Spectator();
            spectator.setUsername("spectator" + i);
            spectator.setEmail("spectator" + i + "@example.com");
            spectator.setPassword("password");
            spectator.setBalance(100.0);
            spectator.setUserType(UserType.SPECTATOR);
            spectator.addRole("USER");
            spectator.addRole("SPECTATOR");

            spectators.add(spectatorRepository.save(spectator));
        }

        return spectators;
    }

    private List<PokerTable> createTestTables(TableRepository tableRepository, List<Player> players) {
        List<PokerTable> tables = new ArrayList<>();

        // Create 3 tables with different settings
        PokerTable table1 = new PokerTable();
        table1.setName("Beginner's Table");
        table1.setDescription("Low stakes, perfect for beginners");
        table1.setMaxPlayers(6);
        table1.setMinBuyIn(10.0);
        table1.setMaxBuyIn(100.0);
        table1.setPrivate(false);
        table1.setOwner(players.get(0));
        table1.addPlayer(players.get(0), 50.0);
        if (players.size() > 1) {
            table1.addPlayer(players.get(1), 75.0);
        }
        tables.add(tableRepository.save(table1));

        PokerTable table2 = new PokerTable();
        table2.setName("Pro Table");
        table2.setDescription("High stakes for experienced players");
        table2.setMaxPlayers(8);
        table2.setMinBuyIn(100.0);
        table2.setMaxBuyIn(1000.0);
        table2.setPrivate(false);
        table2.setOwner(players.get(2));
        table2.addPlayer(players.get(2), 500.0);
        if (players.size() > 3) {
            table2.addPlayer(players.get(3), 750.0);
        }
        tables.add(tableRepository.save(table2));

        PokerTable table3 = new PokerTable();
        table3.setName("Private Game");
        table3.setDescription("Invitation only");
        table3.setMaxPlayers(4);
        table3.setMinBuyIn(50.0);
        table3.setMaxBuyIn(500.0);
        table3.setPrivate(true);
        table3.setOwner(players.get(4));
        table3.addPlayer(players.get(4), 250.0);
        tables.add(tableRepository.save(table3));

        return tables;
    }

    private List<Game> createTestGames(GameRepository gameRepository, List<PokerTable> tables, List<Player> players) {
        List<Game> games = new ArrayList<>();

        // Create a game for the first table
        if (!tables.isEmpty() && tables.get(0).getPlayers().size() >= 2) {
            Game game = new Game();
            game.setPokerTable(tables.get(0));
            game.setSmallBlind(tables.get(0).getMinBuyIn() / 100);
            game.setBigBlind(tables.get(0).getMinBuyIn() / 50);
            game.setStatus(GameStatus.IN_PROGRESS);
            game.setDealerPosition(0);

            // Set as current game
            tables.get(0).setCurrentGame(game);

            games.add(gameRepository.save(game));
        }

        // Create a finished game for the second table
        if (tables.size() > 1) {
            Game game = new Game();
            game.setPokerTable(tables.get(1));
            game.setSmallBlind(tables.get(1).getMinBuyIn() / 100);
            game.setBigBlind(tables.get(1).getMinBuyIn() / 50);
            game.setStatus(GameStatus.FINISHED);
            game.setDealerPosition(0);

            games.add(gameRepository.save(game));
        }

        return games;
    }

    private List<Replay> createTestReplays(ReplayRepository replayRepository, List<Game> games, List<Player> players) {
        List<Replay> replays = new ArrayList<>();

        // Create replay for each game
        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);

            Replay replay = new Replay();
            replay.setGame(game);
            replay.setStartTime(LocalDateTime.now().minusHours(i + 1));

            // Add some actions to the replay
            if (i == 0) { // For the in-progress game
                for (int j = 0; j < Math.min(3, players.size()); j++) {
                    GameAction action = new GameAction();
                    action.setPlayer(players.get(j));
                    action.setActionType("MOVE");
                    action.setActionData(j % 2 == 0 ? "CHECK:0.0" : "RAISE:10.0");
                    action.setSequenceNumber(j + 1);

                    replay.recordAction(action);
                }
            } else { // For the finished game
                for (int j = 0; j < Math.min(5, players.size()); j++) {
                    GameAction action = new GameAction();
                    action.setPlayer(players.get(j % players.size()));

                    if (j < 3) {
                        action.setActionType("MOVE");
                        action.setActionData(j == 0 ? "CALL:10.0" : j == 1 ? "RAISE:20.0" : "ALL_IN:100.0");
                    } else {
                        action.setActionType("MOVE");
                        action.setActionData(j == 3 ? "FOLD:0.0" : "CALL:100.0");
                    }

                    action.setSequenceNumber(j + 1);
                    replay.recordAction(action);
                }

                // Mark the replay as complete
                replay.setEndTime(LocalDateTime.now().minusMinutes(30));
            }

            replays.add(replayRepository.save(replay));
        }

        return replays;
    }

    private List<GameResult> createTestGameResults(GameResultRepository gameResultRepository, List<Game> games, List<Player> players) {
        List<GameResult> results = new ArrayList<>();

        // Create results for finished games
        for (Game game : games) {
            if (game.getStatus() == GameStatus.FINISHED) {
                GameResult result = new GameResult();
                result.setGame(game);

                // Distribute winnings
                Player winner = players.get(0); // First player wins
                result.getWinnings().put(winner, 150.0);

                // Record losses for other players
                for (int i = 1; i < Math.min(3, players.size()); i++) {
                    result.getWinnings().put(players.get(i), 0.0);
                }

                results.add(gameResultRepository.save(result));
            }
        }

        return results;
    }

    private void createTestStatistics(StatisticsRepository statisticsRepository, List<Player> players, List<GameResult> results) {
        // Create statistics for players
        for (Player player : players) {
            Statistics statistics = new Statistics();
            statistics.setUser(player);
            statistics.setGamesPlayed(1 + new Random().nextInt(10));
            statistics.setGamesWon(new Random().nextInt(statistics.getGamesPlayed() + 1));
            statistics.setTotalWinnings((double) (new Random().nextInt(1000)));
            statistics.setWinRate((double) statistics.getGamesWon() / statistics.getGamesPlayed());

            statisticsRepository.save(statistics);
        }
    }

    private void createTestInvitations(InvitationRepository invitationRepository, List<User> users, List<Player> players, List<PokerTable> tables) {
        // Create some test invitations
        if (!tables.isEmpty() && tables.size() >= 2 && !users.isEmpty() && !players.isEmpty()) {
            // Pending invitation
            Invitation invitation1 = new Invitation();
            invitation1.setSender(players.get(0));
            invitation1.setRecipient(users.get(0));
            invitation1.setTableId(tables.get(0).getId());
            invitation1.setMessage("Join my table for a friendly game!");
            invitation1.setStatus(InvitationStatus.PENDING);
            invitationRepository.save(invitation1);

            // Accepted invitation
            Invitation invitation2 = new Invitation();
            invitation2.setSender(players.get(1));
            invitation2.setRecipient(users.get(1));
            invitation2.setTableId(tables.get(1).getId());
            invitation2.setMessage("Let's play some high stakes poker!");
            invitation2.setStatus(InvitationStatus.ACCEPTED);
            invitation2.setCreatedAt(LocalDateTime.now().minusDays(1));
            invitationRepository.save(invitation2);

            // Declined invitation
            Invitation invitation3 = new Invitation();
            invitation3.setSender(players.get(2));
            invitation3.setRecipient(users.get(2));
            invitation3.setTableId(tables.get(2).getId());
            invitation3.setMessage("Private game, only the best players!");
            invitation3.setStatus(InvitationStatus.DECLINED);
            invitation3.setCreatedAt(LocalDateTime.now().minusDays(2));
            invitationRepository.save(invitation3);
            System.out.println("PIMMMEL");
        }
    }
}