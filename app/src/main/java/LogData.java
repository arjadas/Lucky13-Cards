import ch.aplu.jcardgame.Card;

import java.util.List;
import java.util.stream.Collectors;

public class LogData {

    private StringBuilder logResult;

    public LogData() {
        logResult = new StringBuilder();
    }

    public void addCardPlayedToLog(int player, List<Card> cards) {
        if (cards.size() < 2) {
            return;
        }
        logResult.append("P" + player + "-");

        for (int i = 0; i < cards.size(); i++) {
            Rank cardRank = (Rank) cards.get(i).getRank();
            Suit cardSuit = (Suit) cards.get(i).getSuit();
            logResult.append(cardRank.getRankCardLog() + cardSuit.getSuitShortHand());
            if (i < cards.size() - 1) {
                logResult.append("-");
            }
        }
        logResult.append(",");
    }

    public void addRoundInfoToLog(int roundNumber) {
        logResult.append("Round" + roundNumber + ":");
    }

    public void addEndOfRoundToLog(Player[] players ) {
        logResult.append("Score:");
        for (int i = 0; i < players.length; i++) {
            logResult.append(players[i].getScore() + ",");
        }
        logResult.append("\n");
    }

    public void addEndOfGameToLog(List<Integer> winners,Player[] players) {
        logResult.append("EndGame:");
        for (int i = 0; i < players.length; i++) {
            logResult.append(players[i].getScore() + ",");
        }
        logResult.append("\n");
        logResult.append("Winners:" + String.join(", ", winners.stream().map(String::valueOf).collect(Collectors.toList())));
    }

    public StringBuilder getLogResult(){
        return logResult;
    }

}
