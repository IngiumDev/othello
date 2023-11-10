package de.lmu.bio.ifi.players;

import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;
import szte.mi.Player;

import java.util.List;
import java.util.Random;

public class MatrixPlayer implements Player {
    private boolean isPlayerOne;
    private OthelloGame othelloGame;
    private static final int[][] WEIGHT_MATRIX = {
            {20, -3, 11, 8, 8, 11, -3, 20},
            {-3, -7, -4, 1, 1, -4, -7, -3},
            {11, -4, 2, 2, 2, 2, -4, 11},
            {8, 1, 2, -3, -3, 2, 1, 8},
            {8, 1, 2, -3, -3, 2, 1, 8},
            {11, -4, 2, 2, 2, 2, -4, 11},
            {-3, -7, -4, 1, 1, -4, -7, -3},
            {20, -3, 11, 8, 8, 11, -3, 20}
    };

    /**
     * Performs initialization depending on the parameters.
     *
     * @param order Defines the order of the players. Value 0 means
     *              this is the first player to move, 1 means second and so on.
     *              For two-player games only values 0 and 1 are possible.
     * @param t     Gives the remaining overall running time of the player in
     *              ms. Initialization is also counted as running time.
     * @param rnd   source of randomness to be used wherever random
     *              numbers are needed
     */
    @Override
    public void init(int order, long t, Random rnd) {
        assert order == 0 || order == 1;
        othelloGame = new OthelloGame();
        isPlayerOne = order == 0;
    }

    /**
     * Calculates the next move of the player in a two player game.
     * It is assumed that the player is stateful and the game is
     * deterministic, so the parameters only
     * give the previous move of the other player and remaining times.
     *
     * @param prevMove  the previous move of the opponent. It can be null,
     *                  which means the opponent has not moved (or this is the first move).
     * @param tOpponent remaining time of the opponent
     * @param t         remaining time for this player
     */
    @Override
    public Move nextMove(Move prevMove, long tOpponent, long t) {
        // Start timer
        long startTime = System.currentTimeMillis();
        // If the opponent couldn't move record a fake move
        if (prevMove == null) {
            othelloGame.makeMove(!isPlayerOne, -1,-1);
        }
        // If the opponent moved record the move
        else {
            othelloGame.makeMove(!isPlayerOne, prevMove.x, prevMove.y);
        }
        // Get the possible moves
        List<Move> moves = othelloGame.getPossibleMoves(isPlayerOne);
        if (moves == null || moves.isEmpty()) {
            othelloGame.makeMove(isPlayerOne, -1, -1);
            return null;
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
        othelloGame.makeMove(isPlayerOne, bestMove.x, bestMove.y);
        if (prevMove != null) {
            //System.out.println("opponent move: " + prevMove.x + "/" + prevMove.y);
        }
        //System.out.println("My move: " + bestMove.x + "/" + bestMove.y);
        // End timer
        long elapsedTime = System.currentTimeMillis() - startTime;
       // System.out.println("Time: " + elapsedTime + "ms");
        return bestMove;
    }


}
