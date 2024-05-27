import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.List;

public class BasicPlayer extends Player{

    BasicPlayer(String strType, Hand hand, Hand playingArea,int autoIndexHand, List<String> playerAutoMovements){
        super(strType,hand, playingArea,autoIndexHand,playerAutoMovements);
    }

    @Override
    public Card selectRemoveCard() {
        Card selected = this.getMinCard(this.getHand());
        return selected;
    }

    /**
     * Randomly draws a card and return the smallest card (to be discarded)
     */
    public Card getMinCard(Hand hand) {

        List<Card> cardList=hand.getCardList();
        int minRank=0;
        int idex=0;
        for (int i=0;i<cardList.size();i++){
            var rank1= (Rank)cardList.get(i).getRank();
            if(minRank==0){
                minRank=rank1.getRankCardValue();
                idex=i;
            }else{
                if(rank1.getRankCardValue()<minRank){
                    minRank=rank1.getRankCardValue();
                    idex=i;
                }
            }
        }

        return hand.getCardList().get(idex);
    }
}
