import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.List;
import java.util.concurrent.RecursiveTask;

public class HumanPlayer extends Player {

    HumanPlayer(String strType, Hand hand, Hand playingArea, int autoIndexHand, List<String> playerAutoMovements) {
        super(strType, hand, playingArea, autoIndexHand, playerAutoMovements);
    }

    @Override
    public Card selectRemoveCard() {
        return null;
    }
}
