package de.lmu.bio.ifi.runners;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import de.lmu.bio.ifi.players.AIPlayer;
import de.lmu.bio.ifi.players.MonteCarloPlayer;
import szte.mi.Move;
import szte.mi.Player;

import java.util.Random;

public class TwoPlayerRunner {
    private Player playerone;
    private Player playertwo;

    public static void main(String[] args) {
        int totalGames = 200;
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
            System.out.println("Player One wins: " + (100.0* playerOneWins)/(playerOneWins+playerTwoWins));
            if (i % 10 == 0) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                double percentComplete = (double) i / totalGames * 100;
                double timeRemaining = (double) elapsedTime / (i + 1) * (totalGames - i);
                System.out.printf("Progress: %.2f%%, Time Remaining: %.2f seconds%n", percentComplete, timeRemaining / 1000);
            }

        }

        System.out.println("Player One wins: " + playerOneWins);
        System.out.println("Player Two wins: " + playerTwoWins);
        System.out.println("Percentage: " + (double) playerOneWins / totalGames * 100);
        System.out.println("Final Time: " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
    }

    private static GameStatus doGame() {
        long totalTime = 4000; // Total time for the game in milliseconds
        OthelloGame othelloGame = new OthelloGame();
        boolean isPlayerOneTurn = true;
        Random rnd = new Random();
        Random rnd2 = new Random();
        Player playerone = new AIPlayer();
        playerone.init(0, totalTime, rnd);
        MonteCarloPlayer playertwo = new MonteCarloPlayer();
        playertwo.init(1, totalTime, rnd2);
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

            //System.out.println(othelloGame);
            //System.out.println("Black: " + othelloGame.getPlayerOneChips());
            //System.out.println("White: " + othelloGame.getPlayerTwoChips());
        }
        //playerone.printSavedStates();
        //System.out.println("Black: " + othelloGame.getPlayerOneChips());
        //System.out.println("White: " + othelloGame.getPlayerTwoChips());
        //System.out.println(othelloGame);
        //System.out.println("Game over. " + othelloGame.gameStatus());
        //System.out.println(playerone.getMaxDepth());
        System.out.println("Player one time: " + playerOneTime);
        System.out.println("Player two time: " + playerTwoTime);
        // Print out the current win percentage of the AI
        return othelloGame.gameStatus();
    }
}
