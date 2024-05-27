import ch.aplu.jcardgame.Hand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PlayerFactory {


    private Player[] players = LuckyThirdteen.getPlayers();

    public void createPlayers(Properties properties) {
        // create players with their type
        String player0Type = properties.getProperty("players.0");
        String player1Type = properties.getProperty("players.1");
        String player2Type = properties.getProperty("players.2");
        String player3Type = properties.getProperty("players.3");

        // default to random player if no specified one
        if (player0Type == null) {
            player0Type = "random";
        }
        if (player1Type == null) {
            player1Type = "random";
        }
        if (player2Type == null) {
            player2Type = "random";
        }
        if (player3Type == null) {
            player3Type = "random";
        }
        String[] playerTypes = new String[]{player0Type, player1Type, player2Type, player3Type};

        // assign player movements
        String player0AutoMovement = properties.getProperty("players.0.cardsPlayed");
        String player1AutoMovement = properties.getProperty("players.1.cardsPlayed");
        String player2AutoMovement = properties.getProperty("players.2.cardsPlayed");
        String player3AutoMovement = properties.getProperty("players.3.cardsPlayed");

        String[] playerMovements = new String[]{"", "", "", ""};
        if (player0AutoMovement != null) {
            playerMovements[0] = player0AutoMovement;
        }

        if (player1AutoMovement != null) {
            playerMovements[1] = player1AutoMovement;
        }

        if (player2AutoMovement != null) {
            playerMovements[2] = player2AutoMovement;
        }

        if (player3AutoMovement != null) {
            playerMovements[3] = player3AutoMovement;
        }

        for (int i = 0; i < playerMovements.length; i++) {
            String movementString = playerMovements[i];
            if (movementString.equals("")) {
                Player player = getPlayer(playerTypes[i], null, null, 0, new ArrayList<>());
                players[i] = player;
                continue;
            }
            List<String> movements = Arrays.asList(movementString.split(","));
            Player player = getPlayer(playerTypes[i], null, null, 0, movements);
            players[i] = player;

        }
    }


    // return player based on specified type
    private Player getPlayer(String strType, Hand hand,Hand playingArea, int autoIndexHand,
                             List<String> playerAutoMovements){

        if(strType.equals("basic")){
            return new BasicPlayer(strType,hand,playingArea,autoIndexHand,playerAutoMovements);
        }else if(strType.equals("clever")){
            return new CleverPlayer(strType,hand,playingArea,autoIndexHand,playerAutoMovements);

        }
        else if(strType.equals("human")){
            return new HumanPlayer(strType,hand,playingArea,autoIndexHand,playerAutoMovements);

        }
        else {
            return new RandomPlayer(strType,hand,playingArea,autoIndexHand,playerAutoMovements);

        }
    }

}
