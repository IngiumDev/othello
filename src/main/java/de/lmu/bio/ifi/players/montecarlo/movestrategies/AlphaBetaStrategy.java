package de.lmu.bio.ifi.players.montecarlo.movestrategies;

import de.lmu.bio.ifi.OthelloGame;
import de.lmu.bio.ifi.players.AlphaBetaPlayer;

import java.util.HashMap;
import java.util.Random;

public class AlphaBetaStrategy implements MoveStrategy {
    private final HashMap<String, Integer> FIRST_PHASE_WEIGHTS;
    private final HashMap<String, Integer> SECOND_PHASE_WEIGHTS;
    private final HashMap<String, Integer> THIRD_PHASE_WEIGHTS;

    public AlphaBetaStrategy() {
        FIRST_PHASE_WEIGHTS = new HashMap<>();
        FIRST_PHASE_WEIGHTS.put("MATRIX", 1);
        FIRST_PHASE_WEIGHTS.put("MOBILITY", 3);
        FIRST_PHASE_WEIGHTS.put("STABILITY", 0);
        FIRST_PHASE_WEIGHTS.put("DISCS", 0);
        FIRST_PHASE_WEIGHTS.put("FRONTIER", 3);
        FIRST_PHASE_WEIGHTS.put("CORNER", 50);
        FIRST_PHASE_WEIGHTS.put("PARITY", 2);

        SECOND_PHASE_WEIGHTS = new HashMap<>();
        SECOND_PHASE_WEIGHTS.put("MATRIX", 1);
        SECOND_PHASE_WEIGHTS.put("MOBILITY", 2);
        SECOND_PHASE_WEIGHTS.put("STABILITY", 0);
        SECOND_PHASE_WEIGHTS.put("DISCS", 0);
        SECOND_PHASE_WEIGHTS.put("FRONTIER", 3);
        SECOND_PHASE_WEIGHTS.put("CORNER", 50);
        SECOND_PHASE_WEIGHTS.put("PARITY", 2);

        THIRD_PHASE_WEIGHTS = new HashMap<>();
        THIRD_PHASE_WEIGHTS.put("MATRIX", 0);
        THIRD_PHASE_WEIGHTS.put("MOBILITY", 0);
        THIRD_PHASE_WEIGHTS.put("STABILITY", 0);
        THIRD_PHASE_WEIGHTS.put("DISCS", 5);
        THIRD_PHASE_WEIGHTS.put("FRONTIER", 0);
        THIRD_PHASE_WEIGHTS.put("CORNER", 5);
        THIRD_PHASE_WEIGHTS.put("PARITY", 2);
    }

    /**
     * @param game
     * @param isPlayerOne
     * @param random
     * @return
     */
    @Override
    public long getMove(OthelloGame game, boolean isPlayerOne, Random random) {
        long possibleMoves = game.getValidMoves(isPlayerOne);
        int numberOfSetBits = Long.bitCount(possibleMoves);
        if (numberOfSetBits <= 1) {
            return possibleMoves;
        } else {
            return AlphaBetaPlayer.findBestMove(game, possibleMoves, 0, isPlayerOne, FIRST_PHASE_WEIGHTS, SECOND_PHASE_WEIGHTS, THIRD_PHASE_WEIGHTS, 1);
        }

    }
}
