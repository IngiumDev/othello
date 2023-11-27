package de.lmu.bio.ifi.players.montecarlo.movestrategies;

import de.lmu.bio.ifi.OthelloGame;
import de.lmu.bio.ifi.players.montecarlo.MonteCarloTreeSearch;

import java.util.Random;


public class MatrixMoveStrategy implements MoveStrategy {

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
            for (int i = 0; i < 64; i++) {
                long bit = 1L << i;
                long testMove = possibleMoves & bit;
                if (testMove != 0L) {
                    OthelloGame tempGameMove = game.copy();
                    int score = 0;
                    // Make the move
                    tempGameMove.forceMakeMove(isPlayerOne, testMove);
                    // Get the score
                    for (int y = 0; y < OthelloGame.BOARD_SIZE; y++) {
                        for (int x = 0; x < OthelloGame.BOARD_SIZE; x++) {
                            int disc = game.getCell(x, y);
                            if (disc == myPlayerDisc) {
                                score += MonteCarloTreeSearch.WEIGHT_MATRIX[y][x];
                            } else if (disc == opponentDisc) {
                                score -= MonteCarloTreeSearch.WEIGHT_MATRIX[y][x];
                            }
                        }
                    }
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

