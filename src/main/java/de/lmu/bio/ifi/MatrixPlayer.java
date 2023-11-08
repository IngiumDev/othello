package de.lmu.bio.ifi;

import szte.mi.Move;
import java.util.List;
public class MatrixPlayer {
    private static final int[][] WEIGHT_MATRIX = {
            {120, -20, 20, 5, 5, 20, -20, 120},
            {-20, -40, -5, -5, -5, -5, -40, -20},
            {20, -5, 15, 3, 3, 15, -5, 20},
            {5, -5, 3, 3, 3, 3, -5, 5},
            {5, -5, 3, 3, 3, 3, -5, 5},
            {20, -5, 15, 3, 3, 15, -5, 20},
            {-20, -40, -5, -5, -5, -5, -40, -20},
            {120, -20, 20, 5, 5, 20, -20, 120}
    };
    public static Move makeMove(boolean isPlayerOne, List<Move> moves) {
        if (moves.size() == 0) {
            return new Move(-1, -1);
        }
        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;
        for (Move move : moves) {
            int score = WEIGHT_MATRIX[move.y][move.x];
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }
}
