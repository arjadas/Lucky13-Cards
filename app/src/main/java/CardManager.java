import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ch.aplu.jgamegrid.GameGrid.delay;

public class CardManager {

    public static final int seed = 30008;
    private static final Random random = new Random(seed);
    private Hand pack;


    /**
     * Returns random Card from the pack of cards
     */
    public Card randomCard(ArrayList<Card> list) {
        int x = random.nextInt(list.size());
        return list.get(x);
    }

    public Card getCardFromList(List<Card> cards, String cardName) {
        Rank cardRank = getRankFromString(cardName);
        Suit cardSuit = getSuitFromString(cardName);
        for (Card card: cards) {
            if (card.getSuit() == cardSuit
                    && card.getRank() == cardRank) {
                return card;
            }
        }
        return null;
    }

    private Rank getRankFromString(String cardName) {
        String rankString = cardName.substring(0, cardName.length() - 1);
        Integer rankValue = Integer.parseInt(rankString);

        for (Rank rank : Rank.values()) {
            if (rank.getRankCardValue() == rankValue) {
                return rank;
            }
        }

        return Rank.ACE;
    }

    private Suit getSuitFromString(String cardName) {
        String rankString = cardName.substring(0, cardName.length() - 1);
        String suitString = cardName.substring(cardName.length() - 1, cardName.length());
        Integer rankValue = Integer.parseInt(rankString);

        for (Suit suit : Suit.values()) {
            if (suit.getSuitShortHand().equals(suitString)) {
                return suit;
            }
        }
        return Suit.CLUBS;
    }

    /**
     * Implement card drawing logic, moving cards from the public pool to the player's hand
     */
    public void dealACardToHand(Hand hand, Hand pack) {
        if (pack.isEmpty()) return;
        Card dealt = randomCard(pack.getCardList());
        dealt.removeFromHand(false);
        hand.insert(dealt, true);
    }

    /**
     * Initialises each player's hand with 2 cards and the public area with 2 cards
     */
    public void dealingOut(Player[] players, int nbPlayers, int nbCardsPerPlayer, int nbSharedCards,
                           Hand playingArea, Properties properties, Hand pack) {
        // Read public cards from config file
        String initialShareKey = "shared.initialcards";
        String initialShareValue = properties.getProperty(initialShareKey);
        if (initialShareValue != null) {
            String[] initialCards = initialShareValue.split(",");
            for (String initialCard : initialCards) {
                if (initialCard.length() <= 1) {
                    continue;
                }
                // Draw a card from the public card pool
                Card card = getCardFromList(pack.getCardList(), initialCard);
                if (card != null) {
                    card.removeFromHand(true);
                    // Insert into the public area
                    playingArea.insert(card, true);
                }
            }
        }

        int cardsToShare = nbSharedCards - playingArea.getNumberOfCards();

        for (int j = 0; j < cardsToShare; j++) {
            if (pack.isEmpty()) return;
            // Randomly draw a card from the public card pool
            Card dealt = randomCard(pack.getCardList());
            dealt.removeFromHand(true);
            // Insert into the public area
            playingArea.insert(dealt, true);
        }

        // Initialize each player's hand with two cards
        for (int i = 0; i < nbPlayers; i++) {
            String initialCardsKey = "players." + i + ".initialcards";
            String initialCardsValue = properties.getProperty(initialCardsKey);
            if (initialCardsValue == null) {
                continue;
            }
            String[] initialCards = initialCardsValue.split(",");
            for (String initialCard : initialCards) {
                if (initialCard.length() <= 1) {
                    continue;
                }
                Card card = getCardFromList(pack.getCardList(), initialCard);
                if (card != null) {
                    card.removeFromHand(false);
                    players[i].getHand().insert(card, false);
                }
            }
        }
        // If config file is insufficient, randomly draw cards to ensure each player starts with two cards
        for (int i = 0; i < nbPlayers; i++) {
            int cardsToDealt = nbCardsPerPlayer - players[i].getHand().getNumberOfCards();
            for (int j = 0; j < cardsToDealt; j++) {
                if (pack.isEmpty()) return;
                Card dealt = randomCard(pack.getCardList());
                dealt.removeFromHand(false);
                players[i].getHand().insert(dealt, false);
            }
        }
    }

    /**
     * Randomly draws a card from the card pool
     */
    public void randomSelectCard(Hand hand) {

        dealACardToHand(hand, pack);
        delay(LuckyThirdteen.getThinkingTime());
    }

}
