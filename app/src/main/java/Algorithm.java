import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

// Class just cal 13，no more discarded
public class Algorithm {

    private static final int THIRTEEN_GOAL = 13;
    private Player[] players=null;
    private Hand playingArea;// Stores the two public cards

    public Algorithm(Hand playingArea){
        this.playingArea=playingArea;
    }
    public Algorithm(Player[] players,Hand playingArea){
        this.players=players;
        this.playingArea=playingArea;
    }

    public void calculateScoreEndOfRound() {
        List<Boolean> isThirteenChecks = Arrays.asList(false, false, false, false);
        for (int i = 0; i < players.length; i++) {
            isThirteenChecks.set(i, isThirteen(i));// Check if each player meets the 13 criteria
        }
        // Get the indexes of players who meet the 13 criteria
        List<Integer> indexesWithThirteen = new ArrayList<>();
        for (int i = 0; i < isThirteenChecks.size(); i++) {
            if (isThirteenChecks.get(i)) {// Check if it equals 13
                indexesWithThirteen.add(i);
            }
        }
        long countTrue = indexesWithThirteen.size();// Number of players who equal 13
        ;
        if (countTrue == 1) { // Only one player equals 13
            int winnerIndex = indexesWithThirteen.get(0); // Get the index of that player
            players[winnerIndex].setScore(100) ;// Record score as 100
        } else if (countTrue > 1) {// Multiple players equal 13
            for (Integer thirteenIndex : indexesWithThirteen) {
                players[thirteenIndex].setScore(calculateMaxScoreForThirteenPlayer(thirteenIndex));
            }

        } else {// No player equals 13, only calculate the score of two private cards
            for (int i = 0; i < players.length; i++) {
                players[i].setScore(getScorePrivateCard(players[i].getHand().getCardList().get(0)) +
                        getScorePrivateCard(players[i].getHand().getCardList().get(1)));
            }
        }
    }

    // Check if the sum of card ranks equals 13
    private boolean isThirteen(int playerIndex) {
        List<Card> privateCards = players[playerIndex].getHand().getCardList();
        List<Card> publicCards = playingArea.getCardList();
        boolean isThirteenPrivate = isThirteenCards(privateCards.get(0), privateCards.get(1));
        boolean isThirteenMixed = isThirteenMixedCards(privateCards, publicCards);
        return isThirteenMixed || isThirteenPrivate;
    }

    // Repeatedly calculate combinations that satisfy 13, then take the maximum score
    private int calculateMaxScoreForThirteenPlayer(int playerIndex) {
        List<Card> privateCards = players[playerIndex].getHand().getCardList();// Sum of two private cards
        List<Card> publicCards = playingArea.getCardList();
        Card privateCard1 = privateCards.get(0);
        Card privateCard2 = privateCards.get(1);
        Card publicCard1 = publicCards.get(0);
        Card publicCard2 = publicCards.get(1);

        int maxScore = 0;
        // Calculate the maximum score
        if (isThirteenCards(privateCard1, privateCard2)) {
            int score = getScorePrivateCard(privateCard1) + getScorePrivateCard(privateCard2);
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

        if(isThirteenCards(privateCard2, publicCard2)) {
            int score = getScorePrivateCard(privateCard2) + getScorePublicCard(publicCard2);
            if (maxScore < score) {
                maxScore = score;
            }
        }

        // Check if the sum of four cards equals 13, and if so, compare this score with the maximum score
        if(isThirteenCards(privateCard1,privateCard2, publicCard1, publicCard2)) {
            System.out.println("四张牌之和等于13");
            int score =getScorePrivateCard(privateCard1) + getScorePrivateCard(privateCard2) +getScorePublicCard(publicCard1)+ getScorePublicCard(publicCard2);
            System.out.println("score="+score);
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
    public boolean isThirteenCards(Card card1, Card card2) {
        Rank rank1 = (Rank) card1.getRank();
        Rank rank2 = (Rank) card2.getRank();
        return isThirteenFromPossibleValues(rank1.getPossibleSumValues(), rank2.getPossibleSumValues());
    }
    public boolean isThirteenCards(Card card1, Card card2,Card card3, Card card4) {
        Rank rank1 = (Rank) card1.getRank();
        Rank rank2 = (Rank) card2.getRank();
        Rank rank3 = (Rank) card3.getRank();
        Rank rank4 = (Rank) card4.getRank();
        return isThirteenFromPossibleValues(rank1.getPossibleSumValues(), rank2.getPossibleSumValues(), rank3.getPossibleSumValues(), rank4.getPossibleSumValues());
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

    private boolean isThirteenFromPossibleValues(int[] possibleValues1, int[] possibleValues2, int[] possibleValues3, int[] possibleValues4) {
        for (int value1 : possibleValues1) {
            for (int value2 : possibleValues2) {
                for (int value3 : possibleValues3) {
                    for (int value4 : possibleValues4) {
                        if (value1 + value2+value3+value4== THIRTEEN_GOAL) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


}
