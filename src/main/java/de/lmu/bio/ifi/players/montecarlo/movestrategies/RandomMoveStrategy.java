package de.lmu.bio.ifi.players.montecarlo.movestrategies;

import de.lmu.bio.ifi.OthelloGame;

import java.util.Random;

public class RandomMoveStrategy implements MoveStrategy{
    @Override
    public long getMove(OthelloGame game, boolean isPlayerOne, Random random) {
        long possibleMoves = game.getValidMoves(isPlayerOne);
        if (Long.bitCount(possibleMoves) <= 1) {
            return possibleMoves;
        } else {
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
