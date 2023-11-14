package de.lmu.bio.ifi.runners;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import de.lmu.bio.ifi.players.AIPlayer;
import de.lmu.bio.ifi.players.RandomPlayer;
import szte.mi.Move;
import szte.mi.Player;

import java.util.concurrent.*;

public class MultiThreadedHyperParaOptimization {
    private static volatile int maxWins = 0;
    private static volatile int bestMOBILITY_WEIGHT = 0;
    private static volatile int bestFRONTIER_WEIGHT = 0;
    private static volatile int bestSTABLE_WEIGHT = 0;
    private static volatile int bestMATRIX_WEIGHT = 0;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int totalGames = 25;
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        for (int a = 0; a <= 4; a++) {
            for (int b = 0; b <= 4; b++) {
                for (int c = 0; c <= 4; c++) {
                    for (int d = 0; d <= 4; d++) {
                        final int MOBILITY_WEIGHT = a;
                        final int FRONTIER_WEIGHT = b;
                        final int STABLE_WEIGHT = c;
                        final int MATRIX_WEIGHT = d;
                        completionService.submit(() -> {
                            int playerOneWins = 0;
                            for (int i = 0; i < totalGames; i++) {
                                GameStatus result = doGame(MOBILITY_WEIGHT, FRONTIER_WEIGHT, STABLE_WEIGHT, MATRIX_WEIGHT);
                                if (result == GameStatus.PLAYER_1_WON) {
                                    playerOneWins++;
                                }
                            }
                            return playerOneWins;
                        });
                    }
                }
            }
        }

        for (int a = 0; a <= 4; a++) {
            for (int b = 0; b <= 4; b++) {
                for (int c = 0; c <= 4; c++) {
                    for (int d = 0; d <= 4; d++) {
                        Future<Integer> future = completionService.take();
                        int playerOneWins = future.get();
                        synchronized (TwoPlayerRunner.class) {
                            if (playerOneWins > maxWins) {
                                maxWins = playerOneWins;
                                bestMOBILITY_WEIGHT = a;
                                bestFRONTIER_WEIGHT = b;
                                bestSTABLE_WEIGHT = c;
                                bestMATRIX_WEIGHT = d;
                            }
                        }
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        double percentComplete = (double) (a * 125 + b * 25 + c * 5 + d) / 625 * 100;
                        double timeRemaining = (double) elapsedTime / (a * 125 + b * 25 + c * 5 + d + 1) * (625 - (a * 125 + b * 25 + c * 5 + d));
                        System.out.printf("Progress: %.2f%%, Time Remaining: %.2f seconds%n", percentComplete, timeRemaining / 1000);
                    }
                }
            }
        }

        executor.shutdown();

        System.out.println("Best weights: MOBILITY_WEIGHT = " + bestMOBILITY_WEIGHT + ", FRONTIER_WEIGHT = " + bestFRONTIER_WEIGHT + ", STABLE_WEIGHT = " + bestSTABLE_WEIGHT + ", MATRIX_WEIGHT = " + bestMATRIX_WEIGHT);
        System.out.println("Max wins: " + maxWins);
        System.out.println("Percentage: " + (double) maxWins / totalGames * 100);
        System.out.println("Final Time: " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
    }

    private static GameStatus doGame(int MOBILITY_WEIGHT, int FRONTIER_WEIGHT, int STABLE_WEIGHT, int MATRIX_WEIGHT) {
        OthelloGame othelloGame = new OthelloGame();
        boolean isPlayerOneTurn = true;
        AIPlayer playerone = new AIPlayer();
        playerone.init(0, 0, null);
        playerone.changeWeights(MOBILITY_WEIGHT, FRONTIER_WEIGHT, STABLE_WEIGHT, MATRIX_WEIGHT);
        Player playertwo = new RandomPlayer();
        playertwo.init(1, 0, null);
        // Make first move
        Move firstMove = playerone.nextMove(null, 0, 0);
        othelloGame.makeMove(isPlayerOneTurn, firstMove.x, firstMove.y);
        Move prevMove = firstMove;
        while (othelloGame.gameStatus() == GameStatus.RUNNING) {
            isPlayerOneTurn = !isPlayerOneTurn;
            Move move;
            if (isPlayerOneTurn) {
                move = playerone.nextMove(prevMove, 0, 0);
            } else {
                move = playertwo.nextMove(prevMove, 0, 0);
            }
            if (move == null) {
                othelloGame.makeMove(isPlayerOneTurn, -1, -1);
                //System.out.println("Player " + (isPlayerOneTurn ? "one" : "two") + " passed");
            } else {
                if (!othelloGame.isValidMove(isPlayerOneTurn, move.x, move.y)) {
                    System.out.println("Invalid move: " + move.x + "/" + move.y);
                    return isPlayerOneTurn ? GameStatus.PLAYER_2_WON : GameStatus.PLAYER_1_WON;
                }
                othelloGame.makeMove(isPlayerOneTurn, move.x, move.y);
            }
            prevMove = move;
        }
        //playerone.printSavedStates();
        //System.out.println("Black: " + othelloGame.getPlayerOneChips());
        //System.out.println("White: " + othelloGame.getPlayerTwoChips());
        //System.out.println(othelloGame);
        //System.out.println("Game over. " + othelloGame.gameStatus());
        return othelloGame.gameStatus();
    }
}
