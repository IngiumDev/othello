package de.lmu.bio.ifi;

import szte.mi.Move;

import java.util.List;

public class RandomPlayer {
    public static Move makeMove(boolean playerOne, List<Move> moves) {

        if (moves.size() == 0) {
            return new Move(-1, -1);
        }
        return moves.get((int) (Math.random() * moves.size()));
    }
}
