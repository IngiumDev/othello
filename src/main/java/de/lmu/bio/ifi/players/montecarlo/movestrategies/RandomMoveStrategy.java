package de.lmu.bio.ifi.players.montecarlo.movestrategies;

import de.lmu.bio.ifi.OthelloGame;

import java.util.Random;

public class RandomMoveStrategy implements MoveStrategy{
    @Override
    public long getMove(OthelloGame game, boolean isPlayerOne, Random random) {
        long possibleMoves = game.getValidMoves(isPlayerOne);
        int numberOfSetBits = Long.bitCount(possibleMoves);
        if (numberOfSetBits <= 1) {
            return possibleMoves;
        } else {
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
