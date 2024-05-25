import ch.aplu.jcardgame.Hand;

import java.util.List;

public class PlayerFactory {

    public static Player getPlayer(String strType, Hand hand, Hand playingArea, int autoIndexHand, List<String> playerAutoMovements) {

        if (strType.equals("basic")) {
            return new BasicPlayer(strType, hand, playingArea, autoIndexHand, playerAutoMovements);
        } else if (strType.equals("clever")) {
            return new CleverPlayer(strType, hand, playingArea, autoIndexHand, playerAutoMovements);
        } else if (strType.equals("human")) {
            return new HumanPlayer(strType, hand, playingArea, autoIndexHand, playerAutoMovements);
        } else {
            return new RandomPlayer(strType, hand, playingArea, autoIndexHand, playerAutoMovements);

        }
    }
}
