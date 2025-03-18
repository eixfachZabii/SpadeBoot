package com.pokerapp.service.impl.logic;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Deck;
import com.pokerapp.domain.game.*;
import com.pokerapp.domain.poker.HandEvaluator;
import com.pokerapp.domain.poker.HandRank;
import com.pokerapp.domain.poker.HandResult;
import com.pokerapp.domain.poker.WinnerDeterminer;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.*;
import com.pokerapp.service.HandEvaluationService;
import com.pokerapp.service.ReplayService;
import com.pokerapp.service.StatisticsService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GameRoundService {
    private final GameRepository gameRepository;
    private final GameRoundRepository gameRoundRepository;
    private final PlayerRepository playerRepository;
    private final BettingRoundRepository bettingRoundRepository;
    private final HandEvaluationService handEvaluationService;
    private final ReplayService replayService;
    private final StatisticsService statisticsService;
    private final SimpMessagingTemplate messagingTemplate;
    private final BettingRoundService bettingRoundService;
    private final HandEvaluator handEvaluator;
    private final TaskScheduler taskScheduler;

    private static final Logger logger = LoggerFactory.getLogger(GameRoundService.class);



    @Autowired
    public GameRoundService(
            GameRepository gameRepository,
            GameRoundRepository gameRoundRepository,
            PlayerRepository playerRepository,
            BettingRoundRepository bettingRoundRepository,
            HandEvaluationService handEvaluationService,
            ReplayService replayService,
            StatisticsService statisticsService,
            SimpMessagingTemplate messagingTemplate,
            BettingRoundService bettingRoundService,
            HandEvaluator handEvaluator, TaskScheduler taskScheduler) {
        this.gameRepository = gameRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.playerRepository = playerRepository;
        this.bettingRoundRepository = bettingRoundRepository;
        this.handEvaluationService = handEvaluationService;
        this.replayService = replayService;
        this.statisticsService = statisticsService;
        this.messagingTemplate = messagingTemplate;
        this.bettingRoundService = bettingRoundService;
        this.handEvaluator = handEvaluator;
        this.taskScheduler = taskScheduler;
    }

    /**
     * Creates a new game round for a specific game
     */
    @Transactional
    public GameRound createGameRound(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with ID: " + gameId));

        GameRound gameRound = new GameRound();
        game.getGameRounds().add(gameRound);
        gameRound.setGame(game);
        gameRound.setRoundNumber(game.getGameRounds().size());
        gameRound.setPot(0.0);

        return gameRoundRepository.save(gameRound);
    }

    /**
     * Starts a complete game round with all its stages
     */
    @Transactional
    public void startGameRound(Long gameRoundId) {
        GameRound gameRound = gameRoundRepository.findById(gameRoundId)
                .orElseThrow(() -> new NotFoundException("GameRound not found with ID: " + gameRoundId));

        //initialize and shuffle deck
        gameRound.getGame().getDeck().initialize();

        // Deal private cards to players
        dealPrivateCards(gameRound);

        // Play through betting rounds
        playPreFlopBettingRound(gameRound);

        // If only one player remains, end the round
        if (isOnlyOnePlayerRemaining(gameRound)) {
            declareWinnerWithLastRemainingPlayer(gameRound);
            return;
        }

        // Deal and play flop round
        dealCommunityCards(gameRound, 3);
        playFlopBettingRound(gameRound);

        // If only one player remains, end the round
        if (isOnlyOnePlayerRemaining(gameRound)) {
            declareWinnerWithLastRemainingPlayer(gameRound);
            return;
        }

        // Deal and play turn round
        dealCommunityCards(gameRound, 1);
        playTurnBettingRound(gameRound);

        // If only one player remains, end the round
        if (isOnlyOnePlayerRemaining(gameRound)) {
            declareWinnerWithLastRemainingPlayer(gameRound);
            return;
        }

        // Deal and play river round
        dealCommunityCards(gameRound, 1);
        playRiverBettingRound(gameRound);

        // If only one player remains, end the round
        if (isOnlyOnePlayerRemaining(gameRound)) {
            declareWinnerWithLastRemainingPlayer(gameRound);
            return;
        }

        // Showdown if multiple players remain
        determineWinners(gameRound);


        Game game = gameRound.getGame();

        // Only automatically start next round if we're not in manual mode
        if (game.isManualMode() && game.getStatus() == GameStatus.IN_PROGRESS) {
            // Schedule the next round to start after a short delay (e.g., 5 seconds)
            // This gives players time to see results of the current round
            scheduleNextRound(game.getId());
        }
    }

    private void scheduleNextRound(Long gameId) {
        // Use a task scheduler like Spring's TaskScheduler
        // Or create a new Thread with a sleep - though TaskScheduler is preferred
        taskScheduler.schedule(
                () -> {
                    try {
                        startGameRound(gameId);
                    } catch (Exception e) {
                        logger.error("Error starting next round", e);
                    }
                },
                Instant.now().plusSeconds(10)
        );
    }

    /**
     * Deals private cards to players
     */
    private void dealPrivateCards(GameRound gameRound) {
        Game game = gameRound.getGame();
        Deck deck = game.getDeck();
        deck.shuffle();

        for (Player player : game.getPlayers()) {
            // Clear previous hand
            player.getHand().clear();

            // Deal two cards to each active player
            player.getHand().addCard(deck.drawCard());
            player.getHand().addCard(deck.drawCard());
        }
    }

    /**
     * Plays pre-flop betting round
     */
    private void playPreFlopBettingRound(GameRound gameRound) {
        BettingRound preFlopRound = bettingRoundService.createBettingRound(gameRound.getId(), BettingStage.PREFLOP);
        bettingRoundService.playBettingRound(preFlopRound.getId());
    }

    /**
     * Deals community cards
     */
    private void dealCommunityCards(GameRound gameRound, int count) {
        Game game = gameRound.getGame();
        Deck deck = game.getDeck();

        for (int i = 0; i < count; i++) {
            Card card = deck.drawCard();
            gameRound.getCommunityCards().add(card);
        }
    }

    /**
     * Plays flop betting round
     */
    private void playFlopBettingRound(GameRound gameRound) {
        BettingRound flopRound = bettingRoundService.createBettingRound(gameRound.getId(), BettingStage.FLOP);
        bettingRoundService.playBettingRound(flopRound.getId());
    }

    /**
     * Plays turn betting round
     */
    private void playTurnBettingRound(GameRound gameRound) {
        BettingRound turnRound = bettingRoundService.createBettingRound(gameRound.getId(), BettingStage.TURN);
        bettingRoundService.playBettingRound(turnRound.getId());
    }

    /**
     * Plays river betting round
     */
    private void playRiverBettingRound(GameRound gameRound) {
        BettingRound riverRound = bettingRoundService.createBettingRound(gameRound.getId(), BettingStage.RIVER);
        bettingRoundService.playBettingRound(riverRound.getId());
    }

    /**
     * Checks if only one player remains
     */
    private boolean isOnlyOnePlayerRemaining(GameRound gameRound) {
        return gameRound.getGame().getPlayers().stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .count() == 1;
    }

    /**
     * Declares winner when only one player remains
     */
    private void declareWinnerWithLastRemainingPlayer(GameRound gameRound) {
        Player winner = gameRound.getGame().getPlayers().stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active player found"));

        // Winner gets the entire pot
        winner.setChips(winner.getChips() + gameRound.getPot());

        // Log or notify about the winner
        messagingTemplate.convertAndSend("/topic/game/" + gameRound.getGame().getId() + "/winner",
                Map.of("winner", winner.getUsername(), "pot", gameRound.getPot()));
    }

    /**
     * Determines winners in case of showdown
     */
    private void determineWinners(GameRound gameRound) {
        // Get active players with their hands
        List<Player> activePlayers = gameRound.getGame().getPlayers().stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .toList();

        List<Card> communityCards = gameRound.getCommunityCards();

        // Evaluate hand ranks for each player
        Map<String, List<Card>> playerHands = new HashMap<>();
        for (Player player : activePlayers) {
            List<Card> cards = player.getHand().getCards();
            playerHands.put(player.getUsername(), cards);
        }

        // Determine winner(s)
        WinnerDeterminer winnerDeterminer = new WinnerDeterminer(communityCards);
        List<WinnerDeterminer.WinnerResult> winners = winnerDeterminer.determineWinners(playerHands);
        // Distribute pot
        //distributePot(gameRound, winners);
    }

    /**
     * Finds winners based on hand ranks
     */

    /**
     * Distributes pot to winner(s)
     */
    private void distributePot(GameRound gameRound, List<Player> winners) {
        double potAmount = gameRound.getPot();
        double winnerShare = potAmount / winners.size();

        for (Player winner : winners) {
            winner.setChips(winner.getChips() + winnerShare);
        }

        // Log or notify about winners
        messagingTemplate.convertAndSend("/topic/game/" + gameRound.getGame().getId() + "/winners",
                winners.stream().map(Player::getUsername).collect(Collectors.toList()));
    }
}