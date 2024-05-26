import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CleverPlayer extends Player {
    private int seed = 3008;
    private Random random = new Random(seed);

    CleverPlayer(String strType, Hand hand, Hand playingArea, int autoIndexHand, List<String> playerAutoMovements) {
        super(strType, hand, playingArea, autoIndexHand, playerAutoMovements);
    }

    @Override
    public Card selectRemoveCard() {
        Card selected = this.getCleverCard(this.getHand());
        return selected;
    }

    public Card getCleverCard(Hand hand) {
        Algorithm algorithm = new Algorithm(this.getPlayingArea());

        Card lastCard = hand.getCardList().get(2);
        Card privateCard1 = hand.getCardList().get(0);
        Card privateCard2 = hand.getCardList().get(1);

        Rank rank1 = (Rank) privateCard1.getRank();
        Rank rank2 = (Rank) privateCard2.getRank();
        Rank lastRank = (Rank) lastCard.getRank();

        Suit lastSuit = (Suit) lastCard.getSuit();
        Suit suit1 = (Suit) privateCard1.getSuit();
        Suit suit2 = (Suit) privateCard2.getSuit();

        // Rule 1: Check if the drawn card matches any card in the hand
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

        // Rule 2: Save combinations of hand cards and public cards
        List<Card> publicCards = this.getPlayingArea().getCardList();
        Rank publicRank1 = (Rank) publicCards.get(0).getRank();
        Rank publicRank2 = (Rank) publicCards.get(1).getRank();
        List<Integer> sumList = new ArrayList<>();

        sumList.add(rank1.getRankCardValue() + rank2.getRankCardValue());
        sumList.add(rank1.getRankCardValue() + publicRank1.getRankCardValue());
        sumList.add(rank1.getRankCardValue() + publicRank2.getRankCardValue());
        sumList.add(rank2.getRankCardValue() + publicRank1.getRankCardValue());
        sumList.add(rank2.getRankCardValue() + publicRank2.getRankCardValue());

        // Check if any combination equals 13
        for (Integer sum : sumList) {
            if (sum == 13) {
                return lastCard;
            }
        }

        // Rule 3: If no card combinations equal 13
        List<Integer> newSumList = new ArrayList<>();
        newSumList.add(lastRank.getRankCardValue() + rank1.getRankCardValue());
        newSumList.add(lastRank.getRankCardValue() + rank2.getRankCardValue());
        newSumList.add(lastRank.getRankCardValue() + publicRank1.getRankCardValue());
        newSumList.add(lastRank.getRankCardValue() + publicRank2.getRankCardValue());

        // Compare absolute values of sums minus 13
        int minDiff = Integer.MAX_VALUE;
        Card cardToDiscard = null;
        for (int i = 0; i < newSumList.size(); i++) {
            int diff = Math.abs(newSumList.get(i) - 13);
            if (diff < minDiff) {
                minDiff = diff;
                if (i == 0) cardToDiscard = privateCard2;
                else if (i == 1) cardToDiscard = privateCard1;
                else cardToDiscard = random.nextInt(2) == 0 ? privateCard1 : privateCard2;
            }
        }

        return cardToDiscard != null ? cardToDiscard : lastCard;
    }
}
