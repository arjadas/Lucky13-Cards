import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Algorithm {

    private Player[] players = null;
    private Hand playingArea; // Stores the two public cards
    private Calc13 calc13;
    private CalcMaxScore calcMaxScore;

    public Algorithm(Hand playingArea) {
        this.playingArea = playingArea;
        this.calc13 = new Calc13();
        this.calcMaxScore = new CalcMaxScore(playingArea.getCardList(), calc13);
    }

    public Algorithm(Player[] players, Hand playingArea) {
        this.players = players;
        this.playingArea = playingArea;
        this.calc13 = new Calc13();
        this.calcMaxScore = new CalcMaxScore(playingArea.getCardList(), calc13);
    }

    public void calculateScoreEndOfRound() {
        List<Boolean> isThirteenChecks = Arrays.asList(false, false, false, false);
        for (int i = 0; i < players.length; i++) {
            isThirteenChecks.set(i, isThirteen(i)); // Check if each player meets the 13 criteria
        }

        List<Integer> indexesWithThirteen = new ArrayList<>();
        for (int i = 0; i < isThirteenChecks.size(); i++) {
            if (isThirteenChecks.get(i)) { // Check if it equals 13
                indexesWithThirteen.add(i);
            }
        }

        long countTrue = indexesWithThirteen.size(); // Number of players who equal 13

        if (countTrue == 1) { // Only one player equals 13
            int winnerIndex = indexesWithThirteen.get(0); // Get the index of that player
            players[winnerIndex].setScore(100); // Record score as 100
        } else if (countTrue > 1) { // Multiple players equal 13
            for (Integer thirteenIndex : indexesWithThirteen) {
                List<Card> privateCards = players[thirteenIndex].getHand().getCardList();
                players[thirteenIndex].setScore(calcMaxScore.calculateMaxScoreForThirteenPlayer(privateCards));
            }
        } else { // No player equals 13, only calculate the score of two private cards
            for (int i = 0; i < players.length; i++) {
                players[i].setScore(calcMaxScore.getScorePrivateCard(players[i].getHand().getCardList().get(0)) +
                        calcMaxScore.getScorePrivateCard(players[i].getHand().getCardList().get(1)));
            }
        }
    }

    private boolean isThirteen(int playerIndex) {
        List<Card> privateCards = players[playerIndex].getHand().getCardList();
        List<Card> publicCards = playingArea.getCardList();
        boolean isThirteenPrivate = calc13.isThirteenCards(privateCards.get(0), privateCards.get(1));
        boolean isThirteenMixed = calc13.isThirteenMixedCards(privateCards, publicCards);
        return isThirteenMixed || isThirteenPrivate;
    }
}