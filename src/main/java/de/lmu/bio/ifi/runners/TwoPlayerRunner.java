package de.lmu.bio.ifi.runners;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import de.lmu.bio.ifi.players.AIPlayer;
import szte.mi.Move;
import szte.mi.Player;

public class TwoPlayerRunner {
    private Player playerone;
    private Player playertwo;

    public static void main(String[] args) {
        int totalGames = 1;
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
        OthelloGame othelloGame = new OthelloGame();
        boolean isPlayerOneTurn = true;
        AIPlayer playerone = new AIPlayer();
        playerone.init(0, 0, null);
        Player playertwo = new AIPlayer();
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
