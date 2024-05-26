import ch.aplu.jcardgame.Card;
import java.util.List;

public class Calc13 {

    private static final int THIRTEEN_GOAL = 13;

    public boolean isThirteenCards(Card card1, Card card2) {
        return getCardScore(card1) + getCardScore(card2) == THIRTEEN_GOAL;
    }

    public boolean isThirteenCards(Card card1, Card card2, Card card3) {
        return getCardScore(card1) + getCardScore(card2) + getCardScore(card3) == THIRTEEN_GOAL;
    }

    public boolean isThirteenCards(Card card1, Card card2, Card card3, Card card4) {
        return getCardScore(card1) + getCardScore(card2) + getCardScore(card3) + getCardScore(card4) == THIRTEEN_GOAL;
    }

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

    private int getCardScore(Card card) {
        Rank rank = (Rank) card.getRank();
        return rank.getScoreCardValue();
    }
}