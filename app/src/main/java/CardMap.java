
import ch.aplu.jgamegrid.Location;

public class CardMap {

    public final int nbPlayers = 4;

    public final int nbStartCards = 2;
    public final int nbFaceUpCards = 2;
    public final int handWidth = 400;
    public final int trickWidth = 40;

    public final Location[] handLocations = {
            new Location(350, 625),
            new Location(75, 350),
            new Location(350, 75),
            new Location(625, 350)
    };
    public final Location[] scoreLocations = {
            new Location(575, 675),
            new Location(25, 575),
            new Location(575, 25),
            // new Location(650, 575)
            new Location(575, 575)
    };

    public final Location trickLocation = new Location(350, 350);
    public final Location textLocation = new Location(350, 450);

}
