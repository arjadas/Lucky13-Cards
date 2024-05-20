import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Algorithm {

    private static final int THIRTEEN_GOAL = 13;
    static public final int seed = 30008;
    static final Random random = new Random(seed);

    private Player[] players = null;
    private Hand playingArea; // Stores the two public cards

    public Algorithm(Player[] players, Hand playingArea) {
        this.players = players;
        this.playingArea = playingArea;
    }

    public void calculateScoreEndOfRound() {
        List<Boolean> isThirteenChecks = Arrays.asList(false, false, false, false);
        for (int i = 0; i < players.length; i++) {
            isThirteenChecks.set(i, isThirteen(i)); // Check if each player meets the 13 criteria
        }

        // Get the indexes of players who meet the 13 criteria
        List<Integer> indexesWithThirteen = new ArrayList<>();
        for (int i = 0; i < isThirteenChecks.size(); i++) {
            if (isThirteenChecks.get(i)) { // Check if it equals 13
                indexesWithThirteen.add(i);
            }
        }
        long countTrue = indexesWithThirteen.size(); // Number of players who equal 13

        if (countTrue == 1) { // Only one player equals 13
            int winnerIndex = indexesWithThirteen.get(0); // Get the index of that player
            players[winnerIndex].setScore(100); // Record score as 100
        } else if (countTrue > 1) { // Multiple players equal 13
            for (Integer thirteenIndex : indexesWithThirteen) {
                players[thirteenIndex].setScore(calculateMaxScoreForThirteenPlayer(thirteenIndex)); // Update score for players whose card sum is 13
            }
        } else { // No player equals 13, only calculate the score of two private cards
            for (int i = 0; i < players.length; i++) {
                players[i].setScore(getScorePrivateCard(players[i].getHand().getCardList().get(0)) +
                        getScorePrivateCard(players[i].getHand().getCardList().get(1)));
            }
        }
    }

    // Check if the sum of card ranks equals 13
    private boolean isThirteen(int playerIndex) {
        List<Card> privateCards = players[playerIndex].getHand().getCardList(); // Private cards
        List<Card> publicCards = playingArea.getCardList(); // Public cards
        boolean isThirteenPrivate = isThirteenCards(privateCards.get(0), privateCards.get(1)); // Sum of two private cards
        boolean isThirteenMixed = isThirteenMixedCards(privateCards, publicCards); // Sum of one private and one public card
        return isThirteenMixed || isThirteenPrivate;
    }

    // Repeatedly calculate combinations that satisfy 13, then take the maximum score
    private int calculateMaxScoreForThirteenPlayer(int playerIndex) {
        List<Card> privateCards = players[playerIndex].getHand().getCardList(); // Private cards of a player
        List<Card> publicCards = playingArea.getCardList(); // Public cards
        Card privateCard1 = privateCards.get(0);
        Card privateCard2 = privateCards.get(1);
        Card publicCard1 = publicCards.get(0);
        Card publicCard2 = publicCards.get(1);

        int maxScore = 0;

        // Calculate the maximum score
        if (isThirteenCards(privateCard1, privateCard2)) { // Two private cards
            int score = getScorePrivateCard(privateCard1) + getScorePrivateCard(privateCard2); // Score of two private cards
            if (maxScore < score) {
                maxScore = score;
            }
        }

        if (isThirteenCards(privateCard1, publicCard1)) {
            int score = getScorePrivateCard(privateCard1) + getScorePublicCard(publicCard1);
            if (maxScore < score) {
                maxScore = score;
            }
        }

        if (isThirteenCards(privateCard1, publicCard2)) {
            int score = getScorePrivateCard(privateCard1) + getScorePublicCard(publicCard2);
            if (maxScore < score) {
                maxScore = score;
            }
        }

        if (isThirteenCards(privateCard2, publicCard1)) {
            int score = getScorePrivateCard(privateCard2) + getScorePublicCard(publicCard1);
            if (maxScore < score) {
                maxScore = score;
            }
        }

        if (isThirteenCards(privateCard2, publicCard2)) {
            int score = getScorePrivateCard(privateCard2) + getScorePublicCard(publicCard2);
            if (maxScore < score) {
                maxScore = score;
            }
        }

        // Check if the sum of four cards equals 13, and if so, compare this score with the maximum score
        if (isThirteenCards(privateCard1, privateCard2, publicCard1, publicCard2)) {
            System.out.println("The sum of four cards equals 13");
            int score = getScorePrivateCard(privateCard1) + getScorePrivateCard(privateCard2) +
                    getScorePublicCard(publicCard1) + getScorePublicCard(publicCard2);
            System.out.println("score=" + score);
            if (maxScore < score) {
                maxScore = score;
            }
        }

        return maxScore;
    }

    // Calculate the actual score based on the suit
    private int getScorePrivateCard(Card card) {
        Rank rank = (Rank) card.getRank();
        Suit suit = (Suit) card.getSuit();
        return rank.getScoreCardValue() * suit.getMultiplicationFactor();
    }

    // Calculate if the sum of card ranks equals 13
    private boolean isThirteenCards(Card card1, Card card2) {
        Rank rank1 = (Rank) card1.getRank();
        Rank rank2 = (Rank) card2.getRank();
        return isThirteenFromPossibleValues(rank1.getPossibleSumValues(), rank2.getPossibleSumValues());
    }

    private boolean isThirteenCards(Card card1, Card card2, Card card3, Card card4) {
        Rank rank1 = (Rank) card1.getRank();
        Rank rank2 = (Rank) card2.getRank();
        Rank rank3 = (Rank) card3.getRank();
        Rank rank4 = (Rank) card4.getRank();
        return isThirteenFromPossibleValues(rank1.getPossibleSumValues(), rank2.getPossibleSumValues(),
                rank3.getPossibleSumValues(), rank4.getPossibleSumValues());
    }

    private boolean isThirteenMixedCards(List<Card> privateCards, List<Card> publicCards) {
        for (Card privateCard : privateCards) {
            for (Card publicCard : publicCards) {
                if (isThirteenCards(privateCard, publicCard)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Calculate the score for public cards
    private int getScorePublicCard(Card card) {
        Rank rank = (Rank) card.getRank();
        return rank.getScoreCardValue() * Suit.PUBLIC_CARD_MULTIPLICATION_FACTOR;
    }

    // Check if the sum of possible values equals 13
    private boolean isThirteenFromPossibleValues(int[] possibleValues1, int[] possibleValues2) {
        for (int value1 : possibleValues1) {
            for (int value2 : possibleValues2) {
                if (value1 + value2 == THIRTEEN_GOAL) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isThirteenFromPossibleValues(int[] possibleValues1, int[] possibleValues2,
                                                 int[] possibleValues3, int[] possibleValues4) {
        for (int value1 : possibleValues1) {
            for (int value2 : possibleValues2) {
                for (int value3 : possibleValues3) {
                    for (int value4 : possibleValues4) {
                        if (value1 + value2 + value3 + value4 == THIRTEEN_GOAL) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // Randomly draw a card and return the card to be discarded
    public Card getRandomCard(Hand hand) {
        int x = random.nextInt(hand.getCardList().size());
        return hand.getCardList().get(x);
    }

    // Randomly draw a card and return the smallest card (to be discarded)
    public Card getMinCard(Hand hand) {
        List<Card> cardList = hand.getCardList();
        int minRank = 0;
        int index = 0;

        // Find the smallest rank
        for (int i = 0; i < cardList.size(); i++) {
            var rank = (Rank) cardList.get(i).getRank();
            if (minRank == 0) {
                minRank = rank.getRankCardValue();
                index = i;
            } else {
                if (rank.getRankCardValue() < minRank) {
                    minRank = rank.getRankCardValue();
                    index = i;
                }
            }
        }
        return hand.getCardList().get(index);
    }

    // Cleverly choose a card to discard
    public Card getCleverCard(Hand hand) {
        Card lastCard = hand.getCardList().get(2); // The drawn card
        Card privateCard1 = hand.getCardList().get(0);
        Card privateCard2 = hand.getCardList().get(1);

        var rank1 = (Rank) privateCard1.getRank();
        var rank2 = (Rank) privateCard2.getRank();
        var lastRank = (Rank) lastCard.getRank();

        Suit lastSuit = (Suit) lastCard.getSuit();
        Suit suit1 = (Suit) privateCard1.getSuit();
        Suit suit2 = (Suit) privateCard2.getSuit();

        // If the two cards have the same rank, compare the suits
        if (lastRank.getRankCardValue() == rank1.getRankCardValue()) {
            if (lastSuit.getMultiplicationFactor() > suit1.getMultiplicationFactor()) {
                return privateCard1;
            } else {
                return lastCard;
            }
        }
        if (lastRank.getRankCardValue() == rank2.getRankCardValue()) {
            if (lastSuit.getMultiplicationFactor() > suit2.getMultiplicationFactor()) {
                return privateCard2;
            } else {
                return lastCard;
            }
        }

        // Create a list of sums of card pairs (private-private, private-public)
        List<Card> publicCards = playingArea.getCardList();
        var publicRank1 = (Rank) publicCards.get(0).getRank();
        var publicRank2 = (Rank) publicCards.get(1).getRank();
        List<Integer> sumList = new ArrayList<>();

        if (isThirteenCards(privateCard1, privateCard2)) {
            return lastCard;
        }
        sumList.add(rank1.getRankCardValue() + rank2.getRankCardValue());

        if (isThirteenCards(privateCard1, publicCards.get(0))) {
            return lastCard;
        }
        sumList.add(rank1.getRankCardValue() + publicRank1.getRankCardValue());

        if (isThirteenCards(privateCard1, publicCards.get(1))) {
            return lastCard;
        }
        sumList.add(rank1.getRankCardValue() + publicRank2.getRankCardValue());

        if (isThirteenCards(privateCard2, publicCards.get(0))) {
            return lastCard;
        }
        sumList.add(rank2.getRankCardValue() + publicRank1.getRankCardValue());

        if (isThirteenCards(privateCard2, publicCards.get(1))) {
            return lastCard;
        }
        sumList.add(rank2.getRankCardValue() + publicRank2.getRankCardValue());

        // Create a list of sums of new card combinations
        List<Integer> sumNewList = new ArrayList<>();

        if (isThirteenCards(lastCard, privateCard1)) {
            return privateCard2;
        }
        int tmp = lastRank.getRankCardValue() + rank1.getRankCardValue();
        sumNewList.add(tmp);

        if (isThirteenCards(lastCard, privateCard2)) {
            return privateCard1;
        }
        tmp = lastRank.getRankCardValue() + rank2.getRankCardValue();
        sumNewList.add(tmp);

        if (isThirteenCards(lastCard, publicCards.get(0))) {
            int x = random.nextInt(2);
            if (x == 1) {
                return privateCard1;
            } else {
                return privateCard2;
            }
        }
        tmp = lastRank.getRankCardValue() + publicRank1.getRankCardValue();
        sumNewList.add(tmp);

        if (isThirteenCards(lastCard, publicCards.get(1))) {
            int x = random.nextInt(2);
            if (x == 1) {
                return privateCard1;
            } else {
                return privateCard2;
            }
        }
        tmp = lastRank.getRankCardValue() + publicRank2.getRankCardValue();
        sumNewList.add(tmp);

        // Compare the sums with the original list to decide which card to discard
        for (int i = 0; i < sumList.size(); i++) {
            sumList.set(i, Math.abs(sumList.get(i) - 13));
        }
        for (int i = 0; i < sumNewList.size(); i++) {
            sumNewList.set(i, Math.abs(sumNewList.get(i) - 13));
        }

        for (int i = 0; i < sumNewList.size(); i++) {
            int num = 0;
            for (int j = 0; j < sumList.size(); j++) {
                if (sumNewList.get(i) < sumList.get(j)) {
                    num++;
                }
            }
            if (num == sumList.size()) {
                int x = random.nextInt(2);
                if (x == 1) {
                    return privateCard1;
                } else {
                    return privateCard2;
                }
            }
        }
        return lastCard;
    }
}
