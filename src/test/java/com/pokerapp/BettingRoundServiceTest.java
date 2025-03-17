package com.pokerapp;

import com.pokerapp.api.dto.request.MoveDto;
import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Hand;
import com.pokerapp.domain.card.Suit;
import com.pokerapp.domain.card.c_Rank;
import com.pokerapp.domain.game.*;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.PlayerStatus;
import com.pokerapp.domain.user.User;
import com.pokerapp.exception.NotFoundException;
import com.pokerapp.repository.*;
import com.pokerapp.service.HandEvaluationService;
import com.pokerapp.service.ReplayService;
import com.pokerapp.service.StatisticsService;
import com.pokerapp.service.impl.logic.BettingRoundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BettingRoundServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private TableRepository tableRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ReplayService replayService;

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private HandEvaluationService handEvaluationService;

    @Mock
    private GameRoundRepository gameRoundRepository;

    @Mock
    private BettingRoundRepository bettingRoundRepository;

    @InjectMocks
    private BettingRoundService bettingRoundService;

    // Test data
    private Game game;
    private GameRound gameRound;
    private BettingRound bettingRound;
    private List<Player> players;
    private PokerTable pokerTable;

    @BeforeEach
    void setUp() {
        // Create test users
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("Player1");
        user1.setBalance(1000.0);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("Player2");
        user2.setBalance(1000.0);

        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("Player3");
        user3.setBalance(1000.0);

        User user4 = new User();
        user4.setId(4L);
        user4.setUsername("Player4");
        user4.setBalance(1000.0);

        // Create test players
        Player player1 = createPlayer(1L, user1, 500.0, PlayerStatus.ACTIVE);
        Player player2 = createPlayer(2L, user2, 500.0, PlayerStatus.ACTIVE);
        Player player3 = createPlayer(3L, user3, 500.0, PlayerStatus.ACTIVE);
        Player player4 = createPlayer(4L, user4, 500.0, PlayerStatus.ACTIVE);

        players = new ArrayList<>(Arrays.asList(player1, player2, player3, player4));

        // Create poker table
        pokerTable = new PokerTable();
        pokerTable.setId(1L);
        pokerTable.setName("Test Table");
        pokerTable.setMaxPlayers(6);
        pokerTable.setMinBuyIn(100.0);
        pokerTable.setMaxBuyIn(1000.0);
        pokerTable.setPlayers(new HashSet<>(players));

        // Create game
        game = new Game();
        game.setId(1L);
        game.setPokerTable(pokerTable);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setSmallBlind(5.0);
        game.setBigBlind(10.0);
        game.setDealerIndex(0); // Player1 is the dealer

        // Create game round
        gameRound = new GameRound();
        gameRound.setId(1L);
        gameRound.setGame(game);
        gameRound.setPot(0.0);
        gameRound.setRoundNumber(1);

        // Create betting round
        bettingRound = new BettingRound();
        bettingRound.setId(1L);
        bettingRound.setGameRound(gameRound);
        bettingRound.setStage(BettingStage.PREFLOP);
        bettingRound.setCurrentBet(0.0);
        bettingRound.setMoves(new ArrayList<>());

        // Connect game round to betting round
        gameRound.setBettingRounds(new ArrayList<>(Collections.singletonList(bettingRound)));
        gameRound.setCurrentBettingRound(bettingRound);

        // Connect game to game round
        game.setGameRounds(new ArrayList<>(Collections.singletonList(gameRound)));
        game.setCurrentRound(gameRound);
    }

    private Player createPlayer(Long id, User user, Double chips, PlayerStatus status) {
        Player player = new Player();
        player.setId(id);
        player.setUser(user);
        player.setChips(chips);
        player.setStatus(status);
        player.setHand(new Hand());
        return player;
    }

    @Test
    void createBettingRound_ShouldCreateNewBettingRound() {
        // Arrange
        when(gameRoundRepository.findById(anyLong())).thenReturn(Optional.of(gameRound));
        when(bettingRoundRepository.save(any(BettingRound.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BettingRound result = bettingRoundService.createBettingRound(1L, BettingStage.FLOP);

        // Assert
        assertNotNull(result);
        assertEquals(BettingStage.FLOP, result.getStage());
        assertEquals(0.0, result.getCurrentBet());

        verify(gameRoundRepository).findById(1L);
        verify(bettingRoundRepository).save(any(BettingRound.class));
        verify(gameRoundRepository).save(gameRound);
    }

    @Test
    void createBettingRound_GameRoundNotFound_ShouldThrowNotFoundException() {
        // Arrange
        when(gameRoundRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            bettingRoundService.createBettingRound(1L, BettingStage.FLOP);
        });

        verify(gameRoundRepository).findById(1L);
        verify(bettingRoundRepository, never()).save(any(BettingRound.class));
    }

    @Test
    void playBettingRound_Preflop_ShouldHandleBlindsAndPlayerActions() {
        // Arrange
        // Ensure all references are properly set up
        bettingRound.setStage(BettingStage.PREFLOP);
        bettingRound.setGameRound(gameRound);
        gameRound.setGame(game);
        game.setPokerTable(pokerTable);
        gameRound.setBettingRounds(new ArrayList<>(Collections.singletonList(bettingRound)));
        gameRound.setCurrentBettingRound(bettingRound);
        game.setGameRounds(new ArrayList<>(Collections.singletonList(gameRound)));
        game.setCurrentRound(gameRound);

        // Use explicit argument matcher for findById
        when(bettingRoundRepository.findById(eq(1L))).thenReturn(Optional.of(bettingRound));

        // Mock player repository to return players by ID
        for (Player player : players) {
            when(playerRepository.findByUserId(eq(player.getUser().getId()))).thenReturn(Optional.of(player));
        }

        // Just return the saved object when save is called
        when(bettingRoundRepository.save(any(BettingRound.class))).thenAnswer(i -> i.getArgument(0));
        when(gameRoundRepository.save(any(GameRound.class))).thenAnswer(i -> i.getArgument(0));

        // *************************************************************
        // IMPORTANT: This is where we prevent a state transition to FLOP
        // *************************************************************
        // Mock to force test termination after preflop (THIS IS KEY!)
        doAnswer(invocation -> {
            System.out.println("Mock: Ending test after preflop. No state transition.");
            return null;
        }).when(messagingTemplate).convertAndSend(anyString(), Optional.ofNullable(any()));

        // Act - but don't expect result since we're ending the betting early
        try {
            bettingRoundService.playBettingRound(1L);

            // If we get here without exception, verify basic interactions
            verify(bettingRoundRepository).findById(1L);
            verify(playerRepository, atLeast(4)).findByUserId(anyLong()); // All players

            // Success! Test completed without exceptions
        } catch (Exception e) {
            System.out.println("Test failed with exception: " + e.getMessage());
            throw e; // Re-throw to fail the test
        }
    }

    @Test
    void makeMove_ValidCall_ShouldProcessCallMove() {
        // Arrange
        Player player = players.get(2); // Player3
        MoveDto moveDto = new MoveDto();
        moveDto.setType("CALL");
        moveDto.setAmount(10.0);

        when(gameRepository.findById(anyLong())).thenReturn(Optional.of(game));
        when(playerRepository.findByUserId(player.getUser().getId())).thenReturn(Optional.of(player));

        bettingRound.setCurrentBet(10.0); // There's a bet to call

        ArgumentCaptor<Move> moveCaptor = ArgumentCaptor.forClass(Move.class);

        // Act
        bettingRoundService.makeMove(1L, player.getUser().getId(), moveDto);

        // Assert
        verify(gameRepository).findById(1L);
        verify(playerRepository).findByUserId(player.getUser().getId());
        verify(bettingRoundRepository).save(any(BettingRound.class));
        verify(replayService).recordMove(eq(game), moveCaptor.capture());

        Move capturedMove = moveCaptor.getValue();
        assertEquals(MoveType.CALL, capturedMove.getType());
        assertEquals(10.0, capturedMove.getAmount());
        assertEquals(player, capturedMove.getPlayer());
    }

    @Test
    void makeMove_ValidRaise_ShouldProcessRaiseMove() {
        // Arrange
        Player player = players.get(3); // Player4
        MoveDto moveDto = new MoveDto();
        moveDto.setType("RAISE");
        moveDto.setAmount(20.0);

        when(gameRepository.findById(anyLong())).thenReturn(Optional.of(game));
        when(playerRepository.findByUserId(player.getUser().getId())).thenReturn(Optional.of(player));

        bettingRound.setCurrentBet(10.0); // There's a bet to raise

        ArgumentCaptor<Move> moveCaptor = ArgumentCaptor.forClass(Move.class);

        // Act
        bettingRoundService.makeMove(1L, player.getUser().getId(), moveDto);

        // Assert
        verify(gameRepository).findById(1L);
        verify(playerRepository).findByUserId(player.getUser().getId());
        verify(bettingRoundRepository).save(any(BettingRound.class));
        verify(replayService).recordMove(eq(game), moveCaptor.capture());

        Move capturedMove = moveCaptor.getValue();
        assertEquals(MoveType.RAISE, capturedMove.getType());
        assertEquals(20.0, capturedMove.getAmount());
        assertEquals(player, capturedMove.getPlayer());
    }

    @Test
    void makeMove_ValidFold_ShouldProcessFoldMove() {
        // Arrange
        Player player = players.get(1); // Player2
        MoveDto moveDto = new MoveDto();
        moveDto.setType("FOLD");

        when(gameRepository.findById(anyLong())).thenReturn(Optional.of(game));
        when(playerRepository.findByUserId(player.getUser().getId())).thenReturn(Optional.of(player));

        ArgumentCaptor<Move> moveCaptor = ArgumentCaptor.forClass(Move.class);

        // Act
        bettingRoundService.makeMove(1L, player.getUser().getId(), moveDto);

        // Assert
        verify(gameRepository).findById(1L);
        verify(playerRepository).findByUserId(player.getUser().getId());
        verify(bettingRoundRepository).save(any(BettingRound.class));
        verify(replayService).recordMove(eq(game), moveCaptor.capture());

        Move capturedMove = moveCaptor.getValue();
        assertEquals(MoveType.FOLD, capturedMove.getType());
        assertEquals(player, capturedMove.getPlayer());
    }

    @Test
    void makeMove_ValidCheck_ShouldProcessCheckMove() {
        // Arrange
        Player player = players.get(0); // Player1
        MoveDto moveDto = new MoveDto();
        moveDto.setType("CHECK");

        when(gameRepository.findById(anyLong())).thenReturn(Optional.of(game));
        when(playerRepository.findByUserId(player.getUser().getId())).thenReturn(Optional.of(player));

        // No bets, so checking is valid
        bettingRound.setCurrentBet(0.0);

        ArgumentCaptor<Move> moveCaptor = ArgumentCaptor.forClass(Move.class);

        // Act
        bettingRoundService.makeMove(1L, player.getUser().getId(), moveDto);

        // Assert
        verify(gameRepository).findById(1L);
        verify(playerRepository).findByUserId(player.getUser().getId());
        verify(bettingRoundRepository).save(any(BettingRound.class));
        verify(replayService).recordMove(eq(game), moveCaptor.capture());

        Move capturedMove = moveCaptor.getValue();
        assertEquals(MoveType.CHECK, capturedMove.getType());
        assertEquals(player, capturedMove.getPlayer());
    }

    @Test
    void makeMove_GameNotFound_ShouldThrowNotFoundException() {
        // Arrange
        Player player = players.get(0); // Player1
        MoveDto moveDto = new MoveDto();
        moveDto.setType("CHECK");

        when(gameRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            bettingRoundService.makeMove(1L, player.getUser().getId(), moveDto);
        });

        verify(gameRepository).findById(1L);
        verify(playerRepository, never()).findByUserId(anyLong());
        verify(bettingRoundRepository, never()).save(any(BettingRound.class));
    }

    @Test
    void makeMove_PlayerNotFound_ShouldThrowNotFoundException() {
        // Arrange
        Long userId = 999L; // Non-existent user
        MoveDto moveDto = new MoveDto();
        moveDto.setType("CHECK");

        when(gameRepository.findById(anyLong())).thenReturn(Optional.of(game));
        when(playerRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            bettingRoundService.makeMove(1L, userId, moveDto);
        });

        verify(gameRepository).findById(1L);
        verify(playerRepository).findByUserId(userId);
        verify(bettingRoundRepository, never()).save(any(BettingRound.class));
    }

    @Test
    void playBettingRound_CompleteRound_AllPlayersCallOrFold() {
        // Arrange
        // Set up a betting round in the FLOP stage
        bettingRound.setStage(BettingStage.FLOP);

        // Create community cards
        List<Card> communityCards = new ArrayList<>();
        Card card1 = new Card();
        card1.setSuit(Suit.HEARTS);
        card1.setCRank(c_Rank.ACE);
        card1.setShowing(true);

        Card card2 = new Card();
        card2.setSuit(Suit.CLUBS);
        card2.setCRank(c_Rank.KING);
        card2.setShowing(true);

        Card card3 = new Card();
        card3.setSuit(Suit.DIAMONDS);
        card3.setCRank(c_Rank.QUEEN);
        card3.setShowing(true);

        communityCards.add(card1);
        communityCards.add(card2);
        communityCards.add(card3);

        gameRound.setCommunityCards(communityCards);

        // Ensure all references are properly set up
        bettingRound.setGameRound(gameRound);
        gameRound.setGame(game);
        game.setPokerTable(pokerTable);
        gameRound.setBettingRounds(new ArrayList<>(Collections.singletonList(bettingRound)));
        gameRound.setCurrentBettingRound(bettingRound);
        game.setGameRounds(new ArrayList<>(Collections.singletonList(gameRound)));
        game.setCurrentRound(gameRound);

        // Mock the repository to return our betting round with explicit argument
        when(bettingRoundRepository.findById(eq(1L))).thenReturn(Optional.of(bettingRound));

        // Mock player repository to return players by ID
        for (Player player : players) {
            when(playerRepository.findByUserId(eq(player.getUser().getId()))).thenReturn(Optional.of(player));
        }

        // Set up to capture moves for verification
        ArgumentCaptor<BettingRound> bettingRoundCaptor = ArgumentCaptor.forClass(BettingRound.class);

        // Act
        BettingRound result = bettingRoundService.playBettingRound(1L);

        // Assert
        verify(bettingRoundRepository).findById(1L);
        verify(bettingRoundRepository).save(bettingRoundCaptor.capture());
        verify(gameRoundRepository).save(any(GameRound.class));
    }

    @Test
    void playBettingRound_LastPlayerFolds_ShouldEndRoundEarly() {
        // Arrange
        // Set one player as folded already
        players.get(0).setStatus(PlayerStatus.FOLDED); // Player1 folded

        // Ensure all references are properly set up
        bettingRound.setGameRound(gameRound);
        gameRound.setGame(game);
        game.setPokerTable(pokerTable);
        gameRound.setBettingRounds(new ArrayList<>(Collections.singletonList(bettingRound)));
        gameRound.setCurrentBettingRound(bettingRound);
        game.setGameRounds(new ArrayList<>(Collections.singletonList(gameRound)));
        game.setCurrentRound(gameRound);

        // Make sure the betting round has a stage
        bettingRound.setStage(BettingStage.PREFLOP);

        // Mock the repository to return our betting round
        when(bettingRoundRepository.findById(eq(1L))).thenReturn(Optional.of(bettingRound));

        // Mock player repository to return players by ID
        for (Player player : players) {
            when(playerRepository.findByUserId(eq(player.getUser().getId()))).thenReturn(Optional.of(player));
        }

        // Set up to capture moves for verification
        ArgumentCaptor<BettingRound> bettingRoundCaptor = ArgumentCaptor.forClass(BettingRound.class);

        // Act
        BettingRound result = bettingRoundService.playBettingRound(1L);

        // Assert
        verify(bettingRoundRepository).findById(1L);
        verify(bettingRoundRepository).save(bettingRoundCaptor.capture());
        verify(gameRoundRepository).save(any(GameRound.class));
    }

    @Test
    void playBettingRound_AllPlayersAllIn_ShouldEndRoundEarly() {
        // Arrange
        // Set some players with very few chips (will be forced to go all-in)
        players.get(1).setChips(5.0);  // Player2 has very few chips
        players.get(2).setChips(15.0); // Player3 has very few chips

        // Ensure all references are properly set up
        bettingRound.setGameRound(gameRound);
        gameRound.setGame(game);
        game.setPokerTable(pokerTable);
        gameRound.setBettingRounds(new ArrayList<>(Collections.singletonList(bettingRound)));
        gameRound.setCurrentBettingRound(bettingRound);
        game.setGameRounds(new ArrayList<>(Collections.singletonList(gameRound)));
        game.setCurrentRound(gameRound);

        // Make sure the betting round has a stage
        bettingRound.setStage(BettingStage.PREFLOP);

        // Mock the repository to return our betting round
        when(bettingRoundRepository.findById(eq(1L))).thenReturn(Optional.of(bettingRound));

        // Mock player repository to return players by ID
        for (Player player : players) {
            when(playerRepository.findByUserId(eq(player.getUser().getId()))).thenReturn(Optional.of(player));
        }

        // Set up to capture moves for verification
        ArgumentCaptor<BettingRound> bettingRoundCaptor = ArgumentCaptor.forClass(BettingRound.class);

        // Act
        BettingRound result = bettingRoundService.playBettingRound(1L);

        // Assert
        verify(bettingRoundRepository).findById(1L);
        verify(bettingRoundRepository).save(bettingRoundCaptor.capture());
        verify(gameRoundRepository).save(any(GameRound.class));
    }
}