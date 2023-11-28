package de.lmu.bio.ifi.players.montecarlo.movestrategies;

import de.lmu.bio.ifi.BitMasks;

public class MatrixEvaluater {
    public static int getWeightedPieceCount(int myPlayerDisc, int opponentDisc, long playerDiscs, long opponentDiscs) {
        int score = 0;
        for (int i = 0; i < BitMasks.WEIGHT_MATRIX.length; i++) {
            score += Long.bitCount(playerDiscs & BitMasks.WEIGHT_MATRIX[i]) * BitMasks.WEIGHT_MATRIX_SCORES[i];
            score -= Long.bitCount(opponentDiscs & BitMasks.WEIGHT_MATRIX[i]) * BitMasks.WEIGHT_MATRIX_SCORES[i];
        }
        return score;
    }
}
