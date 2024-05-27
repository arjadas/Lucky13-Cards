import ch.aplu.jcardgame.Card;
import java.util.List;

public class Calc13 {

    private static final int THIRTEEN_GOAL = 13;

    /**
     * Checks if the given cards sums up to 13
     */
    public boolean isThirteenCards(Card... cards) {
        return isThirteenFromPossibleValues(cards);
    }

    /**
     * Checks if the given cards sums up to 13, from any combination of private and public cards
     */
    public boolean isThirteenMixedCards(List<Card> privateCards, List<Card> publicCards) {
        // Check all combinations of one private and one public card
        for (Card privateCard : privateCards) {
            for (Card publicCard : publicCards) {
                if (isThirteenCards(privateCard, publicCard)) {
                    return true;
                }
            }
        }

        // Check all combinations of two private and one public card
        for (Card publicCard : publicCards) {
            if (isThirteenCards(privateCards.get(0), privateCards.get(1), publicCard)) {
                return true;
            }
        }

        // Check all combinations of one private and two public cards
        for (Card privateCard : privateCards) {
            if (isThirteenCards(privateCard, publicCards.get(0), publicCards.get(1))) {
                return true;
            }
        }

        // Check all combinations of two private and two public cards
        if (isThirteenCards(privateCards.get(0), privateCards.get(1), publicCards.get(0), publicCards.get(1))) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether given cards sum up to 13.
     * handles cards with multiple possible values（i.e., Ace can be 0 or 1）
     */
    private boolean isThirteenFromPossibleValues(Card... cards) {
        int[][] possibleValues = new int[cards.length][];
        for (int i = 0; i < cards.length; i++) {
            possibleValues[i] = getPossibleValues(cards[i]);
        }
        return checkCombinations(possibleValues, 0, 0);
    }

    /**
     * Uses recursion to check all combinations of the possible values.
     */
    private boolean checkCombinations(int[][] possibleValues, int index, int currentSum) {
        if (index == possibleValues.length) {
            return currentSum == THIRTEEN_GOAL;
        }
        for (int value : possibleValues[index]) {
            if (checkCombinations(possibleValues, index + 1, currentSum + value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the possible values of a given card
     */
    private int[] getPossibleValues(Card card) {
        Rank rank = (Rank) card.getRank();
        return rank.getPossibleSumValues();
    }
}
