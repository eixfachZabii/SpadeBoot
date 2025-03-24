package com.pokerapp;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Hand;
import com.pokerapp.domain.card.Suit;
import com.pokerapp.domain.card.Value;
import com.pokerapp.domain.game.GameRound;
import com.pokerapp.domain.poker.HandEvaluator;
import com.pokerapp.domain.poker.HandRank;
import com.pokerapp.domain.poker.HandResult;
import com.pokerapp.domain.poker.WinnerDeterminer;
import com.pokerapp.domain.user.Player;
import com.pokerapp.domain.user.User;
import com.pokerapp.service.HandEvaluationService;
import com.pokerapp.service.impl.logic.HandEvaluationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HandEvaluationServiceTest {

    private HandEvaluationService handEvaluationService;

    @Mock
    private HandEvaluator handEvaluator;

    @Mock
    private GameRound gameRound;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        handEvaluationService = new HandEvaluationServiceImpl(handEvaluator);
    }

    private Card createCard(Suit suit, Value rank) {
        Card card = new Card();
        card.setSuit(suit);
        card.setCRank(rank);
        return card;
    }

    private Player createPlayer(String name) {
        User user = new User();
        user.setId((long) name.hashCode());
        user.setUsername(name);

        Player player = new Player();
        player.setId((long) name.hashCode());
        player.setUser(user);
        player.setHand(new Hand());

        return player;
    }

    @Test
    public void testRoyalFlushVsFourOfAKind() {
        // Create players
        Player alice = createPlayer("Alice");
        Player bob = createPlayer("Bob");

        // Alice's cards - Royal Flush
        alice.getHand().addCard(createCard(Suit.SPADES, Value.ACE));
        alice.getHand().addCard(createCard(Suit.SPADES, Value.KING));

        // Bob's cards - Four of a Kind
        bob.getHand().addCard(createCard(Suit.HEARTS, Value.FOUR));
        bob.getHand().addCard(createCard(Suit.CLUBS, Value.FOUR));

        // Community cards
        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.SPADES, Value.QUEEN));
        communityCards.add(createCard(Suit.SPADES, Value.JACK));
        communityCards.add(createCard(Suit.SPADES, Value.TEN));
        communityCards.add(createCard(Suit.DIAMONDS, Value.FOUR));
        communityCards.add(createCard(Suit.SPADES, Value.FOUR));

        // Mock hand results
        HandResult aliceHandResult = new HandResult(HandRank.ROYAL_FLUSH,
                List.of(
                        createCard(Suit.SPADES, Value.ACE),
                        createCard(Suit.SPADES, Value.KING),
                        createCard(Suit.SPADES, Value.QUEEN),
                        createCard(Suit.SPADES, Value.JACK),
                        createCard(Suit.SPADES, Value.TEN)
                ));

        HandResult bobHandResult = new HandResult(HandRank.FOUR_OF_A_KIND,
                List.of(
                        createCard(Suit.DIAMONDS, Value.FOUR),
                        createCard(Suit.SPADES, Value.FOUR),
                        createCard(Suit.HEARTS, Value.FOUR),
                        createCard(Suit.CLUBS, Value.FOUR),
                        createCard(Suit.DIAMONDS, Value.ACE)
                ));

        // Mock hand evaluations
        when(handEvaluator.evaluateBestHand(alice.getHand().getCards(), communityCards))
                .thenReturn(aliceHandResult);
        when(handEvaluator.evaluateBestHand(bob.getHand().getCards(), communityCards))
                .thenReturn(bobHandResult);

        // Use the new WinnerDeterminer approach
        WinnerDeterminer winnerDeterminer = new WinnerDeterminer(communityCards);
        Map<String, List<Card>> playerCardsMap = new HashMap<>();
        playerCardsMap.put("alice", alice.getHand().getCards());
        playerCardsMap.put("bob", bob.getHand().getCards());

        List<WinnerDeterminer.WinnerResult> winners = winnerDeterminer.determineWinners(playerCardsMap);

        // Verify Alice wins
        assertEquals(1, winners.size());
        assertEquals("alice", winners.get(0).getPlayerId());
        assertEquals(HandRank.ROYAL_FLUSH, winners.get(0).getHandResult().getHandRank());
    }


//
//
//    public void testFullHouseSplitPot() {
//        // Create players
//        Player alice = createPlayer("Alice");
//        Player bob = createPlayer("Bob");
//
//        // Alice's cards - Full House
//        alice.getHand().addCard(createCard(Suit.SPADES, c_Rank.THREE));
//        alice.getHand().addCard(createCard(Suit.HEARTS, c_Rank.THREE));
//
//        // Bob's cards - Full House (same strength)
//        bob.getHand().addCard(createCard(Suit.DIAMONDS, c_Rank.THREE));
//        bob.getHand().addCard(createCard(Suit.CLUBS, c_Rank.THREE));
//
//        // Community cards
//        List<Card> communityCards = new ArrayList<>();
//        communityCards.add(createCard(Suit.SPADES, c_Rank.KING));
//        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
//        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.KING));
//        communityCards.add(createCard(Suit.CLUBS, c_Rank.TWO));
//        communityCards.add(createCard(Suit.SPADES, c_Rank.FOUR));
//
//        // Set up players in gameRound
//        Set<Player> players = new HashSet<>();
//        players.add(alice);
//        players.add(bob);
//
//        when(gameRound.getPlayers()).thenReturn(players);
//        when(gameRound.getCommunityCards()).thenReturn(communityCards);
//        when(gameRound.getPot()).thenReturn(100.0);
//
//        // Mock hand evaluations
//        when(handEvaluator.evaluateBestHand(alice.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.FULL_HOUSE);
//        when(handEvaluator.evaluateBestHand(bob.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.FULL_HOUSE);
//
//        // Create a mock ranking for compareHands - both players tied for 1st
//        Map<Player, Integer> rankings = new HashMap<>();
//        rankings.put(alice, 1);
//        rankings.put(bob, 1);
//
//        when(handEvaluator.compareHands(anyMap(), eq(communityCards))).thenReturn(rankings);
//
//        // Execute test
//        Map<Player, Double> winnings = handEvaluationService.determineWinners(gameRound);
//
//        // Verify pot is split evenly
//        assertEquals(50.0, winnings.get(alice));
//        assertEquals(50.0, winnings.get(bob));
//    }
//
//
//    public void testStraightFlushVsFlush() {
//        // Create players
//        Player alice = createPlayer("Alice");
//        Player bob = createPlayer("Bob");
//
//        // Alice's cards - Straight Flush
//        alice.getHand().addCard(createCard(Suit.CLUBS, c_Rank.SIX));
//        alice.getHand().addCard(createCard(Suit.CLUBS, c_Rank.FIVE));
//
//        // Bob's cards - Flush
//        bob.getHand().addCard(createCard(Suit.HEARTS, c_Rank.KING));
//        bob.getHand().addCard(createCard(Suit.HEARTS, c_Rank.NINE));
//
//        // Community cards
//        List<Card> communityCards = new ArrayList<>();
//        communityCards.add(createCard(Suit.CLUBS, c_Rank.SEVEN));
//        communityCards.add(createCard(Suit.CLUBS, c_Rank.EIGHT));
//        communityCards.add(createCard(Suit.CLUBS, c_Rank.NINE));
//        communityCards.add(createCard(Suit.HEARTS, c_Rank.TWO));
//        communityCards.add(createCard(Suit.HEARTS, c_Rank.THREE));
//
//        // Set up players in gameRound
//        Set<Player> players = new HashSet<>();
//        players.add(alice);
//        players.add(bob);
//
//        when(gameRound.getPlayers()).thenReturn(players);
//        when(gameRound.getCommunityCards()).thenReturn(communityCards);
//        when(gameRound.getPot()).thenReturn(100.0);
//
//        // Mock hand evaluations
//        when(handEvaluator.evaluateBestHand(alice.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.STRAIGHT_FLUSH);
//        when(handEvaluator.evaluateBestHand(bob.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.FLUSH);
//
//        // Create a mock ranking for compareHands
//        Map<Player, Integer> rankings = new HashMap<>();
//        rankings.put(alice, 1); // Alice has the best hand
//        rankings.put(bob, 2);   // Bob has the second-best hand
//
//        when(handEvaluator.compareHands(anyMap(), eq(communityCards))).thenReturn(rankings);
//
//        // Execute test
//        Map<Player, Double> winnings = handEvaluationService.determineWinners(gameRound);
//
//        // Verify Alice wins all
//        assertEquals(100.0, winnings.get(alice));
//        assertEquals(0.0, winnings.get(bob));
//    }
//
//
//    public void testHighCardSplitPot() {
//        // Create players
//        Player alice = createPlayer("Alice");
//        Player bob = createPlayer("Bob");
//
//        // Alice's cards - High Card Ace
//        alice.getHand().addCard(createCard(Suit.HEARTS, c_Rank.ACE));
//        alice.getHand().addCard(createCard(Suit.CLUBS, c_Rank.TWO));
//
//        // Bob's cards - High Card Ace
//        bob.getHand().addCard(createCard(Suit.SPADES, c_Rank.ACE));
//        bob.getHand().addCard(createCard(Suit.DIAMONDS, c_Rank.THREE));
//
//        // Community cards
//        List<Card> communityCards = new ArrayList<>();
//        communityCards.add(createCard(Suit.HEARTS, c_Rank.FOUR));
//        communityCards.add(createCard(Suit.CLUBS, c_Rank.FIVE));
//        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.SIX));
//        communityCards.add(createCard(Suit.CLUBS, c_Rank.SEVEN));
//        communityCards.add(createCard(Suit.HEARTS, c_Rank.EIGHT));
//
//        // Set up players in gameRound
//        Set<Player> players = new HashSet<>();
//        players.add(alice);
//        players.add(bob);
//
//        when(gameRound.getPlayers()).thenReturn(players);
//        when(gameRound.getCommunityCards()).thenReturn(communityCards);
//        when(gameRound.getPot()).thenReturn(100.0);
//
//        // Mock hand evaluations
//        when(handEvaluator.evaluateBestHand(alice.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.HIGH_CARD);
//        when(handEvaluator.evaluateBestHand(bob.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.HIGH_CARD);
//
//        // Create a mock ranking for compareHands - both players tied for 1st
//        Map<Player, Integer> rankings = new HashMap<>();
//        rankings.put(alice, 1);
//        rankings.put(bob, 1);
//
//        when(handEvaluator.compareHands(anyMap(), eq(communityCards))).thenReturn(rankings);
//
//        // Execute test
//        Map<Player, Double> winnings = handEvaluationService.determineWinners(gameRound);
//
//        // Verify pot is split evenly
//        assertEquals(50.0, winnings.get(alice));
//        assertEquals(50.0, winnings.get(bob));
//    }
//
//
//    public void testFlushVsTwoPair() {
//        // Create players
//        Player alice = createPlayer("Alice");
//        Player bob = createPlayer("Bob");
//
//        // Alice's cards - Flush
//        alice.getHand().addCard(createCard(Suit.HEARTS, c_Rank.ACE));
//        alice.getHand().addCard(createCard(Suit.HEARTS, c_Rank.FIVE));
//
//        // Bob's cards - Two Pair
//        bob.getHand().addCard(createCard(Suit.SPADES, c_Rank.TEN));
//        bob.getHand().addCard(createCard(Suit.CLUBS, c_Rank.TEN));
//
//        // Community cards
//        List<Card> communityCards = new ArrayList<>();
//        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
//        communityCards.add(createCard(Suit.HEARTS, c_Rank.QUEEN));
//        communityCards.add(createCard(Suit.HEARTS, c_Rank.JACK));
//        communityCards.add(createCard(Suit.CLUBS, c_Rank.TWO));
//        communityCards.add(createCard(Suit.SPADES, c_Rank.THREE));
//
//        // Set up players in gameRound
//        Set<Player> players = new HashSet<>();
//        players.add(alice);
//        players.add(bob);
//
//        when(gameRound.getPlayers()).thenReturn(players);
//        when(gameRound.getCommunityCards()).thenReturn(communityCards);
//        when(gameRound.getPot()).thenReturn(100.0);
//
//        // Mock hand evaluations
//        when(handEvaluator.evaluateBestHand(alice.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.FLUSH);
//        when(handEvaluator.evaluateBestHand(bob.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.TWO_PAIR);
//
//        // Create a mock ranking for compareHands
//        Map<Player, Integer> rankings = new HashMap<>();
//        rankings.put(alice, 1); // Alice has the best hand
//        rankings.put(bob, 2);   // Bob has the second-best hand
//
//        when(handEvaluator.compareHands(anyMap(), eq(communityCards))).thenReturn(rankings);
//
//        // Execute test
//        Map<Player, Double> winnings = handEvaluationService.determineWinners(gameRound);
//
//        // Verify Alice wins all
//        assertEquals(100.0, winnings.get(alice));
//        assertEquals(0.0, winnings.get(bob));
//    }
//
//
//    public void testThreeOfAKindVsTwoPair() {
//        // Create players
//        Player alice = createPlayer("Alice");
//        Player bob = createPlayer("Bob");
//
//        // Alice's cards - Three of a Kind
//        alice.getHand().addCard(createCard(Suit.HEARTS, c_Rank.QUEEN));
//        alice.getHand().addCard(createCard(Suit.DIAMONDS, c_Rank.QUEEN));
//
//        // Bob's cards - Two Pair
//        bob.getHand().addCard(createCard(Suit.SPADES, c_Rank.KING));
//        bob.getHand().addCard(createCard(Suit.CLUBS, c_Rank.KING));
//
//        // Community cards
//        List<Card> communityCards = new ArrayList<>();
//        communityCards.add(createCard(Suit.SPADES, c_Rank.QUEEN));
//        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.TEN));
//        communityCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
//        communityCards.add(createCard(Suit.CLUBS, c_Rank.TWO));
//        communityCards.add(createCard(Suit.SPADES, c_Rank.THREE));
//
//        // Set up players in gameRound
//        Set<Player> players = new HashSet<>();
//        players.add(alice);
//        players.add(bob);
//
//        when(gameRound.getPlayers()).thenReturn(players);
//        when(gameRound.getCommunityCards()).thenReturn(communityCards);
//        when(gameRound.getPot()).thenReturn(100.0);
//
//        // Mock hand evaluations
//        when(handEvaluator.evaluateBestHand(alice.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.THREE_OF_A_KIND);
//        when(handEvaluator.evaluateBestHand(bob.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.TWO_PAIR);
//
//        // Create a mock ranking for compareHands
//        Map<Player, Integer> rankings = new HashMap<>();
//        rankings.put(alice, 1); // Alice has the best hand
//        rankings.put(bob, 2);   // Bob has the second-best hand
//
//        when(handEvaluator.compareHands(anyMap(), eq(communityCards))).thenReturn(rankings);
//
//        // Execute test
//        Map<Player, Double> winnings = handEvaluationService.determineWinners(gameRound);
//
//        // Verify Alice wins all
//        assertEquals(100.0, winnings.get(alice));
//        assertEquals(0.0, winnings.get(bob));
//    }
//
//
//    public void testFourWaySplitPotWithStraight() {
//        // Create players
//        Player alice = createPlayer("Alice");
//        Player bob = createPlayer("Bob");
//        Player charlie = createPlayer("Charlie");
//        Player diana = createPlayer("Diana");
//
//        // Each player's cards don't improve the board straight
//        alice.getHand().addCard(createCard(Suit.SPADES, c_Rank.TWO));
//        alice.getHand().addCard(createCard(Suit.HEARTS, c_Rank.THREE));
//
//        bob.getHand().addCard(createCard(Suit.CLUBS, c_Rank.TWO));
//        bob.getHand().addCard(createCard(Suit.DIAMONDS, c_Rank.THREE));
//
//        charlie.getHand().addCard(createCard(Suit.SPADES, c_Rank.FOUR));
//        charlie.getHand().addCard(createCard(Suit.CLUBS, c_Rank.FOUR));
//
//        diana.getHand().addCard(createCard(Suit.CLUBS, c_Rank.SIX));
//        diana.getHand().addCard(createCard(Suit.DIAMONDS, c_Rank.SIX));
//
//        // Community cards - straight on the board
//        List<Card> communityCards = new ArrayList<>();
//        communityCards.add(createCard(Suit.SPADES, c_Rank.FIVE));
//        communityCards.add(createCard(Suit.HEARTS, c_Rank.SIX));
//        communityCards.add(createCard(Suit.CLUBS, c_Rank.SEVEN));
//        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.EIGHT));
//        communityCards.add(createCard(Suit.HEARTS, c_Rank.NINE));
//
//        // Set up players in gameRound
//        Set<Player> players = new HashSet<>();
//        players.add(alice);
//        players.add(bob);
//        players.add(charlie);
//        players.add(diana);
//
//        when(gameRound.getPlayers()).thenReturn(players);
//        when(gameRound.getCommunityCards()).thenReturn(communityCards);
//        when(gameRound.getPot()).thenReturn(100.0);
//
//        // All players have a straight
//        when(handEvaluator.evaluateBestHand(alice.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.STRAIGHT);
//        when(handEvaluator.evaluateBestHand(bob.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.STRAIGHT);
//        when(handEvaluator.evaluateBestHand(charlie.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.STRAIGHT);
//        when(handEvaluator.evaluateBestHand(diana.getHand().getCards(), communityCards).getHandRank())
//                .thenReturn(HandRank.STRAIGHT);
//
//        // Create a mock ranking for compareHands - all players tied
//        Map<Player, Integer> rankings = new HashMap<>();
//        rankings.put(alice, 1);
//        rankings.put(bob, 1);
//        rankings.put(charlie, 1);
//        rankings.put(diana, 1);
//
//        when(handEvaluator.compareHands(anyMap(), eq(communityCards))).thenReturn(rankings);
//
//        // Execute test
//        Map<Player, Double> winnings = handEvaluationService.determineWinners(gameRound);
//
//        // Verify pot is split evenly among all 4 players
//        assertEquals(25.0, winnings.get(alice));
//        assertEquals(25.0, winnings.get(bob));
//        assertEquals(25.0, winnings.get(charlie));
//        assertEquals(25.0, winnings.get(diana));
//    }
}