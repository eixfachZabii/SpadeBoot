package com.pokerapp;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Suit;
import com.pokerapp.domain.card.c_Rank;
import com.pokerapp.domain.poker.HandEvaluator;
import com.pokerapp.domain.poker.HandRank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HandEvaluatorTest {

    private HandEvaluator handEvaluator;

    @BeforeEach
    public void setUp() {
        handEvaluator = new HandEvaluator();
    }

    private Card createCard(Suit suit, c_Rank rank) {
        Card card = new Card();
        card.setSuit(suit);
        card.setCRank(rank);
        return card;
    }

    @Test
    public void testRoyalFlush() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.HEARTS, c_Rank.JACK));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.NINE));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.EIGHT));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.ROYAL_FLUSH, handRank);
    }

    @Test
    public void testRoyalFlushEdgeCase() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.ACE));
        playerCards.add(createCard(Suit.HEARTS, c_Rank.KING));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.JACK));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.ACE));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.ACE));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.ROYAL_FLUSH, handRank);
    }

    @Test
    public void testRoyalFlushInvalid() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.ACE));
        playerCards.add(createCard(Suit.HEARTS, c_Rank.KING));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.JACK));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.TEN));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.NINE));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.EIGHT));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertNotEquals(HandRank.ROYAL_FLUSH, handRank);
    }

    @Test
    public void testRoyalFlushMissingCard() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.HEARTS, c_Rank.JACK));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
        communityCards.add(createCard(Suit.SPADES, c_Rank.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.NINE));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.EIGHT));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertNotEquals(HandRank.ROYAL_FLUSH, handRank);
    }

    @Test
    public void testStraightFlush() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.NINE));
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, c_Rank.JACK));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.ACE));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.ACE));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.STRAIGHT_FLUSH, handRank);
    }

    @Test
    public void testStraightFlushWithAceLow() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.ACE));
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TWO));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, c_Rank.THREE));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.FOUR));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.FIVE));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.SIX));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.SEVEN));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.STRAIGHT_FLUSH, handRank);
    }

    @Test
    public void testStraightFlushInvalid() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.NINE));
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, c_Rank.JACK));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.ACE));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.ACE));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertNotEquals(HandRank.STRAIGHT_FLUSH, handRank);
    }

    @Test
    public void testFourOfAKind() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, c_Rank.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.TEN));
        communityCards.add(createCard(Suit.SPADES, c_Rank.TEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.KING));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.QUEEN));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.FOUR_OF_A_KIND, handRank);
    }

    @Test
    public void testFourOfAKindWithPair() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, c_Rank.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.TEN));
        communityCards.add(createCard(Suit.SPADES, c_Rank.TEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.ACE));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.QUEEN));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.FOUR_OF_A_KIND, handRank);
    }

    @Test
    public void testFourOfAKindInvalid() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, c_Rank.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.NINE));
        communityCards.add(createCard(Suit.SPADES, c_Rank.TEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.KING));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.QUEEN));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertNotEquals(HandRank.FOUR_OF_A_KIND, handRank);
    }

    @Test
    public void testFullHouse() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, c_Rank.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.TEN));
        communityCards.add(createCard(Suit.SPADES, c_Rank.NINE));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.NINE));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.ACE));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.QUEEN));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.FULL_HOUSE, handRank);
    }

    @Test
    public void testFullHouseInvalid() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, c_Rank.NINE));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.NINE));
        communityCards.add(createCard(Suit.SPADES, c_Rank.TEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.KING));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.QUEEN));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertNotEquals(HandRank.FULL_HOUSE, handRank);
    }

    @Test
    public void testFlush() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.HEARTS, c_Rank.JACK));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.NINE));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.EIGHT));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.FLUSH, handRank);
    }

    @Test
    public void testFlushWithStraight() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.HEARTS, c_Rank.JACK));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.NINE));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.TWO));
        communityCards.add(createCard(Suit.SPADES, c_Rank.ACE));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.FLUSH, handRank);
    }

    @Test
    public void testFlushInvalid() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, c_Rank.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.JACK));
        communityCards.add(createCard(Suit.SPADES, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.NINE));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.EIGHT));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertNotEquals(HandRank.FLUSH, handRank);
    }

    @Test
    public void testStraight() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.NINE));
        playerCards.add(createCard(Suit.DIAMONDS, c_Rank.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.JACK));
        communityCards.add(createCard(Suit.SPADES, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.TWO));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.EIGHT));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.STRAIGHT, handRank);
    }

    @Test
    public void testStraightWithAceHigh() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.HEARTS, c_Rank.ACE));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.SPADES, c_Rank.KING));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.TWO));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.NINE));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.JACK));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.STRAIGHT, handRank);
    }

    @Test
    public void testStraightHigh() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.HEARTS, c_Rank.JACK));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.ACE));
        communityCards.add(createCard(Suit.SPADES, c_Rank.NINE));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.EIGHT));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.STRAIGHT, handRank);
    }

    @Test
    public void testThreeOfAKind() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, c_Rank.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.TEN));
        communityCards.add(createCard(Suit.SPADES, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.TWO));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.EIGHT));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.THREE_OF_A_KIND, handRank);
    }

    @Test
    public void testTwoPair() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, c_Rank.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.NINE));
        communityCards.add(createCard(Suit.SPADES, c_Rank.NINE));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.TWO));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.EIGHT));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.TWO_PAIR, handRank);
    }

    @Test
    public void testPair() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, c_Rank.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.NINE));
        communityCards.add(createCard(Suit.SPADES, c_Rank.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.KING));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.TWO));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.EIGHT));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.PAIR, handRank);
    }

    @Test
    public void testHighCard() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, c_Rank.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, c_Rank.QUEEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, c_Rank.TWO));
        communityCards.add(createCard(Suit.SPADES, c_Rank.FOUR));
        communityCards.add(createCard(Suit.HEARTS, c_Rank.SIX));
        communityCards.add(createCard(Suit.DIAMONDS, c_Rank.EIGHT));
        communityCards.add(createCard(Suit.CLUBS, c_Rank.KING));

        HandRank handRank = handEvaluator.evaluateBestHand(playerCards, communityCards).getHandRank();
        assertEquals(HandRank.HIGH_CARD, handRank);
    }
}