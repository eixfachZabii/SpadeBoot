package com.pokerapp.domain.poker;

import com.pokerapp.domain.card.Card;
import com.pokerapp.domain.card.Suit;
import com.pokerapp.domain.card.c_Rank;

import java.util.*;

/**
 * Example class demonstrating how to use the poker hand evaluation and winner determination classes.
 */
public class PokerGameExample {

    public static void main(String[] args) {
        // Create some cards for demonstration
        Card aceHearts = createCard(c_Rank.ACE, Suit.HEARTS);
        Card kingHearts = createCard(c_Rank.KING, Suit.HEARTS);
        Card queenHearts = createCard(c_Rank.QUEEN, Suit.HEARTS);
        Card jackHearts = createCard(c_Rank.JACK, Suit.HEARTS);
        Card tenHearts = createCard(c_Rank.TEN, Suit.HEARTS);

        Card aceSpades = createCard(c_Rank.ACE, Suit.SPADES);
        Card kingSpades = createCard(c_Rank.KING, Suit.SPADES);
        Card queenSpades = createCard(c_Rank.QUEEN, Suit.SPADES);
        Card jackSpades = createCard(c_Rank.JACK, Suit.SPADES);
        Card tenSpades = createCard(c_Rank.TEN, Suit.SPADES);

        Card aceDiamonds = createCard(c_Rank.ACE, Suit.DIAMONDS);
        Card kingDiamonds = createCard(c_Rank.KING, Suit.DIAMONDS);
        Card queenDiamonds = createCard(c_Rank.QUEEN, Suit.DIAMONDS);
        Card jackDiamonds = createCard(c_Rank.JACK, Suit.DIAMONDS);

        Card aceClubs = createCard(c_Rank.ACE, Suit.CLUBS);
        Card kingClubs = createCard(c_Rank.KING, Suit.CLUBS);

        // Set up the community cards - flop, turn, and river
        List<Card> communityCards = new ArrayList<>();
        communityCards.add(tenHearts);
        communityCards.add(jackHearts);
        communityCards.add(queenHearts);
        communityCards.add(kingDiamonds);
        communityCards.add(aceDiamonds);

        // Set up player hands
        Map<String, List<Card>> playerHands = new HashMap<>();

        // Player 1 has Ace and King of Hearts - making a Royal Flush with community cards
        List<Card> player1Cards = new ArrayList<>();
        player1Cards.add(aceHearts);
        player1Cards.add(kingHearts);
        playerHands.put("Player1", player1Cards);

        // Player 2 has Ace and King of Spades - making a Straight Flush to Ace
        List<Card> player2Cards = new ArrayList<>();
        player2Cards.add(aceSpades);
        player2Cards.add(kingSpades);
        playerHands.put("Player2", player2Cards);

        // Player 3 has Ace and King of Clubs - making a Straight to Ace
        List<Card> player3Cards = new ArrayList<>();
        player3Cards.add(aceClubs);
        player3Cards.add(kingClubs);
        playerHands.put("Player3", player3Cards);

        // Create the WinnerDeterminer with the community cards
        WinnerDeterminer winnerDeterminer = new WinnerDeterminer(communityCards);

        // Determine the winners
        List<WinnerDeterminer.WinnerResult> winners = winnerDeterminer.determineWinners(playerHands);

        // Print the community cards
        System.out.println("Community Cards:");
        for (Card card : communityCards) {
            System.out.println("  " + card);
        }
        System.out.println();

        // Print each player's hole cards
        System.out.println("Player Hole Cards:");
        for (Map.Entry<String, List<Card>> entry : playerHands.entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (Card card : entry.getValue()) {
                System.out.println("  " + card);
            }
        }
        System.out.println();

        // Print the winners
        System.out.println("Winners:");
        for (WinnerDeterminer.WinnerResult winner : winners) {
            System.out.println(winner);
        }
    }

    /**
     * Helper method to create a card.
     */
    private static Card createCard(c_Rank rank, Suit suit) {
        Card card = new Card();
        card.setCRank(rank);
        card.setSuit(suit);
        card.setShowing(true);
        return card;
    }
}