package de.lmu.bio.ifi.runners;

import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class Runner {
    // (5/4)
    //    (5/3)
    //    (4/2)
    //    (5/5)
    //    (2/4)
    //    (2/3)
    //    (1/2)
    //hard code the moves
    private ArrayList<Move> moves;

    public static void main(String[] args) {
        OthelloGame othelloGame = new OthelloGame();
        System.out.println(othelloGame);
        System.out.println();
        ArrayList<Move> moves = OthelloGame.getMovesFromList(OthelloGame.readFileToLines("src/main/samplemoves.txt"));
        boolean playerOne = true;
        for (Move move : moves) {
            // print the board before the move, and what move is made
            System.out.println("Possible moves");
            List<Move> possibleMoves = othelloGame.parseValidMovesToMoveList(othelloGame.getValidMoves(playerOne));
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
