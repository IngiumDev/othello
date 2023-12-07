package de.lmu.bio.ifi.players.montecarlo.movestrategies;

import de.lmu.bio.ifi.BitMasks;
import de.lmu.bio.ifi.OthelloGame;

import java.util.Random;

public class CornerMoveStrategy implements MoveStrategy{
    @Override
    public long getMove(OthelloGame game, boolean isPlayerOne, Random random) {
        long possibleMoves = game.getValidMoves(isPlayerOne);
        int numberOfSetBits = Long.bitCount(possibleMoves);
        if (numberOfSetBits <= 1) {
            return possibleMoves;
        } else {
            // Find if there is a corner move
            long possibleCornerMoves = possibleMoves & BitMasks.ALL_CORNER_POSITIONS;
            if (possibleCornerMoves != 0L) {
                // Get a random set bit from possibleCornerMoves
                int randomBitIndex = random.nextInt(Long.bitCount(possibleCornerMoves));
                for (int i = 0; i <= randomBitIndex; i++) {
                    long move = Long.lowestOneBit(possibleCornerMoves);
                    if (i == randomBitIndex) {
                        return move;
                    }
                    possibleCornerMoves ^= move; // unset the current lowest set bit
                }
            }
            // Remove terrible moves if possible
         /*  if ((possibleMoves & ~BitMasks.TERRIBLE_MOVES_COMBINED) != 0L) {
                possibleMoves &= ~BitMasks.TERRIBLE_MOVES_COMBINED;
            }



            // Remove terrible moves 2 if possible
            if ((possibleMoves & ~BitMasks.TERRIBLE_MOVES_2) != 0L) {
                possibleMoves &= ~BitMasks.TERRIBLE_MOVES_2;
            }
            // Remove terrible moves 3 if possible
            if ((possibleMoves & ~BitMasks.TERRIBLE_MOVES_3) != 0L) {
                possibleMoves &= ~BitMasks.TERRIBLE_MOVES_3;
            }*/
            numberOfSetBits = Long.bitCount(possibleMoves);
            // Get a random set bit from possibleMoves
            int randomBitIndex = random.nextInt(numberOfSetBits);
            for (int i = 0; i <= randomBitIndex; i++) {
                long move = Long.lowestOneBit(possibleMoves);
                if (i == randomBitIndex) {
                    return move;
                }
                possibleMoves ^= move; // unset the current lowest set bit
            }
        }
        return 0; // should never reach here
    }

}
