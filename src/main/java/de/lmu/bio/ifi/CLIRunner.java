package de.lmu.bio.ifi;

import szte.mi.Move;

import java.util.List;
import java.util.Scanner;

public class CLIRunner {
    public static void main(String[] args) {
        OthelloGame othelloGame = new OthelloGame();
        System.out.println(othelloGame);
        System.out.println("Starting at 1: 1 2 3 4 5 6 7 8");
        // Create a scanner, ask the player for input (first x, then y) until a valid move is given. Make sure to check if the game is running after every move with gamestatus. Before every move tell the player all their possible moves
        // Then make the move and print the board
        // Alternate between player 1 and player 2
        boolean playerOne = true;
        Scanner scanner = new Scanner(System.in);
        while (othelloGame.gameStatus() == GameStatus.RUNNING) {
            // Say how many chips each player has
            System.out.println("Player 1 has " + othelloGame.getPlayerOneChips() + " chips");
            System.out.println("Player 2 has " + othelloGame.getPlayerTwoChips() + " chips");
            System.out.println("Possible moves");
            List<Move> possibleMoves = othelloGame.getPossibleMoves(playerOne);
            if (possibleMoves.isEmpty()) {
                System.out.println("No possible moves for player " + (playerOne ? "1" : "2") + "!");
                playerOne = !playerOne;
                continue;
            }
            for (Move possibleMove : possibleMoves) {
                System.out.println(possibleMove.x+1 + "/" + (possibleMove.y+1));}
            System.out.println("Player " + (playerOne ? "1" : "2") + " make your move:");
            int x = scanner.nextInt() - 1;
            int y = scanner.nextInt() - 1;
            if (!othelloGame.makeMove(playerOne, x, y)) {
                System.out.println("Invalid move!");
            } else {
                playerOne = !playerOne;
                System.out.println(othelloGame);
            }
        }
    }
}
