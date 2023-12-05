package de.lmu.bio.ifi.players;

import de.lmu.bio.ifi.*;
import de.lmu.bio.ifi.players.montecarlo.MonteCarloNode;
import de.lmu.bio.ifi.players.montecarlo.MonteCarloTreeSearch;
import de.lmu.bio.ifi.players.montecarlo.movestrategies.MatrixEvaluater;
import szte.mi.Move;
import szte.mi.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CombinedAlphaBetaMCTS implements Player {


    private static double REDUCTION_FACTOR = 1;
    private final double C = 1.52;
    private OthelloGame mainGame;
    private boolean isPlayerOne;
    private MonteCarloTreeSearch monteCarloTreeSearch;
    public boolean stillInOpeningBook = true;
    private OpeningBook openingBook;
    // map of reduction factors for each remaining move use Map.of to create
    private static final Map<Integer, Double> REDUCTION_FACTORS = Map.of(40, 1.0,
            20, 1.0,
            0, 1.0);
    private static final int TIME_TO_SUBTRACT_EACH_MOVE = 15;
    private static final int FIRST_PHASE_END_MOVE = 40;
    private static final int SECOND_PHASE_END_MOVE = 9;
    private Map<String, Integer> FIRST_PHASE_WEIGHTS;
    private Map<String, Integer> SECOND_PHASE_WEIGHTS;
    private Map<String, Integer> THIRD_PHASE_WEIGHTS;


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
        this.mainGame = new OthelloGame();
        this.isPlayerOne = (order == 0);
        MonteCarloNode root = new MonteCarloNode(mainGame, C);
        this.monteCarloTreeSearch = new MonteCarloTreeSearch(isPlayerOne, root, rnd, C);
        this.monteCarloTreeSearch.expandNode(root);
        this.openingBook = new OpeningBook();
        FIRST_PHASE_WEIGHTS = new HashMap<>();
        FIRST_PHASE_WEIGHTS.put("MATRIX", 1);
        FIRST_PHASE_WEIGHTS.put("MOBILITY", 3);
        FIRST_PHASE_WEIGHTS.put("STABILITY", 0);
        FIRST_PHASE_WEIGHTS.put("DISCS", 0);
        FIRST_PHASE_WEIGHTS.put("FRONTIER", 3);
        FIRST_PHASE_WEIGHTS.put("CORNER", 50);
        FIRST_PHASE_WEIGHTS.put("PARITY", isPlayerOne ? 1 : 4);

        SECOND_PHASE_WEIGHTS = new HashMap<>();
        SECOND_PHASE_WEIGHTS.put("MATRIX", 1);
        SECOND_PHASE_WEIGHTS.put("MOBILITY", 2);
        SECOND_PHASE_WEIGHTS.put("STABILITY", 0);
        SECOND_PHASE_WEIGHTS.put("DISCS", 0);
        SECOND_PHASE_WEIGHTS.put("FRONTIER", 3);
        SECOND_PHASE_WEIGHTS.put("CORNER", 50);
        SECOND_PHASE_WEIGHTS.put("PARITY", isPlayerOne ? 1 : 4);

        THIRD_PHASE_WEIGHTS = new HashMap<>();
        THIRD_PHASE_WEIGHTS.put("MATRIX", 0);
        THIRD_PHASE_WEIGHTS.put("MOBILITY", 0);
        THIRD_PHASE_WEIGHTS.put("STABILITY", 0);
        THIRD_PHASE_WEIGHTS.put("DISCS", 5);
        THIRD_PHASE_WEIGHTS.put("FRONTIER", 0);
        THIRD_PHASE_WEIGHTS.put("CORNER", 5);
        THIRD_PHASE_WEIGHTS.put("PARITY", isPlayerOne ? 1 : 4);
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
        long prevMoveLong = OthelloGame.moveToLong(prevMove);
        if (prevMoveLong != 0L) {
            mainGame.forceMakeMove(!isPlayerOne, prevMoveLong);
            monteCarloTreeSearch.makeMove(prevMoveLong);
        } else {
            if (!mainGame.getMoveHistory().isEmpty()) {
                mainGame.forceMakeMove(!isPlayerOne, prevMoveLong);
                monteCarloTreeSearch.makeMove(0L);
            }
        }

        long possibleMoves = mainGame.getValidMoves(isPlayerOne);
        if (possibleMoves == 0L) {
            mainGame.forceMakeMove(isPlayerOne, possibleMoves);
            monteCarloTreeSearch.makeMove(possibleMoves);
            return null;
        } else if (Long.bitCount(possibleMoves) == 1) {
            mainGame.forceMakeMove(isPlayerOne, possibleMoves);
            monteCarloTreeSearch.makeMove(possibleMoves);
            return OthelloGame.longToMove(possibleMoves);
        }
        if (stillInOpeningBook) {
            List<PlayerMove> moveHistory = mainGame.getMoveHistory();
            PlayerMove playerMove = openingBook.getOpeningMove(moveHistory);
            if (playerMove != null) {
                long move = playerMove.toLong();
                if (move != 0L) {
                    mainGame.forceMakeMove(isPlayerOne, move);
                    monteCarloTreeSearch.makeMove(move);
                    return playerMove;
                }
            }
            stillInOpeningBook = false;
        }
        long bestMove;
        int remainingMoves = (64 - mainGame.getAmountOfChipsPlaced()) / 2;
        if (remainingMoves == 0) {
            remainingMoves = 1;
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        if (remainingMoves > 40) {
            REDUCTION_FACTOR = REDUCTION_FACTORS.get(40);
        } else if (remainingMoves > 20) {
            REDUCTION_FACTOR = REDUCTION_FACTORS.get(20);
        } else {
            REDUCTION_FACTOR = REDUCTION_FACTORS.get(0);
        }
        long timeToCalculateThisMove = (long) (((t - elapsedTime) / remainingMoves) * REDUCTION_FACTOR);
        if (remainingMoves > 9) {
            bestMove = monteCarloTreeSearch.findNextMove(timeToCalculateThisMove);
        } else {
            bestMove = findBestMove(mainGame, possibleMoves, timeToCalculateThisMove);

        }
        monteCarloTreeSearch.makeMove(bestMove);
        mainGame.forceMakeMove(isPlayerOne, bestMove);
        return OthelloGame.longToMove(bestMove);
    }


    public MonteCarloTreeSearch getMonteCarloTreeSearch() {
        return monteCarloTreeSearch;
    }

    private static int calcDiscScore(long myPlayerBoard, long opponentBoard) {
        return Long.bitCount(myPlayerBoard) - Long.bitCount(opponentBoard);
    }

    private long findBestMove(OthelloGame game, long possibleMoves, long time) {
        long bestMove = 0L;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        long moves = possibleMoves;
        int remainingMovesTotal = game.getRemainingMoves();
        int remainingMovesToMake = (remainingMovesTotal / 2) + 1;
        long endTime = System.currentTimeMillis() + time;
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
                    int score = gameStatus == GameStatus.RUNNING ? minValue(tempGame, depth, alpha, beta) : scoreGame(tempGame, gameStatus);
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

    private int minValue(OthelloGame game, int depth, int alpha, int beta) {
        GameStatus gameStatus = game.gameStatus();
        if (depth == 0 || gameStatus != GameStatus.RUNNING) {
            return scoreGame(game, gameStatus);
        }
        long possibleMoves = game.getValidMoves(!isPlayerOne);
        if (Long.bitCount(possibleMoves) <= 1) {
            OthelloGame tempGame = game.copy();
            tempGame.forceMakeMove(!isPlayerOne, possibleMoves);
            beta = Math.min(beta, maxValue(tempGame, depth - 1, alpha, beta));
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
                    beta = Math.min(beta, maxValue(tempGame, depth - 1, alpha, beta));
                    if (beta <= alpha) {
                        return alpha;
                    }
                }
            }
        }
        return beta;
    }

    private int maxValue(OthelloGame game, int depth, int alpha, int beta) {
        GameStatus gameStatus = game.gameStatus();
        if (depth == 0 || gameStatus != GameStatus.RUNNING) {
            return scoreGame(game, gameStatus);
        }
        long possibleMoves = game.getValidMoves(isPlayerOne);
        if (Long.bitCount(possibleMoves) <= 1) {
            OthelloGame tempGame = game.copy();
            tempGame.forceMakeMove(isPlayerOne, possibleMoves);
            alpha = Math.max(alpha, minValue(tempGame, depth - 1, alpha, beta));
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
                    alpha = Math.max(alpha, minValue(tempGame, depth - 1, alpha, beta));
                    if (alpha >= beta) {
                        return beta;
                    }
                }
            }
        }
        return alpha;
    }

    private int scoreGame(OthelloGame game, GameStatus gameStatus) {
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


        int matrixWeight, mobilityWeight, stabilityWeight, discsWeight, frontierWeight, cornerWeight, parityWeight;
        if (remainingMoves > FIRST_PHASE_END_MOVE) {
            matrixWeight = FIRST_PHASE_WEIGHTS.get("MATRIX");
            mobilityWeight = FIRST_PHASE_WEIGHTS.get("MOBILITY");
            stabilityWeight = FIRST_PHASE_WEIGHTS.get("STABILITY");
            discsWeight = FIRST_PHASE_WEIGHTS.get("DISCS");
            frontierWeight = FIRST_PHASE_WEIGHTS.get("FRONTIER");
            parityWeight = FIRST_PHASE_WEIGHTS.get("PARITY");
            cornerWeight = FIRST_PHASE_WEIGHTS.get("CORNER");
        } else if (remainingMoves > SECOND_PHASE_END_MOVE) {
            matrixWeight = SECOND_PHASE_WEIGHTS.get("MATRIX");
            mobilityWeight = SECOND_PHASE_WEIGHTS.get("MOBILITY");
            stabilityWeight = SECOND_PHASE_WEIGHTS.get("STABILITY");
            discsWeight = SECOND_PHASE_WEIGHTS.get("DISCS");
            frontierWeight = SECOND_PHASE_WEIGHTS.get("FRONTIER");
            parityWeight = SECOND_PHASE_WEIGHTS.get("PARITY");
            cornerWeight = SECOND_PHASE_WEIGHTS.get("CORNER");
        } else {
            matrixWeight = THIRD_PHASE_WEIGHTS.get("MATRIX");
            mobilityWeight = THIRD_PHASE_WEIGHTS.get("MOBILITY");
            stabilityWeight = THIRD_PHASE_WEIGHTS.get("STABILITY");
            discsWeight = THIRD_PHASE_WEIGHTS.get("DISCS");
            frontierWeight = THIRD_PHASE_WEIGHTS.get("FRONTIER");
            parityWeight = THIRD_PHASE_WEIGHTS.get("PARITY");
            cornerWeight = THIRD_PHASE_WEIGHTS.get("CORNER");
        }
        if (matrixWeight != 0) {
            // Works
            totalScore += MatrixEvaluater.getWeightedPieceCount(myPlayerBoard, opponentBoard) * matrixWeight;
        }
        if (mobilityWeight != 0) {
            totalScore += calcMobilityScore(game) * mobilityWeight;
        }
        if (stabilityWeight != 0) {
            //TODO
            totalScore += calcStabilityScore(game) * stabilityWeight;
        }
        if (discsWeight != 0) {
            totalScore += calcDiscScore(myPlayerBoard, opponentBoard) * discsWeight;
        }
        if (frontierWeight != 0) {
            totalScore -= calcFrontierDiscs(game) * frontierWeight;
        }
        if (parityWeight != 0) {
            totalScore += calcParityScore(game) * parityWeight;
        }
        if (cornerWeight != 0) {
            totalScore += calcCornerScore(game) * cornerWeight;
        }

        return totalScore;
    }

    private int calcCornerScore(OthelloGame game) {
        long playerOneBoard = game.getPlayerOneBoard();
        long playerTwoBoard = game.getPlayerTwoBoard();
        long corners = BitMasks.ALL_CORNER_POSITIONS;
        long playerOneCorners = playerOneBoard & corners;
        long playerTwoCorners = playerTwoBoard & corners;
        int cornerScore = Long.bitCount(playerOneCorners) - Long.bitCount(playerTwoCorners);
        return isPlayerOne ? cornerScore : -cornerScore;
    }

    private int calcParityScore(OthelloGame game) {
        long playerOneDiscs = game.getPlayerOneBoard();
        long playerTwoDiscs = game.getPlayerTwoBoard();

        int totalMoves = Long.bitCount(playerOneDiscs) + Long.bitCount(playerTwoDiscs);
        boolean lastMoveIsPlayerOne = totalMoves % 2 == 0;
        return (lastMoveIsPlayerOne == isPlayerOne) ? 1 : -1;
    }

    private int calcStabilityScore(OthelloGame game) {
        long playerOneDiscs = game.getPlayerOneBoard();
        long playerTwoDiscs = game.getPlayerTwoBoard();


        return 0;
    }

    private int calcMobilityScore(OthelloGame game) {
        // Works
        int moves = game.getPlayerTurnNumber() == 1 ? Long.bitCount(game.getValidMoves(true)) : Long.bitCount(game.getValidMoves(false));
        return isPlayerOne ? moves : -moves;
    }

    private int calcFrontierDiscs(OthelloGame game) {
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

    private long shiftNorth(long bitboard) {
        return (bitboard & BitMasks.UP_MASK) >>> 8;
    }

    private long shiftSouth(long bitboard) {
        return (bitboard & BitMasks.DOWN_MASK) << 8;
    }

    private long shiftEast(long bitboard) {
        return (bitboard & BitMasks.RIGHT_MASK) >>> 1;
    }

    private long shiftWest(long bitboard) {
        return (bitboard & BitMasks.LEFT_MASK) << 1;
    }

    private long shiftNorthEast(long bitboard) {
        return (bitboard & BitMasks.RIGHT_MASK & BitMasks.UP_MASK) >>> 9;
    }

    private long shiftNorthWest(long bitboard) {
        return (bitboard & BitMasks.LEFT_MASK & BitMasks.UP_MASK) >>> 7;
    }

    private long shiftSouthEast(long bitboard) {
        return (bitboard & BitMasks.RIGHT_MASK & BitMasks.DOWN_MASK) << 7;
    }

    private long shiftSouthWest(long bitboard) {
        return (bitboard & BitMasks.LEFT_MASK & BitMasks.DOWN_MASK) << 9;
    }

}
