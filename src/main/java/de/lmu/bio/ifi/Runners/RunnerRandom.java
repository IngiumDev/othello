package de.lmu.bio.ifi.Runners;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RunnerRandom {
    public static void main(String[] args) {
        OthelloGame othelloGame = new OthelloGame();
        System.out.println(othelloGame);
        System.out.println();
        boolean playerOne = true;
        while (othelloGame.gameStatus() == GameStatus.RUNNING) {
            // print the board before the move, and what move is made
            System.out.println("Possible moves");
            List<Move> possibleMoves = othelloGame.getPossibleMoves(playerOne);
            Random rand = new Random();
            Move move;
            if (possibleMoves != null && !possibleMoves.isEmpty()) {
                for (Move possibleMove : possibleMoves) {
                    System.out.println(possibleMove.x + "/" + possibleMove.y);
                }
                // Choose a random move
                move = possibleMoves.get(rand.nextInt(possibleMoves.size()));
                System.out.println("Random move: " + move.x + "/" + move.y);
            } else {
                System.out.println("No possible moves for Player" + (playerOne ? "1" : "2") + "!");
                othelloGame.makeMove(playerOne, -1, -1);
                playerOne = !playerOne;
                continue;
            }
            System.out.println("Player " + (playerOne ? "1" : "2") + " makes move: " + move.x + "/" + move.y);
            System.out.println();
            othelloGame.makeMove(playerOne, move.x, move.y);
            System.out.println(othelloGame);
            playerOne = !playerOne;
            System.out.println(othelloGame.gameStatus());
            System.out.println();
        }
        System.out.println("Game over. " + othelloGame.gameStatus());

    }
}
