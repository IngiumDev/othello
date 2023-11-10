package de.lmu.bio.ifi.players;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;
import szte.mi.Player;

import java.util.List;
import java.util.Random;

public class AIPlayer implements Player {
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
    private final int DEPTH = 6;
    private boolean isPlayerOne;
    public OthelloGame othelloGame;

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
        if (prevMove == null) {
            othelloGame.makeMove(!isPlayerOne, -1, -1);
        }
        // If the opponent moved record the move
        else {
            othelloGame.makeMove(!isPlayerOne, prevMove.x, prevMove.y);
        }
        List<Move> moves = othelloGame.getPossibleMoves(isPlayerOne);
        if (moves == null || moves.isEmpty()) {
            othelloGame.makeMove(isPlayerOne, -1, -1);
            return null;
        }
        // AI Logic
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        for (Move move : moves) {
            OthelloGame newGame = othelloGame.copy();
            newGame.makeMove(isPlayerOne, move.x, move.y);
            int score = miniMaxBoard(newGame, DEPTH, !isPlayerOne, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        othelloGame.makeMove(isPlayerOne, bestMove.x, bestMove.y);
        long elapsedTime = System.currentTimeMillis() - startTime;
        //System.out.println("Time: " + elapsedTime + "ms");
        return bestMove;
    }

    private int miniMaxBoard(OthelloGame othelloGame, int depth, boolean isPlayerOne, int minValue, int maxValue) {
        // breakout condition
        if (depth == 0 || othelloGame.gameStatus() != GameStatus.RUNNING) {
            return scoreBoard(othelloGame.getBoard(), isPlayerOne);
        }
        // Get moves
        List<Move> moves = othelloGame.getPossibleMoves(isPlayerOne);
        // TODO: order moves by score

        int bestScore = isPlayerOne ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        // sort type 1
        /*
        moves.sort((move1, move2) -> {
            // Apply the moves to the current game state
            OthelloGame tempGame = othelloGame.copy();
            tempGame.makeMove(isPlayerOne, move1.x, move1.y);
            int score1 = scoreBoard(tempGame.getBoard(), isPlayerOne);

            OthelloGame tempGame2 = othelloGame.copy();

            tempGame2.makeMove(isPlayerOne, move2.x, move2.y);
            int score2 = scoreBoard(tempGame2.getBoard(), isPlayerOne);

            // Compare the scores
            return isPlayerOne ? score2 - score1 : score1 - score2;
        });
*/
        // Sort with map
/*
        HashMap<Move, Integer> moveScores = new HashMap<>();
        for (Move move : moves) {
            OthelloGame tempGame = othelloGame.copy();
            tempGame.makeMove(isPlayerOne, move.x, move.y);
            score = scoreBoard(tempGame.getBoard(), isPlayerOne);
            moveScores.put(move, score);
        }

        moves.sort((move1, move2) -> {
            int score1 = moveScores.get(move1);
            int score2 = moveScores.get(move2);
            return isPlayerOne ? score2 - score1 : score1 - score2;
        });
*/

        // If more than 5, we only want to look at the first 5
        /*int size = moves.size();
        if (size > 5) {
            moves = moves.subList(0,5);
        }*/
        for (Move move : moves) {
            OthelloGame newGame = othelloGame.copy();
            newGame.makeMove(isPlayerOne, move.x, move.y);
            score = miniMaxBoard(newGame, depth - 1, !isPlayerOne, minValue, maxValue);

            if (isPlayerOne) {
                bestScore = Math.max(bestScore, score);
                minValue = Math.max(minValue, score);
            } else {
                bestScore = Math.min(bestScore, score);
                maxValue = Math.min(maxValue, score);
            }

            if (maxValue <= minValue) {
                break;
            }
        }
        return bestScore;
    }


    private int scoreBoard(int[][] board, boolean isPlayerOne) {
        int myPlayerDisc = isPlayerOne ? 1 : 2;
        int opponentDisc = isPlayerOne ? 2 : 1;
        int score = 0;
        // Score with weight matrix

        for (int y = 0; y < board.length; y++) {
            int[] row = board[y];
            for (int x = 0; x < row.length; x++) {
                int disc = row[x];
                if (disc == myPlayerDisc) {
                    score += WEIGHT_MATRIX[y][x];
                } else if (disc == opponentDisc) {
                    score -= WEIGHT_MATRIX[y][x];
                }
            }
        }


        return score;
    }
}
