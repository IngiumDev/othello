package de.lmu.bio.ifi;

public class TranspositionEntry {
    public int depth;
    public int value;

    public TranspositionEntry(int depth, int bestScore) {
        this.depth = depth;
        this.value = bestScore;
    }

}
