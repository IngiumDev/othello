package de.lmu.bio.ifi.players.montecarlo.movestrategies;

import de.lmu.bio.ifi.BitMasks;
import de.lmu.bio.ifi.OthelloGame;

import java.util.Random;

public class CornerMoveStrategy implements MoveStrategy{
    @Override
    public long getMove(OthelloGame game, boolean isPlayerOne, Random random) {
        long possibleMoves = game.getValidMoves(isPlayerOne);
        if (Long.bitCount(possibleMoves) <= 1) {
            return possibleMoves;
        } else {
            // Find if there is a corner move
            if ((possibleMoves & BitMasks.ALL_CORNER_POSITIONS) != 0L) {
                if ((possibleMoves & BitMasks.TOP_LEFT_CORNER) != 0L) {
                    return BitMasks.TOP_LEFT_CORNER;
                } else if ((possibleMoves & BitMasks.TOP_RIGHT_CORNER) != 0L) {
                    return BitMasks.TOP_RIGHT_CORNER;
                } else if ((possibleMoves & BitMasks.BOTTOM_LEFT_CORNER) != 0L) {
                    return BitMasks.BOTTOM_LEFT_CORNER;
                } else if ((possibleMoves & BitMasks.BOTTOM_RIGHT_CORNER) != 0L) {
                    return BitMasks.BOTTOM_RIGHT_CORNER;
                }
            }
            // Remove terrible moves if possible
            if ((possibleMoves & ~BitMasks.TERRIBLE_MOVES_1) != 0L) {
                possibleMoves &= ~BitMasks.TERRIBLE_MOVES_1;
            }
            // Remove terrible moves 2 if possible
            if ((possibleMoves & ~BitMasks.TERRIBLE_MOVES_2) != 0L) {
                possibleMoves &= ~BitMasks.TERRIBLE_MOVES_2;
            }
            // Remove terrible moves 3 if possible
            if ((possibleMoves & ~BitMasks.TERRIBLE_MOVES_3) != 0L) {
                possibleMoves &= ~BitMasks.TERRIBLE_MOVES_3;
            }
            // Get a random set bit from possibleMoves
            int numberOfSetBits = Long.bitCount(possibleMoves);
            int randomBitIndex = random.nextInt(numberOfSetBits);
            long move = Long.highestOneBit(possibleMoves);
            for (int i = 0; i < randomBitIndex; i++) {
                possibleMoves ^= move; // unset the current highest set bit
                move = Long.highestOneBit(possibleMoves);
            }
            return move;
        }
    }

}
