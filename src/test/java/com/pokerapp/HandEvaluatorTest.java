package com.pokerapp;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Suit;
import com.pokerapp.domain.card.Value;
import com.pokerapp.domain.game.HandEvaluation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HandEvaluatorTest {

    private HandEvaluation handEvaluator;

    @BeforeEach
    public void setUp() {
        handEvaluator = new HandEvaluation();
    }

    private Card createCard(Suit suit, Value rank) {
        Card card = new Card();
        card.setSuit(suit);
        card.setValue(rank);
        return card;
    }

    @Test
    public void testRoyalFlush() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.HEARTS, Value.JACK));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.KING));
        communityCards.add(createCard(Suit.HEARTS, Value.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, Value.NINE));
        communityCards.add(createCard(Suit.CLUBS, Value.EIGHT));

    }

    @Test
    public void testRoyalFlushEdgeCase() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.ACE));
        playerCards.add(createCard(Suit.HEARTS, Value.KING));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.JACK));
        communityCards.add(createCard(Suit.HEARTS, Value.TEN));
        communityCards.add(createCard(Suit.DIAMONDS, Value.ACE));
        communityCards.add(createCard(Suit.CLUBS, Value.ACE));

    }

    @Test
    public void testRoyalFlushInvalid() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.ACE));
        playerCards.add(createCard(Suit.HEARTS, Value.KING));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.JACK));
        communityCards.add(createCard(Suit.DIAMONDS, Value.TEN));
        communityCards.add(createCard(Suit.DIAMONDS, Value.NINE));
        communityCards.add(createCard(Suit.DIAMONDS, Value.EIGHT));


    }

    @Test
    public void testRoyalFlushMissingCard() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.HEARTS, Value.JACK));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.KING));
        communityCards.add(createCard(Suit.SPADES, Value.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, Value.NINE));
        communityCards.add(createCard(Suit.CLUBS, Value.EIGHT));

    }

    @Test
    public void testStraightFlush() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.NINE));
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, Value.JACK));
        communityCards.add(createCard(Suit.HEARTS, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.KING));
        communityCards.add(createCard(Suit.DIAMONDS, Value.ACE));
        communityCards.add(createCard(Suit.CLUBS, Value.ACE));

    }

    @Test
    public void testStraightFlushWithAceLow() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.ACE));
        playerCards.add(createCard(Suit.HEARTS, Value.TWO));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, Value.THREE));
        communityCards.add(createCard(Suit.HEARTS, Value.FOUR));
        communityCards.add(createCard(Suit.HEARTS, Value.FIVE));
        communityCards.add(createCard(Suit.DIAMONDS, Value.SIX));
        communityCards.add(createCard(Suit.CLUBS, Value.SEVEN));


    }

    @Test
    public void testStraightFlushInvalid() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.NINE));
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, Value.JACK));
        communityCards.add(createCard(Suit.DIAMONDS, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.KING));
        communityCards.add(createCard(Suit.DIAMONDS, Value.ACE));
        communityCards.add(createCard(Suit.CLUBS, Value.ACE));


    }

    @Test
    public void testFourOfAKind() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, Value.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.TEN));
        communityCards.add(createCard(Suit.SPADES, Value.TEN));
        communityCards.add(createCard(Suit.HEARTS, Value.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, Value.KING));
        communityCards.add(createCard(Suit.CLUBS, Value.QUEEN));


    }

    @Test
    public void testFourOfAKindWithPair() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, Value.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.TEN));
        communityCards.add(createCard(Suit.SPADES, Value.TEN));
        communityCards.add(createCard(Suit.HEARTS, Value.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, Value.ACE));
        communityCards.add(createCard(Suit.CLUBS, Value.QUEEN));


    }

    @Test
    public void testFourOfAKindInvalid() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, Value.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.NINE));
        communityCards.add(createCard(Suit.SPADES, Value.TEN));
        communityCards.add(createCard(Suit.HEARTS, Value.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, Value.KING));
        communityCards.add(createCard(Suit.CLUBS, Value.QUEEN));

    }

    @Test
    public void testFullHouse() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, Value.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.TEN));
        communityCards.add(createCard(Suit.SPADES, Value.NINE));
        communityCards.add(createCard(Suit.HEARTS, Value.NINE));
        communityCards.add(createCard(Suit.DIAMONDS, Value.ACE));
        communityCards.add(createCard(Suit.CLUBS, Value.QUEEN));


    }

    @Test
    public void testFullHouseInvalid() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, Value.NINE));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.NINE));
        communityCards.add(createCard(Suit.SPADES, Value.TEN));
        communityCards.add(createCard(Suit.HEARTS, Value.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, Value.KING));
        communityCards.add(createCard(Suit.CLUBS, Value.QUEEN));

    }

    @Test
    public void testFlush() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.HEARTS, Value.JACK));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.KING));
        communityCards.add(createCard(Suit.HEARTS, Value.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, Value.NINE));
        communityCards.add(createCard(Suit.HEARTS, Value.EIGHT));

    }

    @Test
    public void testFlushWithStraight() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.HEARTS, Value.JACK));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.KING));
        communityCards.add(createCard(Suit.DIAMONDS, Value.NINE));
        communityCards.add(createCard(Suit.HEARTS, Value.TWO));
        communityCards.add(createCard(Suit.SPADES, Value.ACE));


    }

    @Test
    public void testFlushInvalid() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, Value.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.JACK));
        communityCards.add(createCard(Suit.SPADES, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.ACE));
        communityCards.add(createCard(Suit.DIAMONDS, Value.NINE));
        communityCards.add(createCard(Suit.CLUBS, Value.EIGHT));


    }

    @Test
    public void testStraight() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.NINE));
        playerCards.add(createCard(Suit.DIAMONDS, Value.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.JACK));
        communityCards.add(createCard(Suit.SPADES, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.KING));
        communityCards.add(createCard(Suit.DIAMONDS, Value.TWO));
        communityCards.add(createCard(Suit.CLUBS, Value.EIGHT));


    }

    @Test
    public void testStraightWithAceHigh() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.HEARTS, Value.ACE));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.QUEEN));
        communityCards.add(createCard(Suit.SPADES, Value.KING));
        communityCards.add(createCard(Suit.DIAMONDS, Value.TWO));
        communityCards.add(createCard(Suit.DIAMONDS, Value.NINE));
        communityCards.add(createCard(Suit.CLUBS, Value.JACK));


    }

    @Test
    public void testStraightHigh() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.HEARTS, Value.JACK));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.HEARTS, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.KING));
        communityCards.add(createCard(Suit.DIAMONDS, Value.ACE));
        communityCards.add(createCard(Suit.SPADES, Value.NINE));
        communityCards.add(createCard(Suit.CLUBS, Value.EIGHT));


    }

    @Test
    public void testThreeOfAKind() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, Value.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.TEN));
        communityCards.add(createCard(Suit.SPADES, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.KING));
        communityCards.add(createCard(Suit.DIAMONDS, Value.TWO));
        communityCards.add(createCard(Suit.CLUBS, Value.EIGHT));


    }

    @Test
    public void testTwoPair() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, Value.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.NINE));
        communityCards.add(createCard(Suit.SPADES, Value.NINE));
        communityCards.add(createCard(Suit.HEARTS, Value.KING));
        communityCards.add(createCard(Suit.DIAMONDS, Value.TWO));
        communityCards.add(createCard(Suit.CLUBS, Value.EIGHT));


    }

    @Test
    public void testPair() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, Value.TEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.NINE));
        communityCards.add(createCard(Suit.SPADES, Value.QUEEN));
        communityCards.add(createCard(Suit.HEARTS, Value.KING));
        communityCards.add(createCard(Suit.DIAMONDS, Value.TWO));
        communityCards.add(createCard(Suit.CLUBS, Value.EIGHT));


    }

    @Test
    public void testHighCard() {
        List<Card> playerCards = new ArrayList<>();
        playerCards.add(createCard(Suit.HEARTS, Value.TEN));
        playerCards.add(createCard(Suit.DIAMONDS, Value.QUEEN));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(createCard(Suit.CLUBS, Value.TWO));
        communityCards.add(createCard(Suit.SPADES, Value.FOUR));
        communityCards.add(createCard(Suit.HEARTS, Value.SIX));
        communityCards.add(createCard(Suit.DIAMONDS, Value.EIGHT));
        communityCards.add(createCard(Suit.CLUBS, Value.KING));


    }
}