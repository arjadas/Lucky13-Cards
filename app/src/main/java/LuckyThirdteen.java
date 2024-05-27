import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class LuckyThirdteen extends CardGame {



    static public final int seed = 30008;
    static final Random random = new Random(seed);
    private Properties properties;
    private LogData logInfo = new LogData();


    private final String version = "1.0";

    // Generate 52 playing cards
    private final Deck deck = new Deck(Suit.values(), Rank.values(), "cover");

    private int thinkingTime = 2000;
    private int delayTime = 600;

    public void setStatus(String string) {
        setStatusText(string);
    }

    CardMap cardMap = new CardMap();

    private Player[] players = new Player[cardMap.nbPlayers];
    private boolean isAuto = false;
    private Hand playingArea;
    private Hand pack;

    private Actor[] scoreActors = {null, null, null, null};


    Font bigFont = new Font("Arial", Font.BOLD, 36);

    private Algorithm algorithm = null;

    /**
     * Method which sets up new players with a score of 0
     */
    private void initScore() {
        for (int i = 0; i < cardMap.nbPlayers; i++) {
            players[i].setScore(0);
            String text = "[" + String.valueOf(players[i].getScore()) + "]";
            scoreActors[i] = new TextActor(text, Color.WHITE, bgColor, bigFont);
            addActor(scoreActors[i], cardMap.scoreLocations[i]);
        }
    }


    private void updateScore(int player) {
        //removeActor(scoreActors[player]);
        int displayScore = Math.max(players[player].getScore(), 0);
        String text = "P" + player + "[" + String.valueOf(displayScore) + "]";
        scoreActors[player] = new TextActor(text, Color.WHITE, bgColor, bigFont);
        addActor(scoreActors[player], cardMap.scoreLocations[player]);
    }

    private Card selected;

    private void initGame() {

        playingArea = new Hand(deck);
        for (int i = 0; i < cardMap.nbPlayers; i++) {
            players[i].setHand(new Hand(deck));
            players[i].setPlayingArea(playingArea);

        }

        dealingOut(players, cardMap.nbPlayers, cardMap.nbStartCards, cardMap.nbFaceUpCards);
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


    // return random Enum value

    // Draw a card from the public card pool and place it in the hand, selecting the card to be discarded
    private Card applyAutoMovement(Hand hand, String nextMovement) {
        if (pack.isEmpty()) return null;
        String[] cardStrings = nextMovement.split("-");
        String cardDealtString = cardStrings[0];
        // Select card
        Card dealt = CardManager.getCardFromList(pack.getCardList(), cardDealtString);
        if (dealt != null) {
            dealt.removeFromHand(false);
            hand.insert(dealt, true);
        } else {
            System.out.println("cannot draw card: " + cardDealtString + " - hand: " + hand);
        }
        // Prepare to discard card
        if (cardStrings.length > 1) {
            String cardDiscardString = cardStrings[1];
            return CardManager.getCardFromList(hand.getCardList(), cardDiscardString);
        } else {
            return null;
        }
    }

    // Initialize each player's hand with 2 cards and the public area with 2 cards
    private void dealingOut(Player[] players, int nbPlayers, int nbCardsPerPlayer, int nbSharedCards) {
        pack = deck.toHand(false);
        // Read public cards from config file
        String initialShareKey = "shared.initialcards";
        String initialShareValue = properties.getProperty(initialShareKey);
        if (initialShareValue != null) {
            String[] initialCards = initialShareValue.split(",");
            for (String initialCard : initialCards) {
                if (initialCard.length() <= 1) {
                    continue;
                }
                // Draw a card from the public card pool
                Card card = CardManager.getCardFromList(pack.getCardList(), initialCard);
                if (card != null) {
                    card.removeFromHand(true);
                    // Insert into the public area
                    playingArea.insert(card, true);
                }
            }
        }
        int cardsToShare = nbSharedCards - playingArea.getNumberOfCards();

        for (int j = 0; j < cardsToShare; j++) {
            if (pack.isEmpty()) return;
            // Randomly draw a card from the public card pool
            Card dealt = CardManager.randomCard(pack.getCardList());
            dealt.removeFromHand(true);
            // Insert into the public area
            playingArea.insert(dealt, true);
        }

        // Initialize each player's hand with two cards
        for (int i = 0; i < nbPlayers; i++) {
            String initialCardsKey = "players." + i + ".initialcards";
            String initialCardsValue = properties.getProperty(initialCardsKey);
            if (initialCardsValue == null) {
                continue;
            }
            String[] initialCards = initialCardsValue.split(",");
            for (String initialCard : initialCards) {
                if (initialCard.length() <= 1) {
                    continue;
                }
                Card card = CardManager.getCardFromList(pack.getCardList(), initialCard);
                if (card != null) {
                    card.removeFromHand(false);
                    players[i].getHand().insert(card, false);
                }
            }
        }
        // If config file is insufficient, randomly draw cards to ensure each player starts with two cards
        for (int i = 0; i < nbPlayers; i++) {
            int cardsToDealt = nbCardsPerPlayer - players[i].getHand().getNumberOfCards();
            for (int j = 0; j < cardsToDealt; j++) {
                if (pack.isEmpty()) return;
                Card dealt = CardManager.randomCard(pack.getCardList());
                dealt.removeFromHand(false);
                players[i].getHand().insert(dealt, false);
            }
        }
    }

    // Implement card drawing logic, moving cards from the public pool to the player's hand
    private void dealACardToHand(Hand hand) {
        if (pack.isEmpty()) return;
        Card dealt = CardManager.randomCard(pack.getCardList());
        dealt.removeFromHand(false);
        hand.insert(dealt, true);
    }

    private void playGame() {
        // End trump suit
        int winner = 0;
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

                    if (selected != null) {
                        selected.removeFromHand(true);
                    } else {
                        selected = CardManager.getRandomCard(players[nextPlayer].getHand());
                        selected.removeFromHand(true);
                    }
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
                    dealACardToHand(players[0].getHand());
                    // Wait for user to double click to select a card
                    while (null == selected) delay(delayTime);
                    selected.removeFromHand(true);
                } else {
                    setStatusText("Player " + nextPlayer + " thinking...");

                    randomSelectCard(players[nextPlayer].getHand());

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

    // Randomly draw a card from the card pool
    private void randomSelectCard(Hand hand) {
        dealACardToHand(hand);

        delay(thinkingTime);
    }

    private void createPlayers() {

        String player0Type = properties.getProperty("players.0");
        String player1Type = properties.getProperty("players.1");
        String player2Type = properties.getProperty("players.2");
        String player3Type = properties.getProperty("players.3");

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
                Player player = PlayerFactory.getPlayer(playerTypes[i], null, null, 0, new ArrayList<>());   //new Player(playerTypes[i],null,0,new ArrayList<>());
                players[i] = player;
                continue;
            }
            List<String> movements = Arrays.asList(movementString.split(","));
            Player player = PlayerFactory.getPlayer(playerTypes[i], null, null, 0, movements); //new Player(playerTypes[i],null,0,movements);
            players[i] = player;

        }
    }

    // Program entry point
    public String runApp() {
        setTitle("LuckyThirteen (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
        setStatusText("Initializing...");

        createPlayers();

        initScore();

        initGame();

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

    public LuckyThirdteen(Properties properties) {
        super(700, 700, 30);
        this.properties = properties;
        isAuto = Boolean.parseBoolean(properties.getProperty("isAuto"));
        thinkingTime = Integer.parseInt(properties.getProperty("thinkingTime", "200"));
        delayTime = Integer.parseInt(properties.getProperty("delayTime", "50"));
    }

}
