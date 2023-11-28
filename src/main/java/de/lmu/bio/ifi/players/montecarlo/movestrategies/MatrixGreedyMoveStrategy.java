package de.lmu.bio.ifi.players.montecarlo.movestrategies;

import de.lmu.bio.ifi.OthelloGame;

import java.util.Random;

import static de.lmu.bio.ifi.OthelloGame.forceTempMakeMove;
import static de.lmu.bio.ifi.players.montecarlo.movestrategies.MatrixEvaluater.getWeightedPieceCount;

// TODO: WRITE THIS CLASS
public class MatrixGreedyMoveStrategy implements MoveStrategy {
    @Override
    public long getMove(OthelloGame game, boolean isPlayerOne, Random random) {
        long possibleMoves = game.getValidMoves(isPlayerOne);
        if (Long.bitCount(possibleMoves) <= 1) {
            return possibleMoves;
        } else {
            int myPlayerDisc = isPlayerOne ? OthelloGame.PLAYER_ONE : OthelloGame.PLAYER_TWO;
            int opponentDisc = isPlayerOne ? OthelloGame.PLAYER_TWO : OthelloGame.PLAYER_ONE;
            // Use Weight matrix to get the best move for the current player
            int bestScore = Integer.MIN_VALUE;
            // for each possible move
            long move = 0L;
            long moves = possibleMoves;
            while (moves != 0) {
                long testMove = Long.lowestOneBit(moves); // get the lowest set bit
                moves ^= testMove; // unset the lowest
                if (testMove != 0L) {
                    OthelloGame tempGameMove = game.copy();
                    int score = 0;
                    // Make the move
                    long playerDiscs = isPlayerOne ? tempGameMove.getPlayerOneBoard() : tempGameMove.getPlayerTwoBoard();
                    long opponentDiscs = isPlayerOne ? tempGameMove.getPlayerTwoBoard() : tempGameMove.getPlayerOneBoard();
                    long[] discs = forceTempMakeMove(isPlayerOne, playerDiscs, opponentDiscs, testMove);
                    playerDiscs = discs[0];
                    opponentDiscs = discs[1];
                    // Get the score
                    score = getWeightedPieceCount(myPlayerDisc, opponentDisc, playerDiscs, opponentDiscs);
                    // Check if it is the best move
                    if (score > bestScore) {
                        bestScore = score;
                        move = testMove;
                    }
                }
            }
            return move;
        }
    }
}
