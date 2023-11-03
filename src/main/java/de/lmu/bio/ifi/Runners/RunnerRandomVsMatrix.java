package de.lmu.bio.ifi.Runners;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;

import java.util.List;
import java.util.Random;

public class RunnerRandomVsMatrix {
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
    private static final int[][] WEIGHT_MATRIX2 = {{10000, -3000, 1000, 800, 800, 1000, -3000, 10000},
            {-3000, -5000, -450, -500, -500, -450, -5000, -3000},
            {1000, -450, 30, 10, 10, 30, -450, 1000},
            {800, -500, 10, 50, 50, 10, -500, 800},
            {800, -500, 10, 50, 50, 10, -500, 800},
            {1000, -450, 30, 10, 10, 30, -450, 1000},
            {-3000, -5000, -450, -500, -500, -450, -5000, -3000},
            {10000, -3000, 1000, 800, 800, 1000, -3000, 10000}
    };
    private static final int[][] WEIGHT_MATRIX3 = {
            {100, -20, 10, 5, 5, 10, -20, 100},
            {-20, -50, -2, -2, -2, -2, -50, -20},
            {10, -2, -1, -1, -1, -1, -2, 10},
            {5, -2, -1, -1, -1, -1, -2, 5},
            {5, -2, -1, -1, -1, -1, -2, 5},
            {10, -2, -1, -1, -1, -1, -2, 10},
            {-20, -50, -2, -2, -2, -2, -50, -20},
            {100, -20, 10, 5, 5, 10, -20, 100}
    };


    public static void main(String[] args) {
        int totalGames = 100_000;
        int playerOneWins = 0;
        int playerTwoWins = 0;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalGames; i++) {
            GameStatus result = doGame();
            if (result == GameStatus.PLAYER_1_WON) {
                playerOneWins++;
            } else if (result == GameStatus.PLAYER_2_WON) {
                playerTwoWins++;
            }

            if (i % 1000 == 0) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                double percentComplete = (double) i / totalGames * 100;
                double timeRemaining = (double) elapsedTime / (i + 1) * (totalGames - i);
                System.out.printf("Progress: %.2f%%, Time Remaining: %.2f seconds%n", percentComplete, timeRemaining / 1000);
            }

        }

        System.out.println("Player One wins: " + playerOneWins);
        System.out.println("Player Two wins: " + playerTwoWins);
    }

    public static GameStatus doGame() {
        OthelloGame othelloGame = new OthelloGame();
//        System.out.println(othelloGame);
//        System.out.println();
        boolean playerOne = true;
        Random rand = new Random();
        while (othelloGame.gameStatus() == GameStatus.RUNNING) {
            List<Move> possibleMoves = othelloGame.getPossibleMoves(playerOne);
            Move move;
            if (possibleMoves != null && !possibleMoves.isEmpty()) {
                if (playerOne) {
                    // Player One uses the weight matrix to choose the best move
                    move = possibleMoves.get(0);
                    int maxWeight = WEIGHT_MATRIX3[move.y][move.x];
                    for (Move possibleMove : possibleMoves) {
                        int weight = WEIGHT_MATRIX3[possibleMove.y][possibleMove.x];
                        if (weight > maxWeight) {
                            move = possibleMove;
                            maxWeight = weight;
                        } else if (weight == maxWeight) {
                            // If weights are the same, choose randomly
                            if (rand.nextBoolean()) {
                                move = possibleMove;
                            }
                        }
                    }
                } else {
                    // Player Two chooses a random move
                    move = possibleMoves.get(rand.nextInt(possibleMoves.size()));
                }
                othelloGame.makeMove(playerOne, move.x, move.y);
            } else {
                othelloGame.makeMove(playerOne, -1, -1);
            }
            playerOne = !playerOne;
            //System.out.println(othelloGame);
            //System.out.println(othelloGame.gameStatus());
//            System.out.println();
        }
//        System.out.println("Game over. " + othelloGame.gameStatus());
        return othelloGame.gameStatus();
    }
}