package de.lmu.bio.ifi.runners;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import de.lmu.bio.ifi.players.AIPlayer;
import de.lmu.bio.ifi.players.MonteCarloPlayer;
import szte.mi.Move;
import szte.mi.Player;

import java.util.Random;
import java.util.concurrent.*;

public class MultiThreadedHyperParaOptimization {
    private static volatile int maxWins = 0;
    private static volatile double bestC = 0;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int totalGames = 100;
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(4);
        CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        for (double c = 1.30; c <= 1.60; c += 0.01) {
            final double C = c;
            completionService.submit(() -> {
                int playerTwoWins = 0;
                for (int i = 0; i < totalGames; i++) {
                    GameStatus result = doGame(C);
                    if (result == GameStatus.PLAYER_2_WON) {
                        playerTwoWins++;
                    }
                }
                return playerTwoWins;
            });
        }

        for (double c = 1.30; c <= 1.60; c += 0.01) {
            Future<Integer> future = completionService.take();
            int playerTwoWins = future.get();
            synchronized (TwoPlayerRunner.class) {
                if (playerTwoWins > maxWins) {
                    maxWins = playerTwoWins;
                    bestC = c;
                }
            }
            System.out.println(c + " " + playerTwoWins);
            long elapsedTime = System.currentTimeMillis() - startTime;
            double percentComplete = (c - 1.30) / 0.30 * 100;
            double timeRemaining = (double) elapsedTime / (c - 1.30) * (1.60 - c);
            System.out.printf("Progress: %.2f%%, Time Remaining: %.2f seconds%n", percentComplete, timeRemaining / 1000);
        }

        executor.shutdown();

        System.out.println("Best C: " + bestC);
        System.out.println("Max wins: " + maxWins);
        System.out.println("Percentage: " + (double) maxWins / totalGames * 100);
        System.out.println("Final Time: " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
    }

    private static GameStatus doGame(double C) {
        long totalTime = 4000; // Total time for the game in milliseconds
        OthelloGame othelloGame = new OthelloGame();
        boolean isPlayerOneTurn = true;
        Random rnd = new Random();
        Random rnd2 = new Random();
        Player playerone = new AIPlayer();
        playerone.init(0, totalTime, rnd);
        MonteCarloPlayer playertwo = new MonteCarloPlayer();
        playertwo.init(1, totalTime, rnd2, C);
        long playerOneTime = totalTime;
        long playerTwoTime = totalTime;
        // Make first move
        Move firstMove = playerone.nextMove(null, 0, playerOneTime);
        othelloGame.makeMove(isPlayerOneTurn, firstMove.x, firstMove.y);
        Move prevMove = firstMove;
        while (othelloGame.gameStatus() == GameStatus.RUNNING) {
            // Check if player has time left
            if (isPlayerOneTurn && playerOneTime <= 0) {
                System.out.println("Player one ran out of time");
                return GameStatus.PLAYER_2_WON;
            } else if (!isPlayerOneTurn && playerTwoTime <= 0) {
                System.out.println("Player two ran out of time");
                return GameStatus.PLAYER_1_WON;
            }
            isPlayerOneTurn = !isPlayerOneTurn;
            Move move;

            if (isPlayerOneTurn) {
                long startTime = System.currentTimeMillis();
                move = playerone.nextMove(prevMove, 0, playerOneTime);
                long endTime = System.currentTimeMillis();
                playerOneTime -= (endTime - startTime);
            } else {
                long startTime = System.currentTimeMillis();
                move = playertwo.nextMove(prevMove, 0, playerTwoTime);
                long endTime = System.currentTimeMillis();
                playerTwoTime -= (endTime - startTime);
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
        System.out.println("done");
        return othelloGame.gameStatus();
    }
}