package de.lmu.bio.ifi.runners;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import de.lmu.bio.ifi.players.AIPlayer;
import de.lmu.bio.ifi.players.RandomPlayer;
import szte.mi.Move;
import szte.mi.Player;

import java.util.List;

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
//        System.out.println(othelloGame);
//        System.out.println();
        boolean playerOne = true;
        AIPlayer playerone = new AIPlayer();
        playerone.init(0, 0, null);
        Player playertwo = new RandomPlayer();
        playertwo.init(1, 0, null);
        // Make first move
        Move firstMove = playerone.nextMove(null, 0, 0);
        othelloGame.makeMove(playerOne, firstMove.x, firstMove.y);
        playerOne = !playerOne;
        while (othelloGame.gameStatus() == GameStatus.RUNNING) {
            List<Move> possibleMoves = othelloGame.getPossibleMoves(playerOne);
            Move move;
            if (possibleMoves != null && !possibleMoves.isEmpty()) {
                if (playerOne) {
                    move = playerone.nextMove(othelloGame.getMoveHistory().get(othelloGame.getMoveHistory().size() - 1), 0, 0);
                } else {
                    // Player Two chooses a random move
                    move = playertwo.nextMove(othelloGame.getMoveHistory().get(othelloGame.getMoveHistory().size() - 1), 0, 0);
                }
                if (move != null) {
                    othelloGame.makeMove(playerOne, move.x, move.y);
                } else {
                    othelloGame.makeMove(playerOne, -1, -1);
                }
            } else {
                othelloGame.makeMove(playerOne, -1, -1);
            }
            playerOne = !playerOne;
            //System.out.println(othelloGame);
            // System.out.println(othelloGame.gameStatus());
            //System.out.println();
        }
        playerone.printSavedStates();
        System.out.println("Game over. " + othelloGame.gameStatus());
        /*Map<Integer, Integer> knownGameStates = playerone.getKnownGameStates();

        File file = new File("knownGameStates.csv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<Integer, Integer> entry : knownGameStates.entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return othelloGame.gameStatus();
    }
}
