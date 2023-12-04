package de.lmu.bio.ifi.players;

import de.lmu.bio.ifi.BitMasks;
import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;
import szte.mi.Player;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class NegaMaxPlayer implements Player {

    private final static long LEFT_RIGHT_EDGE = 0x8181818181818181L;
    private static final int[][] WEIGHT_MATRIX = {
            {120, -20, 20, 5, 5, 20, -20, 120},
            {-20, -40, -5, -5, -5, -5, -40, -20},
            {20, -5, 15, 3, 3, 15, -5, 20},
            {5, -5, 3, 3, 3, 3, -5, 5},
            {5, -5, 3, 3, 3, 3, -5, 5},
            {20, -5, 15, 3, 3, 15, -5, 20},
            {-20, -40, -5, -5, -5, -5, -40, -20},
            {120, -20, 20, 5, 5, 20, -20, 120}
    };
    private static final Map<String, Integer> OPENING_WEIGHTS = Map.of(
            "mobility", 5,
            "stability", 1,
            "cornerOccupancy", 25,
            "potentialMobility", 5,
            "frontierDiscs", -5,
            "discSquare", 10
    );

    private static final Map<String, Integer> MIDGAME_WEIGHTS = Map.of(
            "mobility", 3,
            "stability", 5,
            "cornerOccupancy", 20,
            "potentialMobility", 10,
            "frontierDiscs", -10,
            "discSquare", 10
    );

    private static final Map<String, Integer> ENDGAME_WEIGHTS = Map.of(
            "mobility", 5,
            "stability", 15,
            "cornerOccupancy", 30,
            "potentialMobility", 5,
            "frontierDiscs", -10,
            "discSquare", 10,
            "discParity", 10
    );
    private static final int OPENING = 20;
    private static final int MIDGAME = 40;
    private OthelloGame mainGame;
    private boolean isPlayerOne;

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
        mainGame = new OthelloGame();
        isPlayerOne = (order == 0);
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
        if (prevMove != null) {
            mainGame.makeMove(!isPlayerOne, prevMove.x, prevMove.y);
        }
        if (prevMove == null) {
            if (!mainGame.getMoveHistory().isEmpty()) {
                mainGame.makeMove(!isPlayerOne, -1, -1);
            }
        }

        List<Move> moves = mainGame.parseValidMovesToMoveList(mainGame.getValidMoves(isPlayerOne));
        if (moves == null || moves.isEmpty()) {
            mainGame.makeMove(isPlayerOne, -1, -1);
            return null;
        } else if (moves.size() == 1) {
            mainGame.makeMove(isPlayerOne, moves.get(0).x, moves.get(0).y);
            return moves.get(0);
        }


        // Get Best Move
        Move bestMove = null;
        int maxScore = Integer.MIN_VALUE;
        int depth = 1;
        long startTime = System.currentTimeMillis();

        // Estimate the number of moves left in the game
        int estimatedMovesLeft = estimateMovesLeft(mainGame);
        // Calculate the maximum time for this move
        long maxTime = t / estimatedMovesLeft - 10;  // Leave a buffer of 1 second

        while (System.currentTimeMillis() - startTime < maxTime && depth <= estimatedMovesLeft) {
            for (Move move : moves) {
                OthelloGame clonedGame = mainGame.copy();
                if (clonedGame.gameStatus() == GameStatus.RUNNING) {
                    clonedGame.makeMove(isPlayerOne, move.x, move.y);
                    int score = -negamax(clonedGame, depth - 1, !isPlayerOne, startTime, maxTime);
                    if (score > maxScore) {
                        maxScore = score;
                        bestMove = move;
                    }
                }
                if (clonedGame.gameStatus() == GameStatus.PLAYER_1_WON && isPlayerOne) {
                    return move;
                } else if (clonedGame.gameStatus() == GameStatus.PLAYER_2_WON && !isPlayerOne) {
                    return move;
                }
            }
            depth++;
        }
        if (bestMove == null) {
            bestMove = moves.get(0);
            System.out.println("No move found, playing random move");
        }
        System.out.println(depth);

        mainGame.makeMove(isPlayerOne, bestMove.x, bestMove.y);
        return bestMove;
    }

    private int negamax(OthelloGame game, int depth, boolean isPlayerOne, long startTime, long maxTime) {
        if (depth == 0 || game.gameStatus() != GameStatus.RUNNING || System.currentTimeMillis() - startTime > maxTime) {
            return evaluateGameState(game, isPlayerOne);
        }

        int maxScore = Integer.MIN_VALUE;
        List<Move> moves = game.parseValidMovesToMoveList(game.getValidMoves(isPlayerOne));
        if (moves == null || moves.isEmpty()) {
            OthelloGame clonedGame = game.copy();
            clonedGame.makeMove(isPlayerOne, -1, -1);
            int score = -negamax(clonedGame, depth - 1, !isPlayerOne, startTime, maxTime);
            maxScore = Math.max(maxScore, score);
        }
        for (Move move : moves) {
            OthelloGame clonedGame = game.copy();
            clonedGame.makeMove(isPlayerOne, move.x, move.y);
            int score = -negamax(clonedGame, depth - 1, !isPlayerOne, startTime, maxTime);
            maxScore = Math.max(maxScore, score);
        }

        return maxScore;
    }

    private int evaluateGameState(OthelloGame game, boolean isPlayerOne) {
        // Calculate heuristics
        int mobility = calculateMobility(game, isPlayerOne);
        int stability = calculateStability(game, isPlayerOne);
        int cornerOccupancy = calculateCornerOccupancy(game, isPlayerOne);
        int potentialMobility = calculatePotentialMobility(game, isPlayerOne);
        int frontierDiscs = calculateFrontierDiscs(game, isPlayerOne);
        int discSquare = calculateDiscSquare(game, isPlayerOne);
        int discParity = calculateDiscParity(game, isPlayerOne);

        // Determine game phase and corresponding weights
        int phase = game.getAmountOfChipsPlaced();
        Map<String, Integer> weights;
        if (phase <= OPENING) {
            weights = OPENING_WEIGHTS;
        } else if (phase <= MIDGAME) {
            weights = MIDGAME_WEIGHTS;
        } else {
            weights = ENDGAME_WEIGHTS;
        }

        // Calculate weighted sum of features
        return weights.get("mobility") * mobility
                + weights.get("stability") * stability
                + weights.get("cornerOccupancy") * cornerOccupancy
                + weights.get("potentialMobility") * potentialMobility
                + weights.get("frontierDiscs") * frontierDiscs
                + weights.get("discSquare") * discSquare
                + weights.getOrDefault("discParity", 0) * discParity;
    }

    private int estimateMovesLeft(OthelloGame game) {
        // This is a simple estimation. You might want to refine this based on your game's rules and state.
        return OthelloGame.BOARD_SIZE * OthelloGame.BOARD_SIZE - game.getAmountOfChipsPlaced();
    }

    private int calculateMobility(OthelloGame game, boolean isPlayerOne) {
        return Long.bitCount(game.getValidMoves(isPlayerOne));
    }

    private int calculateStability(OthelloGame game, boolean isPlayerOne) {
        return countStableCoins(game, isPlayerOne);
    }

    private int calculateCornerOccupancy(OthelloGame game, boolean isPlayerOne) {
        long playerCoins = game.getPlayerBoard(isPlayerOne);
        long corners = 0x8100000000000081L;  // Bitmask for the corners
        return Long.bitCount(playerCoins & corners);
    }

    private int calculatePotentialMobility(OthelloGame game, boolean isPlayerOne) {
        long opponentCoins = game.getPlayerBoard(!isPlayerOne);
        long emptySquares = ~game.getPlayerBoard(true) & ~game.getPlayerBoard(false);
        long opponentAdjacentSquares = getAdjacentSquares(opponentCoins) & emptySquares;
        return Long.bitCount(opponentAdjacentSquares);
    }

    private int calculateFrontierDiscs(OthelloGame game, boolean isPlayerOne) {
        long playerCoins = game.getPlayerBoard(isPlayerOne);
        long emptySquares = ~game.getPlayerBoard(true) & ~game.getPlayerBoard(false);
        long frontierDiscs = playerCoins & getAdjacentSquares(emptySquares);
        return Long.bitCount(frontierDiscs);
    }

    private int calculateDiscSquare(OthelloGame game, boolean isPlayerOne) {
        long playerCoins = game.getPlayerBoard(isPlayerOne);
        int score = 0;
        for (int i = 0; i < 64; i++) {
            if ((playerCoins & (1L << i)) != 0) {
                int x = i / 8;
                int y = i % 8;
                score += WEIGHT_MATRIX[y][x];
            }
        }
        return score;
    }

    private int calculateDiscParity(OthelloGame game, boolean isPlayerOne) {
        int myDiscs = Long.bitCount(game.getPlayerBoard(isPlayerOne));
        int opponentDiscs = Long.bitCount(game.getPlayerBoard(!isPlayerOne));
        return myDiscs - opponentDiscs;
    }

    private long getAdjacentSquares(long coins) {
        long left = (coins << 1) & BitMasks.LEFT_MASK;
        long right = (coins >> 1) & BitMasks.RIGHT_MASK;
        long up = (coins << 8);
        long down = (coins >> 8);
        return left | right | up | down;
    }

    private int countStableCoins(OthelloGame game, boolean isPlayerOne) {
        int stableCoins = 0;
        long playerCoins = game.getPlayerBoard(isPlayerOne);
        long opponentCoins = game.getPlayerBoard(!isPlayerOne);

        // Iterate over all discs
        for (int i = 0; i < 64; i++) {
            long disc = 1L << i;

            // Skip if the disc is not present or belongs to the opponent
            if ((playerCoins & disc) == 0) continue;

            // Check if the disc is stable
            boolean stable = isDiscStable(disc, playerCoins, opponentCoins);

            if (stable) stableCoins++;
        }

        return stableCoins;
    }

    private boolean isDiscStable(long disc, long playerCoins, long opponentCoins) {
        // Directions towards the nearest corners

        for (int direction : BitMasks.BIT_DIRECTIONS) {
            for (long d = disc; (d & 0x8080808080808080L) == 0; d <<= direction) {
                // If we encounter an opponent's disc, the disc is not stable
                if ((d & opponentCoins) != 0) return false;
                // If we encounter an empty square, break the loop
                if ((d & (playerCoins | opponentCoins)) == 0) break;
            }
        }

        return true;
    }

}