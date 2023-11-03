package de.lmu.bio.ifi.Runners;

import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;

import java.util.ArrayList;
import java.util.List;

public class RunnerIn {
    public static void main(String[] args) {
        OthelloGame othelloGame = new OthelloGame();
        System.out.println(othelloGame);
        System.out.println();
        // load in from the first input arg
        String filename = args[0];
        ArrayList<Move> moves = OthelloGame.getMovesFromList(OthelloGame.readFileToLines(filename));
        boolean playerOne = true;
        for (Move move : moves) {
            // print the board before the move, and what move is made
            System.out.println("Possible moves");
            List<Move> possibleMoves = othelloGame.getPossibleMoves(playerOne);
            for (Move possibleMove : possibleMoves) {
                System.out.println(possibleMove.x + "/" + possibleMove.y);
            }
            System.out.println("Player " + (playerOne ? "1" : "2") + " makes move: " + move.x + "/" + move.y);
            System.out.println();
            othelloGame.makeMove(playerOne, move.x, move.y);
            System.out.println(othelloGame);
            playerOne = !playerOne;
            System.out.println(othelloGame.gameStatus());
            System.out.println();
        }


    }
}
