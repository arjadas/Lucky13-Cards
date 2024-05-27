import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.ArrayList;
import java.util.List;

public abstract class Player {

    private String strType;
    private Hand hand;
    private  int score;
    private int autoIndexHand;
    private List<String> playerAutoMovements = new ArrayList<>();
    private Hand playingArea;

    Player(String strType,Hand hand,Hand playingArea,int autoIndexHand,List<String> playerAutoMovements){
        this.setStrType(strType);
        this.setHand(hand);
        this.setAutoIndexHand(autoIndexHand);
        this.setPlayerAutoMovements(playerAutoMovements);
        this.setPlayingArea(playingArea);
    }

    /*
        getters and setters
     */

    public String getStrType() {
        return strType;
    }

    public void setStrType(String strType) {
        this.strType = strType;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getAutoIndexHand() {
        return autoIndexHand;
    }

    public void setAutoIndexHand(int autoIndexHand) {
        this.autoIndexHand = autoIndexHand;
    }

    public List<String> getPlayerAutoMovements() {
        return playerAutoMovements;
    }

    public void setPlayerAutoMovements(List<String> playerAutoMovements) {
        this.playerAutoMovements = playerAutoMovements;
    }

    public Hand getPlayingArea() {
        return playingArea;
    }

    public void setPlayingArea(Hand playingArea) {
        this.playingArea = playingArea;
    }

    /**
     * Selects the card that needs to be discarded
     */
    public abstract Card selectRemoveCard();
}
