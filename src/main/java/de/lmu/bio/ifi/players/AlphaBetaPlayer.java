package de.lmu.bio.ifi.players;

import de.lmu.bio.ifi.*;
import de.lmu.bio.ifi.players.montecarlo.movestrategies.MatrixEvaluater;
import szte.mi.Move;
import szte.mi.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class AlphaBetaPlayer implements Player {
    private static final int DEPTH = 6;
    private static final int TIME_TO_SUBTRACT_EACH_MOVE = 15;
    private static final int FIRST_PHASE_END_MOVE = 40;
    private static final int SECOND_PHASE_END_MOVE = 9;
    private HashMap<String, Integer> FIRST_PHASE_WEIGHTS;
    private HashMap<String, Integer> SECOND_PHASE_WEIGHTS;
    private HashMap<String, Integer> THIRD_PHASE_WEIGHTS;
    private OthelloGame mainGame;
    private boolean isPlayerOne;
    private OpeningBook openingBook;
    private boolean stillInOpeningBook = true;

    private static int calcDiscScore(long myPlayerBoard, long opponentBoard) {
        return Long.bitCount(myPlayerBoard) - Long.bitCount(opponentBoard);
    }

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
        this.mainGame = new OthelloGame();
        this.isPlayerOne = (order == 0);
        this.openingBook = new OpeningBook();
        FIRST_PHASE_WEIGHTS = new HashMap<>();
        FIRST_PHASE_WEIGHTS.put("MATRIX", 1);
        FIRST_PHASE_WEIGHTS.put("MOBILITY", 1);
        FIRST_PHASE_WEIGHTS.put("STABILITY", 0);
        FIRST_PHASE_WEIGHTS.put("DISCS", 0);
        FIRST_PHASE_WEIGHTS.put("FRONTIER", 3);
        FIRST_PHASE_WEIGHTS.put("CORNER", 50);
        FIRST_PHASE_WEIGHTS.put("PARITY", isPlayerOne ? 1 : 4);
        FIRST_PHASE_WEIGHTS.put("CORNER_CLOSENESS", 20);

        SECOND_PHASE_WEIGHTS = new HashMap<>();
        SECOND_PHASE_WEIGHTS.put("MATRIX", 1);
        SECOND_PHASE_WEIGHTS.put("MOBILITY", 1);
        SECOND_PHASE_WEIGHTS.put("STABILITY", 0);
        SECOND_PHASE_WEIGHTS.put("DISCS", 0);
        SECOND_PHASE_WEIGHTS.put("FRONTIER", 2);
        SECOND_PHASE_WEIGHTS.put("CORNER", 50);
        SECOND_PHASE_WEIGHTS.put("PARITY", isPlayerOne ? 1 : 4);
        SECOND_PHASE_WEIGHTS.put("CORNER_CLOSENESS", 20);

        THIRD_PHASE_WEIGHTS = new HashMap<>();
        THIRD_PHASE_WEIGHTS.put("MATRIX", 0);
        THIRD_PHASE_WEIGHTS.put("MOBILITY", 0);
        THIRD_PHASE_WEIGHTS.put("STABILITY", 0);
        THIRD_PHASE_WEIGHTS.put("DISCS", 5);
        THIRD_PHASE_WEIGHTS.put("FRONTIER", 0);
        THIRD_PHASE_WEIGHTS.put("CORNER", 5);
        THIRD_PHASE_WEIGHTS.put("PARITY", isPlayerOne ? 1 : 4);
        THIRD_PHASE_WEIGHTS.put("CORNER_CLOSENESS", 3);


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
        long startTime = System.currentTimeMillis();
        // Handle previous move recording
        long prevMoveLong = OthelloGame.moveToLong(prevMove);
        if (prevMoveLong != 0L) {
            mainGame.forceMakeMove(!isPlayerOne, prevMoveLong);
        } else {
            if (!mainGame.getMoveHistory().isEmpty()) {
                mainGame.forceMakeMove(!isPlayerOne, prevMoveLong);
            }
        }

        long possibleMoves = mainGame.getValidMoves(isPlayerOne);
        // If there is only one possible move, make it
        if (Long.bitCount(possibleMoves) <= 1) {
            mainGame.forceMakeMove(isPlayerOne, possibleMoves);
            return possibleMoves == 0L ? null : OthelloGame.longToMove(possibleMoves);
        }

        // Opening Book logic
        if (stillInOpeningBook) {
            List<PlayerMove> moveHistory = mainGame.getMoveHistory();
            PlayerMove playerMove = openingBook.getOpeningMove(moveHistory);
            if (playerMove != null) {
                long move = playerMove.toLong();
                if (move != 0L) {
                    mainGame.forceMakeMove(isPlayerOne, move);
                    System.out.println("Opening book move: " + playerMove);
                    return playerMove;
                }
            }
            stillInOpeningBook = false;
        }


        long bestMove = findBestMoveIterative(mainGame, possibleMoves, t - (System.currentTimeMillis() - startTime), isPlayerOne, FIRST_PHASE_WEIGHTS, SECOND_PHASE_WEIGHTS, THIRD_PHASE_WEIGHTS);
        mainGame.forceMakeMove(isPlayerOne, bestMove);
//        System.out.println(mainGame);
        return OthelloGame.longToMove(bestMove);
    }

    private static long findBestMoveIterative(OthelloGame game, long possibleMoves, long time, boolean isPlayerOne, HashMap<String, Integer> FIRST_PHASE_WEIGHTS, HashMap<String, Integer> SECOND_PHASE_WEIGHTS, HashMap<String, Integer> THIRD_PHASE_WEIGHTS) {
        long bestMove = 0L;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        long moves = possibleMoves;
        int remainingMovesTotal = game.getRemainingMoves();
        int remainingMovesToMake = (remainingMovesTotal / 2) + 1;
        long timeForMove = (time / remainingMovesToMake) - TIME_TO_SUBTRACT_EACH_MOVE;
        long endTime = System.currentTimeMillis() + timeForMove;
        int depth = 0;
        while (endTime > System.currentTimeMillis() && depth <= remainingMovesTotal) {
            moves = possibleMoves;
            while (moves != 0) {
                long testMove = Long.lowestOneBit(moves);
                moves ^= testMove;
                if (testMove != 0L) {
                    OthelloGame tempGame = game.copy();
                    tempGame.forceMakeMove(isPlayerOne, testMove);
                    GameStatus gameStatus = tempGame.gameStatus();
                    int score = gameStatus == GameStatus.RUNNING ? minValue(tempGame, depth, alpha, beta, isPlayerOne, FIRST_PHASE_WEIGHTS, SECOND_PHASE_WEIGHTS, THIRD_PHASE_WEIGHTS) : scoreGame(tempGame, gameStatus, isPlayerOne, FIRST_PHASE_WEIGHTS, SECOND_PHASE_WEIGHTS, THIRD_PHASE_WEIGHTS);
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = testMove;
                        alpha = Math.max(alpha, bestScore); // Update alpha here
                    }
                }
            }
            depth++;
        }
        System.out.println("Depth: " + depth);
        System.out.println("Best score: " + bestScore);

        if (bestMove == 0L) {
            // If no move was found, make a random move
            System.out.println("No move found, making random move");
            int numberOfPossibleMoves = Long.bitCount(possibleMoves);
            int randomMoveIndex = new Random().nextInt(numberOfPossibleMoves);
            for (int i = 0; i <= randomMoveIndex; i++) {
                bestMove = Long.lowestOneBit(possibleMoves);
                possibleMoves ^= bestMove; // unset the current lowest set bit
            }
        }

        return bestMove;
    }

    public static long findBestMove(OthelloGame game, long possibleMoves, long time, boolean isPlayerOne, HashMap<String, Integer> FIRST_PHASE_WEIGHTS, HashMap<String, Integer> SECOND_PHASE_WEIGHTS, HashMap<String, Integer> THIRD_PHASE_WEIGHTS, int depth) {
        long bestMove = 0L;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        long moves = possibleMoves;
        int remainingMovesToMake = (game.getRemainingMoves() / 2) + 1;

        while (moves != 0) {
            long testMove = Long.lowestOneBit(moves);
            moves ^= testMove;
            if (testMove != 0L) {
                OthelloGame tempGame = game.copy();
                tempGame.forceMakeMove(isPlayerOne, testMove);
                GameStatus gameStatus = tempGame.gameStatus();
                int score = gameStatus == GameStatus.RUNNING ? minValue(tempGame, depth, alpha, beta, isPlayerOne, FIRST_PHASE_WEIGHTS, SECOND_PHASE_WEIGHTS, THIRD_PHASE_WEIGHTS) : scoreGame(tempGame, gameStatus, isPlayerOne, FIRST_PHASE_WEIGHTS, SECOND_PHASE_WEIGHTS, THIRD_PHASE_WEIGHTS);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = testMove;
                    alpha = Math.max(alpha, bestScore); // Update alpha here
                }
            }
        }


        if (bestMove == 0L) {
            // If no move was found, make a random move
            System.out.println("No move found, making random move");
            int numberOfPossibleMoves = Long.bitCount(possibleMoves);
            int randomMoveIndex = new Random().nextInt(numberOfPossibleMoves);
            for (int i = 0; i <= randomMoveIndex; i++) {
                bestMove = Long.lowestOneBit(possibleMoves);
                possibleMoves ^= bestMove; // unset the current lowest set bit
            }
        }

        return bestMove;

    }

    private static int minValue(OthelloGame game, int depth, int alpha, int beta, boolean isPlayerOne, HashMap<String, Integer> FIRST_PHASE_WEIGHTS, HashMap<String, Integer> SECOND_PHASE_WEIGHTS, HashMap<String, Integer> THIRD_PHASE_WEIGHTS) {
        GameStatus gameStatus = game.gameStatus();
        if (depth == 0 || gameStatus != GameStatus.RUNNING) {
            return scoreGame(game, gameStatus, isPlayerOne, FIRST_PHASE_WEIGHTS, SECOND_PHASE_WEIGHTS, THIRD_PHASE_WEIGHTS);
        }
        long possibleMoves = game.getValidMoves(!isPlayerOne);
        if (Long.bitCount(possibleMoves) <= 1) {
            OthelloGame tempGame = game.copy();
            tempGame.forceMakeMove(!isPlayerOne, possibleMoves);
            beta = Math.min(beta, maxValue(tempGame, depth - 1, alpha, beta, isPlayerOne, FIRST_PHASE_WEIGHTS, SECOND_PHASE_WEIGHTS, THIRD_PHASE_WEIGHTS));
            if (beta <= alpha) {
                return alpha;
            }
        } else {
            long moves = possibleMoves;
            while (moves != 0) {
                long testMove = Long.lowestOneBit(moves);
                moves ^= testMove;
                if (testMove != 0L) {
                    OthelloGame tempGame = game.copy();
                    tempGame.forceMakeMove(!isPlayerOne, testMove);
                    beta = Math.min(beta, maxValue(tempGame, depth - 1, alpha, beta, isPlayerOne, FIRST_PHASE_WEIGHTS, SECOND_PHASE_WEIGHTS, THIRD_PHASE_WEIGHTS));
                    if (beta <= alpha) {
                        return alpha;
                    }
                }
            }
        }
        return beta;
    }

    private static int maxValue(OthelloGame game, int depth, int alpha, int beta, boolean isPlayerOne, HashMap<String, Integer> FIRST_PHASE_WEIGHTS, HashMap<String, Integer> SECOND_PHASE_WEIGHTS, HashMap<String, Integer> THIRD_PHASE_WEIGHTS) {
        GameStatus gameStatus = game.gameStatus();
        if (depth == 0 || gameStatus != GameStatus.RUNNING) {
            return scoreGame(game, gameStatus, isPlayerOne, FIRST_PHASE_WEIGHTS, SECOND_PHASE_WEIGHTS, THIRD_PHASE_WEIGHTS);
        }
        long possibleMoves = game.getValidMoves(isPlayerOne);
        if (Long.bitCount(possibleMoves) <= 1) {
            OthelloGame tempGame = game.copy();
            tempGame.forceMakeMove(isPlayerOne, possibleMoves);
            alpha = Math.max(alpha, minValue(tempGame, depth - 1, alpha, beta, isPlayerOne, FIRST_PHASE_WEIGHTS, SECOND_PHASE_WEIGHTS, THIRD_PHASE_WEIGHTS));
            if (alpha >= beta) {
                return beta;
            }
        } else {
            long moves = possibleMoves;
            while (moves != 0) {
                long testMove = Long.lowestOneBit(moves);
                moves ^= testMove;
                if (testMove != 0L) {
                    OthelloGame tempGame = game.copy();
                    tempGame.forceMakeMove(isPlayerOne, testMove);
                    alpha = Math.max(alpha, minValue(tempGame, depth - 1, alpha, beta, isPlayerOne, FIRST_PHASE_WEIGHTS, SECOND_PHASE_WEIGHTS, THIRD_PHASE_WEIGHTS));
                    if (alpha >= beta) {
                        return beta;
                    }
                }
            }
        }
        return alpha;
    }

    private static int scoreGame(OthelloGame game, GameStatus gameStatus, boolean isPlayerOne, HashMap<String, Integer> FIRST_PHASE_WEIGHTS, HashMap<String, Integer> SECOND_PHASE_WEIGHTS, HashMap<String, Integer> THIRD_PHASE_WEIGHTS) {
        if (gameStatus == GameStatus.PLAYER_1_WON) {
            return isPlayerOne ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        } else if (gameStatus == GameStatus.PLAYER_2_WON) {
            return isPlayerOne ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        } else if (gameStatus == GameStatus.DRAW) {
            return 0;
        }
        int myPlayerDisc = isPlayerOne ? OthelloGame.PLAYER_ONE : OthelloGame.PLAYER_TWO;
        int opponentDisc = isPlayerOne ? OthelloGame.PLAYER_TWO : OthelloGame.PLAYER_ONE;
        long myPlayerBoard = isPlayerOne ? game.getPlayerOneBoard() : game.getPlayerTwoBoard();
        long opponentBoard = isPlayerOne ? game.getPlayerTwoBoard() : game.getPlayerOneBoard();
        int remainingMoves = game.getRemainingMoves() / 2 + 1;
        int totalScore = 0;


        int matrixWeight, mobilityWeight, stabilityWeight, discsWeight, frontierWeight, cornerWeight, parityWeight, cornerClosenessWeight;
        if (remainingMoves > FIRST_PHASE_END_MOVE) {
            matrixWeight = FIRST_PHASE_WEIGHTS.get("MATRIX");
            mobilityWeight = FIRST_PHASE_WEIGHTS.get("MOBILITY");
            stabilityWeight = FIRST_PHASE_WEIGHTS.get("STABILITY");
            discsWeight = FIRST_PHASE_WEIGHTS.get("DISCS");
            frontierWeight = FIRST_PHASE_WEIGHTS.get("FRONTIER");
            parityWeight = FIRST_PHASE_WEIGHTS.get("PARITY");
            cornerWeight = FIRST_PHASE_WEIGHTS.get("CORNER");
            cornerClosenessWeight = FIRST_PHASE_WEIGHTS.get("CORNER_CLOSENESS");
        } else if (remainingMoves > SECOND_PHASE_END_MOVE) {
            matrixWeight = SECOND_PHASE_WEIGHTS.get("MATRIX");
            mobilityWeight = SECOND_PHASE_WEIGHTS.get("MOBILITY");
            stabilityWeight = SECOND_PHASE_WEIGHTS.get("STABILITY");
            discsWeight = SECOND_PHASE_WEIGHTS.get("DISCS");
            frontierWeight = SECOND_PHASE_WEIGHTS.get("FRONTIER");
            parityWeight = SECOND_PHASE_WEIGHTS.get("PARITY");
            cornerWeight = SECOND_PHASE_WEIGHTS.get("CORNER");
            cornerClosenessWeight = SECOND_PHASE_WEIGHTS.get("CORNER_CLOSENESS");
        } else {
            matrixWeight = THIRD_PHASE_WEIGHTS.get("MATRIX");
            mobilityWeight = THIRD_PHASE_WEIGHTS.get("MOBILITY");
            stabilityWeight = THIRD_PHASE_WEIGHTS.get("STABILITY");
            discsWeight = THIRD_PHASE_WEIGHTS.get("DISCS");
            frontierWeight = THIRD_PHASE_WEIGHTS.get("FRONTIER");
            parityWeight = THIRD_PHASE_WEIGHTS.get("PARITY");
            cornerWeight = THIRD_PHASE_WEIGHTS.get("CORNER");
            cornerClosenessWeight = THIRD_PHASE_WEIGHTS.get("CORNER_CLOSENESS");
        }
        if (matrixWeight != 0) {
            int matrixScore = MatrixEvaluater.getWeightedPieceCount(myPlayerBoard, opponentBoard) * matrixWeight;
            totalScore += matrixScore;
//            System.out.println("Matrix Score: " + matrixScore);
        }

        if (mobilityWeight != 0) {
            int mobilityScore = (int) (calcMobilityScore(game, isPlayerOne) * mobilityWeight * 0.4);
            totalScore += mobilityScore;
//            System.out.println("Mobility Score: " + mobilityScore);
        }

        if (stabilityWeight != 0) {
            //TODO
            int stabilityScore = calcStabilityScore(game, isPlayerOne) * stabilityWeight;
            totalScore += stabilityScore;
//            System.out.println("Stability Score: " + stabilityScore);
        }

        if (discsWeight != 0) {
            int discScore = calcDiscScore(myPlayerBoard, opponentBoard) * discsWeight;
            totalScore += discScore;
//            System.out.println("Disc Score: " + discScore);
        }

        if (frontierWeight != 0) {
            int frontierScore = calcFrontierDiscs(game, isPlayerOne) * frontierWeight;
            totalScore -= frontierScore;
//            System.out.println("Frontier Score: " + frontierScore);
        }

        if (parityWeight != 0) {
            int parityScore = calcParityScore(game, isPlayerOne) * parityWeight;
            totalScore += parityScore;
//            System.out.println("Parity Score: " + parityScore);
        }

        if (cornerWeight != 0) {
            int cornerScore = calcCornerScore(game, isPlayerOne) * cornerWeight;
            totalScore += cornerScore;
//            System.out.println("Corner Score: " + cornerScore);
        }

        if (cornerClosenessWeight != 0) {
            int closenessScore = calcCornerClosenessScore(game, isPlayerOne) * cornerClosenessWeight;
            totalScore += closenessScore;
//            System.out.println("Corner Closeness Score: " + closenessScore);
        }


        return totalScore;
    }

    private static int calcCornerScore(OthelloGame game, boolean isPlayerOne) {
        long playerOneBoard = game.getPlayerOneBoard();
        long playerTwoBoard = game.getPlayerTwoBoard();
        long corners = BitMasks.ALL_CORNER_POSITIONS;
        long playerOneCorners = playerOneBoard & corners;
        long playerTwoCorners = playerTwoBoard & corners;
        int cornerScore = Long.bitCount(playerOneCorners) - Long.bitCount(playerTwoCorners);

        return isPlayerOne ? cornerScore : -cornerScore;
    }

    private static int calcParityScore(OthelloGame game, boolean isPlayerOne) {
        long playerOneDiscs = game.getPlayerOneBoard();
        long playerTwoDiscs = game.getPlayerTwoBoard();

        int totalMoves = Long.bitCount(playerOneDiscs) + Long.bitCount(playerTwoDiscs);
        boolean lastMoveIsPlayerOne = totalMoves % 2 == 0;
        return (lastMoveIsPlayerOne == isPlayerOne) ? 1 : -1;
    }

    private static int calcStabilityScore(OthelloGame game, boolean isPlayerOne) {
        long playerOneDiscs = game.getPlayerOneBoard();
        long playerTwoDiscs = game.getPlayerTwoBoard();


        return 0;
    }

    private static int calcMobilityScore(OthelloGame game, boolean isPlayerOne) {
        // Works
        int playerTurnNumber = game.getPlayerTurnNumber();
        boolean isPlayerOneTurn = playerTurnNumber == 1;
        long possibleMoves = game.getValidMoves(isPlayerOneTurn);
        int mobilityScore = Long.bitCount(possibleMoves);
        long moves = possibleMoves;
        while (moves != 0) {
            long testMove = Long.lowestOneBit(moves);
            moves ^= testMove;
            if (testMove != 0L) {
                OthelloGame tempGame = game.copy();
                tempGame.forceMakeMove(isPlayerOneTurn, testMove);
                mobilityScore -= Long.bitCount(tempGame.getValidMoves(isPlayerOneTurn));
            }
        }
        return isPlayerOne ? mobilityScore : -mobilityScore;
    }

    private static int calcFrontierDiscs(OthelloGame game, boolean isPlayerOne) {
        long playerOneDiscs = game.getPlayerOneBoard();
        long playerTwoDiscs = game.getPlayerTwoBoard();
        long empty = ~(playerOneDiscs | playerTwoDiscs);

        long emptyNeighbors = shiftNorth(empty) | shiftSouth(empty) | shiftEast(empty) | shiftWest(empty) |
                shiftNorthEast(empty) | shiftNorthWest(empty) | shiftSouthEast(empty) | shiftSouthWest(empty);

        long frontierPlayerOne = playerOneDiscs & emptyNeighbors;
        long frontierPlayerTwo = playerTwoDiscs & emptyNeighbors;

        int frontierScore = Long.bitCount(frontierPlayerOne) - Long.bitCount(frontierPlayerTwo);
        return isPlayerOne ? frontierScore : -frontierScore;
    }

    private static int calcCornerClosenessScore(OthelloGame game, boolean isPlayerOne) {
        long playerOneBoard = game.getPlayerOneBoard();
        long playerTwoBoard = game.getPlayerTwoBoard();
        long[] corners = BitMasks.CORNERS; // An array of bitmasks for each corner
        long[] cornerCloseness = BitMasks.CORNERS_CLOSE_POSITIONS; // An array of bitmasks for positions close to each corner

        int cornerClosenessScore = 0;
        for (int i = 0; i < corners.length; i++) {
            long playerOneCorners = playerOneBoard & corners[i];
            long playerTwoCorners = playerTwoBoard & corners[i];
            long playerOneCornerCloseness = playerOneBoard & cornerCloseness[i];
            long playerTwoCornerCloseness = playerTwoBoard & cornerCloseness[i];

            if (playerOneCorners != 0) {
                cornerClosenessScore += Long.bitCount(playerOneCornerCloseness);
                cornerClosenessScore += Long.bitCount(playerTwoCornerCloseness);
            }
            if (playerTwoCorners != 0) {
                cornerClosenessScore -= Long.bitCount(playerTwoCornerCloseness);
                cornerClosenessScore -= Long.bitCount(playerOneCornerCloseness);
            }
        }

        return isPlayerOne ? cornerClosenessScore : -cornerClosenessScore;
    }


    private static long shiftNorth(long bitboard) {
        return (bitboard & BitMasks.UP_MASK) >>> 8;
    }

    private static long shiftSouth(long bitboard) {
        return (bitboard & BitMasks.DOWN_MASK) << 8;
    }

    private static long shiftEast(long bitboard) {
        return (bitboard & BitMasks.RIGHT_MASK) >>> 1;
    }

    private static long shiftWest(long bitboard) {
        return (bitboard & BitMasks.LEFT_MASK) << 1;
    }

    private static long shiftNorthEast(long bitboard) {
        return (bitboard & BitMasks.RIGHT_MASK & BitMasks.UP_MASK) >>> 9;
    }

    private static long shiftNorthWest(long bitboard) {
        return (bitboard & BitMasks.LEFT_MASK & BitMasks.UP_MASK) >>> 7;
    }

    private static long shiftSouthEast(long bitboard) {
        return (bitboard & BitMasks.RIGHT_MASK & BitMasks.DOWN_MASK) << 7;
    }

    private static long shiftSouthWest(long bitboard) {
        return (bitboard & BitMasks.LEFT_MASK & BitMasks.DOWN_MASK) << 9;
    }

}
