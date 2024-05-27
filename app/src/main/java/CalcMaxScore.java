import ch.aplu.jcardgame.Card;
import java.util.List;

public class CalcMaxScore {

    private List<Card> publicCards;
    private Calc13 calc13;

    public CalcMaxScore(List<Card> publicCards, Calc13 calc13) {
        this.publicCards = publicCards;
        this.calc13 = calc13;
    }

    public int calculateMaxScoreForThirteenPlayer(List<Card> privateCards) {
        Card privateCard1 = privateCards.get(0);
        Card privateCard2 = privateCards.get(1);
        Card publicCard1 = publicCards.get(0);
        Card publicCard2 = publicCards.get(1);

        int maxScore = 0;

        // Check all combinations of two cards
        if (calc13.isThirteenCards(privateCard1, privateCard2)) {
            int score = getScorePrivateCard(privateCard1) + getScorePrivateCard(privateCard2);
            maxScore = Math.max(maxScore, score);
        }

        if (calc13.isThirteenCards(privateCard1, publicCard1)) {
            int score = getScorePrivateCard(privateCard1) + getScorePublicCard(publicCard1);
            maxScore = Math.max(maxScore, score);
        }

        if (calc13.isThirteenCards(privateCard1, publicCard2)) {
            int score = getScorePrivateCard(privateCard1) + getScorePublicCard(publicCard2);
            maxScore = Math.max(maxScore, score);
        }

        if (calc13.isThirteenCards(privateCard2, publicCard1)) {
            int score = getScorePrivateCard(privateCard2) + getScorePublicCard(publicCard1);
            maxScore = Math.max(maxScore, score);
        }

        if (calc13.isThirteenCards(privateCard2, publicCard2)) {
            int score = getScorePrivateCard(privateCard2) + getScorePublicCard(publicCard2);
            maxScore = Math.max(maxScore, score);
        }

        // Check all combinations of three cards

        if (calc13.isThirteenCards(privateCard1, publicCard1, publicCard2)) {
            int score = getScorePrivateCard(privateCard1) +
                    getScorePublicCard(publicCard1) + getScorePublicCard(publicCard2);
            maxScore = Math.max(maxScore, score);
        }

        if (calc13.isThirteenCards(privateCard2, publicCard1, publicCard2)) {
            int score = getScorePrivateCard(privateCard2)
                    + getScorePublicCard(publicCard1) + getScorePublicCard(publicCard2);
            maxScore = Math.max(maxScore, score);
        }

        // Check all combinations of four cards
        if (calc13.isThirteenCards(privateCard1, privateCard2, publicCard1, publicCard2)) {
            int score = getScorePrivateCard(privateCard1) + getScorePrivateCard(privateCard2) +
                    getScorePublicCard(publicCard1) + getScorePublicCard(publicCard2);
            maxScore = Math.max(maxScore, score);
        }

        return maxScore;
    }

    public int getScorePrivateCard(Card card) {
        Rank rank = (Rank) card.getRank();
        Suit suit = (Suit) card.getSuit();
        return rank.getScoreCardValue() * suit.getMultiplicationFactor();
    }

    public int getScorePublicCard(Card card) {
        Rank rank = (Rank) card.getRank();
        return rank.getScoreCardValue() * Suit.PUBLIC_CARD_MULTIPLICATION_FACTOR;
    }
}