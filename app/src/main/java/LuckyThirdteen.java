import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class LuckyThirdteen extends CardGame {

    private Properties properties;
    private LogData logInfo = new LogData();
    private final String version = "1.0";
    public static final Deck deck = new Deck(Suit.values(), Rank.values(), "cover"); // 52 playing cards created
    private static int thinkingTime = 2000;
    private static int delayTime = 600;

    private static CardMap cardMap = new CardMap();
    private PlayerFactory playerFactory = new PlayerFactory();
    private CardManager cardManager = new CardManager();
    private Card selected;

    private static Player[] players = new Player[cardMap.nbPlayers];
    private boolean isAuto = false;
    private Hand playingArea;
    private Hand pack = LuckyThirdteen.deck.toHand(false);;
    private Actor[] scoreActors = {null, null, null, null};
    Font bigFont = new Font("Arial", Font.BOLD, 36);
    private Algorithm algorithm = null;


    public LuckyThirdteen(Properties properties) {
        super(700, 700, 30);
        this.properties = properties;
        isAuto = Boolean.parseBoolean(properties.getProperty("isAuto"));
        thinkingTime = Integer.parseInt(properties.getProperty("thinkingTime", "200"));
        delayTime = Integer.parseInt(properties.getProperty("delayTime", "50"));
    }

    /*
        getter methods
     */

    public static int getThinkingTime() {
        return thinkingTime;
    }

    public static Player[] getPlayers() {
        return players;
    }

    public void setStatus(String string) {
        setStatusText(string);
    }

    /**
     * Method which sets up new players with a score of 0
     * */
    private void initScore() {
        for (int i = 0; i < cardMap.nbPlayers; i++) {
            players[i].setScore(0);
            String text = "[" + String.valueOf(players[i].getScore()) + "]";
            scoreActors[i] = new TextActor(text, Color.WHITE, bgColor, bigFont);
            addActor(scoreActors[i], cardMap.scoreLocations[i]);
        }
    }

    /**
     * Method that updates score for each player
     */
    private void updateScore(int player) {
        //removeActor(scoreActors[player]);
        int displayScore = Math.max(players[player].getScore(), 0);
        String text = "P" + player + "[" + String.valueOf(displayScore) + "]";
        scoreActors[player] = new TextActor(text, Color.WHITE, bgColor, bigFont);
        addActor(scoreActors[player], cardMap.scoreLocations[player]);
    }

    /**
     * Method which initialises the game environment
     */
    private void initGame() {

        playingArea = new Hand(deck);
        for (int i = 0; i < cardMap.nbPlayers; i++) {
            players[i].setHand(new Hand(deck));
            players[i].setPlayingArea(playingArea);
        }

        cardManager.dealingOut(players, cardMap.nbPlayers, cardMap.nbStartCards,
                cardMap.nbFaceUpCards, playingArea, properties, pack);

        playingArea.setView(this, new RowLayout(cardMap.trickLocation,
                (playingArea.getNumberOfCards() + 2) * cardMap.trickWidth));

        playingArea.draw();
        for (int i = 0; i < cardMap.nbPlayers; i++) {
            players[i].getHand().sort(Hand.SortType.SUITPRIORITY, false);
        }

        // Set up human player for interaction
        CardListener cardListener = new CardAdapter()  // Human Player plays card
        {
            public void leftDoubleClicked(Card card) {
                selected = card;
                players[0].getHand().setTouchEnabled(false);
            }
        };

        players[0].getHand().addCardListener(cardListener);
        // graphics
        RowLayout[] layouts = new RowLayout[cardMap.nbPlayers];
        for (int i = 0; i < cardMap.nbPlayers; i++) {
            layouts[i] = new RowLayout(cardMap.handLocations[i], cardMap.handWidth);
            layouts[i].setRotationAngle(90 * i);
            // layouts[i].setStepDelay(10);
            players[i].getHand().setView(this, layouts[i]);
            players[i].getHand().setTargetArea(new TargetArea(cardMap.trickLocation));
            players[i].getHand().draw();
        }
    }

    /**
     * Draws a card from the public card pool and place it in the hand, selecting the card to be discarded
     */
    private Card applyAutoMovement(Hand hand, String nextMovement) {
        if (pack.isEmpty()) return null;
        String[] cardStrings = nextMovement.split("-");
        String cardDealtString = cardStrings[0];
        // Select card
        Card dealt = cardManager.getCardFromList(pack.getCardList(), cardDealtString);
        if (dealt != null) {
            dealt.removeFromHand(false);
            hand.insert(dealt, true);
        } else {
            System.out.println("cannot draw card: " + cardDealtString + " - hand: " + hand);
        }
        // Prepare to discard card
        if (cardStrings.length > 1) {
            String cardDiscardString = cardStrings[1];
            return cardManager.getCardFromList(hand.getCardList(), cardDiscardString);
        } else {
            return null;
        }
    }

    private void playGame() {

        int roundNumber = 1;// Default start from round 1

        for (int i = 0; i < cardMap.nbPlayers; i++) updateScore(i);

        List<Card> cardsPlayed = new ArrayList<>();
        logInfo.addRoundInfoToLog(roundNumber);

        int nextPlayer = 0;
        // Maximum of 4 rounds
        while (roundNumber <= 4) {

            selected = null;
            boolean finishedAuto = false;

            // Auto play according to config file
            if (isAuto) {

                int nextPlayerAutoIndex = players[nextPlayer].getAutoIndexHand();

                List<String> nextPlayerMovement = players[nextPlayer].getPlayerAutoMovements();
                String nextMovement = "";

                if (nextPlayerMovement.size() > nextPlayerAutoIndex) {

                    nextMovement = nextPlayerMovement.get(nextPlayerAutoIndex);
                    nextPlayerAutoIndex++;

                    players[nextPlayer].setAutoIndexHand(nextPlayerAutoIndex);

                    Hand nextHand = players[nextPlayer].getHand();

                    // Apply movement for player
                    selected = applyAutoMovement(nextHand, nextMovement);
                    delay(delayTime);

                    selected.removeFromHand(true);

                } else {
                    finishedAuto = true;
                }
            }

            if (!isAuto || finishedAuto) {
                if (0 == nextPlayer) {

                    players[0].getHand().setTouchEnabled(true);

                    setStatus("Player 0 is playing. Please double click on a card to discard");
                    selected = null;
                    // Draw a card first
                    cardManager.dealACardToHand(players[0].getHand(), pack);
                    // Wait for user to double click to select a card
                    while (null == selected) delay(delayTime);
                    selected.removeFromHand(true);
                } else {
                    setStatusText("Player " + nextPlayer + " thinking...");
                    cardManager.randomSelectCard(players[nextPlayer].getHand());
                    selected = players[nextPlayer].selectRemoveCard();
                    selected.removeFromHand(true);
                }
            }

            logInfo.addCardPlayedToLog(nextPlayer, players[nextPlayer].getHand().getCardList());

            if (selected != null) {
                cardsPlayed.add(selected);
                selected.setVerso(false);  // In case it is upside down
                delay(delayTime);
                // End Follow
            }

            nextPlayer = (nextPlayer + 1) % cardMap.nbPlayers;

            if (nextPlayer == 0) {
                roundNumber++;
                logInfo.addEndOfRoundToLog(players);

                if (roundNumber <= 4) {
                    logInfo.addRoundInfoToLog(roundNumber);
                }
            }
            if (roundNumber > 4) {
                algorithm.calculateScoreEndOfRound();// calculate everyone's final score
            }
            delay(delayTime);
        }
    }

    /**
     * Program entry point
     */
    public String runApp() {
        setTitle("LuckyThirteen (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
        setStatusText("Initializing...");

        playerFactory.createPlayers(properties);

        initGame();

        initScore();

        if (playingArea == null) {
            System.out.println("playingArea is null");
        }
        algorithm = new Algorithm(players, playingArea);

        playGame();

        for (int i = 0; i < cardMap.nbPlayers; i++) updateScore(i);
        int maxScore = 0;
        for (int i = 0; i < cardMap.nbPlayers; i++)
            if (players[i].getScore() > maxScore) maxScore = players[i].getScore();
        List<Integer> winners = new ArrayList<Integer>();
        for (int i = 0; i < cardMap.nbPlayers; i++) if (players[i].getScore() == maxScore) winners.add(i);
        String winText;
        if (winners.size() == 1) {
            winText = "Game over. Winner is player: " + winners.iterator().next();
        } else {
            winText = "Game Over. Drawn winners are players: " + String.join(", ",
                    winners.stream().map(String::valueOf).collect(Collectors.toList()));
        }
        addActor(new Actor("sprites/gameover.gif"), cardMap.textLocation);
        setStatusText(winText);
        refresh();

        logInfo.addEndOfGameToLog(winners, players);
        return logInfo.getLogResult().toString();

    }

}
