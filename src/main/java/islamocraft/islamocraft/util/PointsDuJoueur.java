package islamocraft.islamocraft.util;

public class PointsDuJoueur {
    private int hassanates;
    private int peches;

    public PointsDuJoueur(int hassanates, int peches)
    {
        this.hassanates = hassanates;
        this.peches = peches;
    }

    public int getHassanates() {
        return hassanates;
    }

    public void addHassanates(int hassanates) {
        this.hassanates += hassanates;
    }

    public int getPeches() {
        return peches;
    }

    public void addPeches(int peches) {
        this.peches += peches;
    }

}
