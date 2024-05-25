import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.List;
import java.util.Random;

public class RandomPlayer extends Player {
    private int seed = 3008;
    private Random random = new Random(seed);

    RandomPlayer(String strType, Hand hand, Hand playingArea, int autoIndexHand, List<String> playerAutoMovements) {
        super(strType, hand, playingArea, autoIndexHand, playerAutoMovements);
    }

    @Override
    public Card selectRemoveCard() {
        Card selected = getRandomCard(this.getHand());
        return selected;
    }

    public Card getRandomCard(Hand hand) {

        int x = random.nextInt(hand.getCardList().size());
        return hand.getCardList().get(x);
    }
}
