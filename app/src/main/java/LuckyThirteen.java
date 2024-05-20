// LuckyThirteen.java

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class LuckyThirteen extends CardGame {

    final String trumpImage[] = {"bigspade.gif", "bigheart.gif", "bigdiamond.gif", "bigclub.gif"};

    static public final int seed = 30008;
    static final Random random = new Random(seed);
    private Properties properties;
    private StringBuilder logResult = new StringBuilder();

    public boolean rankGreater(Card card1, Card card2) {
        return card1.getRankId() < card2.getRankId(); // Warning: Reverse rank order of cards (see comment on enum)
    }

    private final String version = "1.0";

    // Generate 52 playing cards
    private final Deck deck = new Deck(Suit.values(), Rank.values(), "cover");

    private int thinkingTime = 2000;
    private int delayTime = 600;

    CarkMap carkMap = new CarkMap();

    private Player[] players = new Player[carkMap.nbPlayers];
    private boolean isAuto = false;
    private Hand playingArea; // Holds the two public cards
    // Shared card pool, used by all four players
    private Hand pack; // Public card pool

    private Actor[] scoreActors = {null, null, null, null};

    Font bigFont = new Font("Arial", Font.BOLD, 36);

    private Algorithm algorithm = null;

    private void initScore() {
        for (int i = 0; i < carkMap.nbPlayers; i++) {
            String text = "[" + String.valueOf(players[i].getScore()) + "]";
            scoreActors[i] = new TextActor(text, Color.WHITE, bgColor, bigFont);
            addActor(scoreActors[i], carkMap.scoreLocations[i]);
        }
    }

    // Update the score display on the UI
    private void updateScore(int player) {
        removeActor(scoreActors[player]);
        int displayScore = Math.max(players[player].getScore(), 0);
        String text = "P" + player + "[" + String.valueOf(displayScore) + "]";
        scoreActors[player] = new TextActor(text, Color.WHITE, bgColor, bigFont);
        addActor(scoreActors[player], carkMap.scoreLocations[player]);
    }

    private void initScores() {
        for (int i = 0; i < carkMap.nbPlayers; i++) {
            players[i].setScore(0);
        }
    }

    private Card selected;

    private void initGame() {
        for (int i = 0; i < carkMap.nbPlayers; i++) {
            players[i].setHand(new Hand(deck));
        }
        playingArea = new Hand(deck);
        dealingOut(players, carkMap.nbPlayers, carkMap.nbStartCards, carkMap.nbFaceUpCards);
        // Draw cards to the public area
        playingArea.setView(this, new RowLayout(carkMap.trickLocation, (playingArea.getNumberOfCards() + 2) * carkMap.trickWidth));
        playingArea.draw();
        // Sort hands
        for (int i = 0; i < carkMap.nbPlayers; i++) {
            players[i].getHand().sort(Hand.SortType.SUITPRIORITY, false);
        }
        // Set up human player for interaction
        CardListener cardListener = new CardAdapter() {
            public void leftDoubleClicked(Card card) {
                // For human player (only at position 0), accept double-clicked card
                selected = card;
                players[0].getHand().setTouchEnabled(false);
            }
        };
        // Bind double-click listener
        players[0].getHand().addCardListener(cardListener);
        // Graphics
        RowLayout[] layouts = new RowLayout[carkMap.nbPlayers];
        // Draw each player's cards
        for (int i = 0; i < carkMap.nbPlayers; i++) {
            layouts[i] = new RowLayout(carkMap.handLocations[i], carkMap.handWidth);
            layouts[i].setRotationAngle(90 * i);
            players[i].getHand().setView(this, layouts[i]);
            players[i].getHand().setTargetArea(new TargetArea(carkMap.trickLocation));
            players[i].getHand().draw();
        }
    }

    // Return random Enum value
    public static <T extends Enum<?>> T randomEnum(Class<T> clazz) {
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    // Return random Card from ArrayList
    public static Card randomCard(ArrayList<Card> list) {
        int x = random.nextInt(list.size());
        return list.get(x);
    }

    private Rank getRankFromString(String cardName) {
        String rankString = cardName.substring(0, cardName.length() - 1);
        Integer rankValue = Integer.parseInt(rankString);

        for (Rank rank : Rank.values()) {
            if (rank.getRankCardValue() == rankValue) {
                return rank;
            }
        }

        return Rank.ACE;
    }

    private Suit getSuitFromString(String cardName) {
        String rankString = cardName.substring(0, cardName.length() - 1);
        String suitString = cardName.substring(cardName.length() - 1);
        for (Suit suit : Suit.values()) {
            if (suit.getSuitShortHand().equals(suitString)) {
                return suit;
            }
        }
        return Suit.CLUBS;
    }

    private Card getCardFromList(List<Card> cards, String cardName) {
        Rank cardRank = getRankFromString(cardName);
        Suit cardSuit = getSuitFromString(cardName);
        for (Card card : cards) {
            if (card.getSuit() == cardSuit && card.getRank() == cardRank) {
                return card;
            }
        }
        return null;
    }

    // Draw a card from the public card pool and place it in the hand, selecting the card to be discarded
    private Card applyAutoMovement(Hand hand, String nextMovement) {
        if (pack.isEmpty()) return null;
        String[] cardStrings = nextMovement.split("-");
        String cardDealtString = cardStrings[0];
        // Select card
        Card dealt = getCardFromList(pack.getCardList(), cardDealtString);
        if (dealt != null) {
            dealt.removeFromHand(false);
            hand.insert(dealt, true);
        } else {
            System.out.println("cannot draw card: " + cardDealtString + " - hand: " + hand);
        }
        // Prepare to discard card
        if (cardStrings.length > 1) {
            String cardDiscardString = cardStrings[1];
            return getCardFromList(hand.getCardList(), cardDiscardString);
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
                Card card = getCardFromList(pack.getCardList(), initialCard);
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
            Card dealt = randomCard(pack.getCardList());
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
                Card card = getCardFromList(pack.getCardList(), initialCard);
                if (card != null) {
                    card.removeFromHand(false);
                    players[i].getHand().insert(card, false); // Add drawn card to the player's hand
                }
            }
        }
        // If config file is insufficient, randomly draw cards to ensure each player starts with two cards
        for (int i = 0; i < nbPlayers; i++) {
            int cardsToDealt = nbCardsPerPlayer - players[i].getHand().getNumberOfCards();
            for (int j = 0; j < cardsToDealt; j++) {
                if (pack.isEmpty()) return;
                Card dealt = randomCard(pack.getCardList());
                dealt.removeFromHand(false);
                players[i].getHand().insert(dealt, false);
            }
        }
    }

    // Implement card drawing logic, moving cards from the public pool to the player's hand
    private void dealACardToHand(Hand hand) {
        if (pack.isEmpty()) return;
        Card dealt = randomCard(pack.getCardList());
        dealt.removeFromHand(false);
        hand.insert(dealt, true);
    }

    private void addCardPlayedToLog(int player, List<Card> cards) {
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

    private void addRoundInfoToLog(int roundNumber) {
        logResult.append("Round" + roundNumber + ":");
    }

    private void addEndOfRoundToLog() {
        logResult.append("Score:");
        for (int i = 0; i < players.length; i++) {
            logResult.append(players[i].getScore() + ",");
        }
        logResult.append("\n");
    }

    private void addEndOfGameToLog(List<Integer> winners) {
        logResult.append("EndGame:");
        for (int i = 0; i < players.length; i++) {
            logResult.append(players[i].getScore() + ",");
        }
        logResult.append("\n");
        logResult.append("Winners:" + String.join(", ", winners.stream().map(String::valueOf).collect(Collectors.toList())));
    }

    private void playGame() {
        int winner = 0;
        int roundNumber = 1; // Default start from round 1
        for (int i = 0; i < carkMap.nbPlayers; i++) updateScore(i);

        List<Card> cardsPlayed = new ArrayList<>();
        addRoundInfoToLog(roundNumber);

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
                        selected = algorithm.getRandomCard(players[nextPlayer].getHand());
                        selected.removeFromHand(true);
                    }
                } else {
                    finishedAuto = true;
                }
            }

            if (!isAuto || finishedAuto) {
                if (0 == nextPlayer) {
                    players[0].getHand().setTouchEnabled(true);

                    setStatusText("Player 0 is playing. Please double click on a card to discard");
                    selected = null;
                    // Draw a card first
                    dealACardToHand(players[0].getHand());
                    // Wait for user to double click to select a card
                    while (null == selected) delay(delayTime);
                    selected.removeFromHand(true);
                } else {
                    setStatusText("Player " + nextPlayer + " thinking...");
                    dealACardToHand(players[nextPlayer].getHand());

                    if (players[nextPlayer].getStrType().equals("basic")) {
                        selected = algorithm.getMinCard(players[nextPlayer].getHand());
                        selected.removeFromHand(true);
                    } else if (players[nextPlayer].getStrType().equals("clever")) {
                        selected = algorithm.getCleverCard(players[nextPlayer].getHand());
                        selected.removeFromHand(true);
                    } else {
                        selected = algorithm.getRandomCard(players[nextPlayer].getHand());
                        selected.removeFromHand(true);
                    }
                }
            }

            addCardPlayedToLog(nextPlayer, players[nextPlayer].getHand().getCardList());

            if (selected != null) {
                cardsPlayed.add(selected);
                selected.setVerso(false); // In case it is upside down
                delay(delayTime);
            }

            nextPlayer = (nextPlayer + 1) % carkMap.nbPlayers;

            if (nextPlayer == 0) {
                roundNumber++;
                addEndOfRoundToLog();

                if (roundNumber <= 4) {
                    addRoundInfoToLog(roundNumber);
                }
            }
            if (roundNumber > 4) {
                algorithm.calculateScoreEndOfRound(); // Calculate each player's final score
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
                Player player = new Player(playerTypes[i], null, 0, new ArrayList<>());
                players[i] = player;
                continue;
            }
            List<String> movements = Arrays.asList(movementString.split(","));
            Player player = new Player(playerTypes[i], null, 0, movements);
            players[i] = player;
        }
    }

    // Program entry point
    public String runApp() {
        setTitle("LuckyThirteen (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
        setStatusText("Initializing...");
        createPlayers(); // Create four players, read auto player actions from config file, initialize player types

        initScores(); // Initialize scores, all set to 0
        initScore(); // Draw the initial scores on the UI

        initGame(); // Initialize each player's hand with 2 cards, the public area with 2 cards, and draw them to specified positions. Bind double-click card event for the human player

        algorithm = new Algorithm(players, playingArea); // Initialize algorithm (including 13 calculation and card discard algorithm)
        playGame(); // Run 4 rounds, each player draws and discards cards (auto players follow config, manual players choose randomly, human players choose by double-clicking). Calculate each player's score at the end

        for (int i = 0; i < carkMap.nbPlayers; i++) updateScore(i); // Update scores on the UI
        int maxScore = 0;
        for (int i = 0; i < carkMap.nbPlayers; i++) if (players[i].getScore() > maxScore) maxScore = players[i].getScore(); // Calculate the highest score among the four players
        List<Integer> winners = new ArrayList<>();
        for (int i = 0; i < carkMap.nbPlayers; i++) if (players[i].getScore() == maxScore) winners.add(i); // Record the highest scorers (winners, could be multiple)
        String winText;
        // Display winners on the bottom left corner
        if (winners.size() == 1) {
            winText = "Game over. Winner is player: " + winners.iterator().next();
        } else {
            winText = "Game Over. Drawn winners are players: " + String.join(", ", winners.stream().map(String::valueOf).collect(Collectors.toList()));
        }
        addActor(new Actor("sprites/gameover.gif"), carkMap.textLocation);
        setStatusText(winText);
        refresh();
        addEndOfGameToLog(winners);

        return logResult.toString();
    }

    public LuckyThirteen(Properties properties) {
        super(700, 700, 30);
        this.properties = properties;
        isAuto = Boolean.parseBoolean(properties.getProperty("isAuto"));
        thinkingTime = Integer.parseInt(properties.getProperty("thinkingTime", "200"));
        delayTime = Integer.parseInt(properties.getProperty("delayTime", "50"));
    }

}
