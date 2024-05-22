/**
 CleverPlayer Logic Rules
 1. Check if the drawn card matches any card in the hand:
 First, check if the rank of the drawn card matches any card in the hand. If the ranks match, compare the suits. Keep the card with the higher suit and discard the card with the lower suit (there will be no case where both rank and suit are the same).
 The program returns the card that needs to be discarded.

 2. Save combinations of hand cards and public cards:
 Save five combinations of hand cards and public cards in an array. The specific combinations are:
 Combination 1: Sum of hand card 1 and hand card 2.
 Combination 2: Sum of hand card 1 and public card 1.
 Combination 3: Sum of hand card 1 and public card 2.
 Combination 4: Sum of hand card 2 and public card 1.
 Combination 5: Sum of hand card 2 and public card 2.
 Check if any combination equals 13:
 Based on the combinations saved in the second step, check if any combination equals 13. If such a combination exists, play the drawn card directly.
 If none of the five combinations equal 13, make a decision based on the sum of the drawn card and the hand cards/public cards:
 If the sum of the drawn card and any hand card equals 13, discard the other hand card directly.
 If the sum of the drawn card and any public card equals 13, discard one hand card randomly.

 3. If no card combinations equal 13:
 Save the new card combinations of the drawn card with hand cards and public cards in an array. The specific combinations are:
 Combination 1: Sum of the drawn card and hand card 1.
 Combination 2: Sum of the drawn card and hand card 2.
 Combination 3: Sum of the drawn card and public card 1.
 Combination 4: Sum of the drawn card and public card 2.
 Compare the absolute values of the sums minus 13 for all combinations. If any of the new four combinations have a smaller absolute value than the previous five combinations, the drawn card is more suitable.
 If the absolute value of the sum of the drawn card and hand card 1 is smaller than the absolute values of the previous five combinations, discard hand card 2.
 If the absolute value of the sum of the drawn card and hand card 2 is smaller than the absolute values of the previous five combinations, discard hand card 1.
 If the absolute value of the sum of the drawn card and any public card is smaller than the absolute values of the previous five combinations, discard one of the old hand cards randomly.

 */

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CleverPlayer extends  Player{
    private int seed=3008;
    private Random random = new Random(seed);
    CleverPlayer(String strType, Hand hand,Hand playingArea, int autoIndexHand, List<String> playerAutoMovements){
        super(strType,hand, playingArea,autoIndexHand,playerAutoMovements);
    }
    @Override
    public Card selectRemoveCard() {
        Card selected = this.getCleverCard(this.getHand());
        return selected;
    }


    public Card getCleverCard(Hand hand) {

        Algorithm algorithm=new Algorithm(this.getPlayingArea());


        Card lastCard=hand.getCardList().get(2);
        Card privateCard1=hand.getCardList().get(0);
        Card privateCard2=hand.getCardList().get(1);

        var rank1= (Rank)privateCard1.getRank();
        var rank2= (Rank)privateCard2.getRank();
        var lastRank= (Rank)lastCard.getRank();

        Suit lastSuit = (Suit) lastCard.getSuit();
        Suit suit1 = (Suit) privateCard1.getSuit();
        Suit suit2 = (Suit) privateCard2.getSuit();

        if(lastRank.getRankCardValue() == rank1.getRankCardValue()){
            if(lastSuit.getMultiplicationFactor()>suit1.getMultiplicationFactor()){
                return privateCard1;
            }else{
                return lastCard;
            }
        }
        if(lastRank.getRankCardValue() == rank2.getRankCardValue()){
            if(lastSuit.getMultiplicationFactor()>suit2.getMultiplicationFactor()){
                return privateCard2;
            }
            else{
                return lastCard;
            }
        }


        List<Card> publicCards = this.getPlayingArea().getCardList();//拿到公牌
        var publicRank1= (Rank)publicCards.get(0).getRank();
        var publicRank2= (Rank)publicCards.get(1).getRank();
        List<Integer> sumList=new ArrayList<Integer>();


        if(algorithm.isThirteenCards(privateCard1, privateCard2)){
            return lastCard;
        }
        sumList.add(rank1.getRankCardValue()+rank2.getRankCardValue());

        if(algorithm.isThirteenCards(privateCard1, publicCards.get(0))){
            return lastCard;
        }
        sumList.add(rank1.getRankCardValue()+publicRank1.getRankCardValue());

        if(algorithm.isThirteenCards(privateCard1, publicCards.get(1))){
            return lastCard;
        }
        sumList.add(rank1.getRankCardValue()+publicRank2.getRankCardValue());

        if(algorithm.isThirteenCards(privateCard2, publicCards.get(0))){
            return lastCard;
        }
        sumList.add(rank2.getRankCardValue()+publicRank1.getRankCardValue());

        if(algorithm.isThirteenCards(privateCard2, publicCards.get(1))){
            return lastCard;
        }
        sumList.add(rank2.getRankCardValue()+publicRank2.getRankCardValue());


        List<Integer> sumNewList=new ArrayList<Integer>();

        if(algorithm.isThirteenCards(lastCard, privateCard1)){
            return privateCard2;
        }
        int tmp=lastRank.getRankCardValue()+rank1.getRankCardValue();
        sumNewList.add(tmp);

        if(algorithm.isThirteenCards(lastCard, privateCard2)){
            return privateCard1;
        }
        tmp=lastRank.getRankCardValue()+rank2.getRankCardValue();
        sumNewList.add(tmp);


        if(algorithm.isThirteenCards(lastCard, publicCards.get(0))){
            int x = random.nextInt(2);
            if(x==1){
                return privateCard1;
            }else{
                return privateCard2;
            }
        }
        tmp=lastRank.getRankCardValue()+publicRank1.getRankCardValue();
        sumNewList.add(tmp);

        if(algorithm.isThirteenCards(lastCard, publicCards.get(1))){
            int x = random.nextInt(2);
            if(x==1){
                return privateCard1;
            }else{
                return privateCard2;
            }
        }
        tmp=lastRank.getRankCardValue()+publicRank2.getRankCardValue();
        sumNewList.add(tmp);


        for(int i=0;i<sumList.size();i++){
            sumList.set(i,Math.abs(sumList.get(i)-13));
        }
        for(int i=0;i<sumNewList.size();i++){
            sumNewList.set(i,Math.abs(sumNewList.get(i)-13));
        }

        int index=0;
        for(int i=0;i<sumNewList.size();i++) {
            int num=0;
            index++;
            for(int j=0;j<sumList.size();j++) {

                if(sumNewList.get(i)<sumList.get(j)){
                    num++;
                }
            }
            if(num==sumList.size()){
                if(index==1){
                    return privateCard2;

                }else if(index==2){
                    return privateCard1;

                }else{
                    int x = random.nextInt(2);
                    if(x==1){
                        return privateCard1;
                    }else{
                        return privateCard2;
                    }
                }

            }
        }
        return  lastCard;

    }
}

