package de.lmu.bio.ifi.players;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;
import szte.mi.Player;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AIPlayer implements Player {
    public final static int[][] DIRECTIONS = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
    private static final int[][] STABILITY_MATRIX = {
            {4, -4, 2, 2, 2, 2, -4, 4},
            {-3, -4, -1, -1, -1, -1, -4, -3},
            {2, -1, 1, 0, 0, 1, -1, 2},
            {2, -1, 0, 1, 1, 0, -1, 2},
            {2, -1, 0, 1, 1, 0, -1, 2},
            {2, -1, 1, 0, 0, 1, -1, 2},
            {-3, -4, -1, -1, -1, -1, -4, -3},
            {4, -4, 2, 2, 2, 2, -3, 4}
    };
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
    private final int DEPTH = 3;
    private int MOBILITY_WEIGHT = 3;
    private int FRONTIER_WEIGHT = 5;
    private int STABLE_WEIGHT = 3;
    private int MATRIX_WEIGHT = 1;
    private final boolean use_saved_states = false;
    public OthelloGame mainGame;
    private final Map<Integer, Integer> knownGameStates = new HashMap<>();
    private boolean isPlayerOne;
    private int usedStates = 0;
    String gameStatesPath = "src/main/java/de/lmu/bio/ifi/data//knownGameStates.csv";

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
        isPlayerOne = order == 0;
        if (use_saved_states) {
            File file = new File(gameStatesPath);
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] split = line.split(",");
                        knownGameStates.put(Integer.parseInt(split[0]), (order == 0) ? Integer.parseInt(split[1]) : -Integer.parseInt(split[1]));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
        if (prevMove != null) {
            mainGame.makeMove(!isPlayerOne, prevMove.x, prevMove.y);
        }
        if (prevMove == null) {
            if (!mainGame.getMoveHistory().isEmpty()) {
                mainGame.makeMove(!isPlayerOne, -1, -1);
            }
        }
        // If the opponent moved record the move

        List<Move> moves = mainGame.getPossibleMoves(isPlayerOne);
        if (moves == null || moves.isEmpty()) {
            mainGame.makeMove(isPlayerOne, -1, -1);
            return null;
        }
        // AI Logic
        Move bestMove = moves.get(0);
        int bestScore = Integer.MIN_VALUE;
        for (Move move : moves) {
            OthelloGame newGame = mainGame.copy();
            newGame.makeMove(isPlayerOne, move.x, move.y);
            int score = miniMaxBoard(newGame, DEPTH, !isPlayerOne, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        mainGame.makeMove(isPlayerOne, bestMove.x, bestMove.y);

        long elapsedTime = System.currentTimeMillis() - startTime;
        // If this is the first move of the game, save the game state
        /*if (prevMove == null) {
            saveGameStates(gameStatesPath);
            System.out.println("saved");
        }*/
        //System.out.println("Time: " + elapsedTime + "ms");
        //printSavedStates();
        return bestMove;
    }

    private int miniMaxBoard(OthelloGame othelloGame, int depth, boolean isCheckPlayerOne, int minValue, int maxValue) {
        Integer gameState = othelloGame.hashCode();
        if (knownGameStates.containsKey(gameState)) {
            usedStates++;
            return knownGameStates.get(gameState);
        }
        // breakout condition
        if (depth == 0) {
            return scoreBoard(othelloGame, isCheckPlayerOne);
        } else if (othelloGame.gameStatus() == GameStatus.PLAYER_1_WON) {
            return isCheckPlayerOne ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        } else if (othelloGame.gameStatus() == GameStatus.PLAYER_2_WON) {
            return isCheckPlayerOne ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        } else if (othelloGame.gameStatus() == GameStatus.DRAW) {
            return 0;
        }
        // Get moves
        List<Move> moves = othelloGame.getPossibleMoves(isCheckPlayerOne);

        int bestScore = isCheckPlayerOne ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;
        if (moves == null || moves.isEmpty()) {
            OthelloGame newGame = othelloGame.copy();
            newGame.makeMove(isCheckPlayerOne, -1, -1);
            score = miniMaxBoard(newGame, depth - 1, !isCheckPlayerOne, minValue, maxValue);
            if (isCheckPlayerOne) {
                bestScore = Math.max(bestScore, score);
                minValue = Math.max(minValue, score);
            } else {
                bestScore = Math.min(bestScore, score);
                maxValue = Math.min(maxValue, score);
            }
            if (maxValue <= minValue) {
                return bestScore;
            }
        }
        for (Move move : moves) {
            OthelloGame newGame = othelloGame.copy();
            newGame.makeMove(isCheckPlayerOne, move.x, move.y);
            score = miniMaxBoard(newGame, depth - 1, !isCheckPlayerOne, minValue, maxValue);

            if (isCheckPlayerOne) {
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
        knownGameStates.put(gameState, bestScore);
        return bestScore;
    }

    private int scoreBoard(OthelloGame game, boolean isCheckPlayerOne) {
        int myPlayerDisc = isCheckPlayerOne ? OthelloGame.PLAYER_ONE : OthelloGame.PLAYER_TWO;
        int opponentDisc = isCheckPlayerOne ? OthelloGame.PLAYER_TWO : OthelloGame.PLAYER_ONE;
        int score = 0;
        int frontierDiscs = 0;
        int mobility = 0;
        int stableCount = 0;
        int matrixScore = 0;

        // Score with weight matrix
        for (int y = 0; y < OthelloGame.BOARD_SIZE; y++) {
            for (int x = 0; x < OthelloGame.BOARD_SIZE; x++) {
                int disc = game.getCell(x, y);
                if (disc == myPlayerDisc) {
                    matrixScore += WEIGHT_MATRIX[y][x];
                    if (isFrontierDisc(game, x, y)) {
                        frontierDiscs++;
                    }
                    // Calculate stability
                    if (isStableDisc(game, x, y, myPlayerDisc)) {
                        stableCount++;
                    }
                } else if (disc == opponentDisc) {
                    matrixScore -= WEIGHT_MATRIX[y][x];
                    if (isFrontierDisc(game, x, y)) {
                        frontierDiscs--;
                    }
                    // Calculate stability
                    if (isStableDisc(game, x, y, opponentDisc)) {
                        stableCount--;
                    }
                }
            }
        }

        // Score with mobility
        List<Move> moves = game.getPossibleMoves(isCheckPlayerOne);
        if (moves != null) {
            if (isCheckPlayerOne == isPlayerOne) {
                mobility += moves.size();
            } else {
                if (moves.isEmpty()) {
                    mobility += 5;
                }
                mobility -= moves.size();
            }
        }

        int totalMoves = game.getMoveHistory().size();
        if (totalMoves < 20) {
            MOBILITY_WEIGHT = 7;
            FRONTIER_WEIGHT = 3;
            STABLE_WEIGHT = 1;
            MATRIX_WEIGHT = 1;
        } else if (totalMoves < 40) {
            MOBILITY_WEIGHT = 4;
            FRONTIER_WEIGHT = 4;
            STABLE_WEIGHT = 5;
            MATRIX_WEIGHT = 2;
        } else {
            MOBILITY_WEIGHT = 1;
            FRONTIER_WEIGHT = 4;
            STABLE_WEIGHT = 10;
            MATRIX_WEIGHT = 1;
        }

        score += STABLE_WEIGHT * stableCount;
        score += MOBILITY_WEIGHT * mobility;
        score -= FRONTIER_WEIGHT * frontierDiscs;
        score += MATRIX_WEIGHT * matrixScore;

        return score;
    }

    private boolean isStableDisc(OthelloGame game, int x, int y, int playerDisc) {
        // Check all eight directions from the disc
        for (int[] direction : OthelloGame.DIRECTIONS) {
            int nx = x + direction[0];
            int ny = y + direction[1];
            if (nx >= 0 && nx < OthelloGame.BOARD_SIZE && ny >= 0 && ny < OthelloGame.BOARD_SIZE && game.getCell(nx, ny) == -playerDisc) {
                return false;
            }
            while (nx >= 0 && nx < OthelloGame.BOARD_SIZE && ny >= 0 && ny < OthelloGame.BOARD_SIZE && game.getCell(nx, ny) == playerDisc) {
                nx += direction[0];
                ny += direction[1];
            }
            if (nx >= 0 && nx < OthelloGame.BOARD_SIZE && ny >= 0 && ny < OthelloGame.BOARD_SIZE && game.getCell(nx, ny) == 0) {
                return false;
            }
        }
        return true;
    }



    public void printSavedStates() {
        System.out.println("Used States: " + usedStates);
        System.out.println("Saved States: " + knownGameStates.size());


    }

    private boolean isFrontierDisc(OthelloGame game, int x, int y) {
        // Check the 8 surrounding cells
        for (int[] direction : DIRECTIONS) {
            int dx = x + direction[0];
            int dy = y + direction[1];
            if (dx >= 0 && dx < OthelloGame.BOARD_SIZE && dy >= 0 && dy < OthelloGame.BOARD_SIZE) {
                if (game.getCell(dx, dy) == OthelloGame.EMPTY) {  // If the cell is empty
                    return true;
                }
            }
        }
        return false;
    }



    public Map<Integer, Integer> getKnownGameStates() {
        return knownGameStates;
    }

    public void saveGameStates(String fileName) {
        File file = new File(fileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<Integer, Integer> entry : knownGameStates.entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OthelloGame getMainGame() {
        return mainGame;
    }

    public void changeWeights(int MOBILITY_WEIGHT, int FRONTIER_WEIGHT, int STABLE_WEIGHT, int MATRIX_WEIGHT) {
        this.MOBILITY_WEIGHT = MOBILITY_WEIGHT;
        this.FRONTIER_WEIGHT = FRONTIER_WEIGHT;
        this.STABLE_WEIGHT = STABLE_WEIGHT;
        this.MATRIX_WEIGHT = MATRIX_WEIGHT;
    }
}
