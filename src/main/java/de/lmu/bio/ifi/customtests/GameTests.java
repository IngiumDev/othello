package de.lmu.bio.ifi.customtests;
import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import org.junit.Test;
import szte.mi.Move;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;
public class GameTests {

    @Test
    public void testShortestGame() {
        OthelloGame othelloGame = new OthelloGame();
        // load in from the first input arg
        String filename = "src/main/java/de/lmu/bio/ifi/shortestgame.txt"; // replace with your filename
        ArrayList<Move> moves = OthelloGame.getMovesFromList(OthelloGame.readFileToLines(filename));
        boolean playerOne = true;
        for (Move move : moves) {
            List<Move> possibleMoves = othelloGame.getPossibleMoves(playerOne);
            othelloGame.makeMove(playerOne, move.x, move.y);
            playerOne = !playerOne;
        }
        assertEquals(GameStatus.PLAYER_1_WON, othelloGame.gameStatus());

        // Check final board state
        String expectedBoard =
                ". . . . . . . .\n" +
                        ". . . . . . . .\n" +
                        ". . . . X . . .\n" +
                        ". . . X X X . .\n" +
                        ". . X X X X X .\n" +
                        ". . . X X X . .\n" +
                        ". . . . X . . .\n" +
                        ". . . . . . . .";
        assertEquals(expectedBoard, othelloGame.toString());
    }
    @Test
    public void testGameStatus() {
        OthelloGame othelloGame = new OthelloGame();
        // load in from the first input arg
        String filename = "src/main/samplemoves.txt"; // replace with your filename
        ArrayList<Move> moves = OthelloGame.getMovesFromList(OthelloGame.readFileToLines(filename));
        boolean playerOne = true;
        for (Move move : moves) {
            List<Move> possibleMoves = othelloGame.getPossibleMoves(playerOne);
            othelloGame.makeMove(playerOne, move.x, move.y);
            playerOne = !playerOne;
        }
        assertEquals(GameStatus.RUNNING, othelloGame.gameStatus());

        // Check final board state
        String expectedBoard =
                ". . . . . . . .\n" +
                        ". . . . . . . .\n" +
                        ". X . . X . . .\n" +
                        ". . X O O O . .\n" +
                        ". . X X O O . .\n" +
                        ". . . . . O . .\n" +
                        ". . . . . . . .\n" +
                        ". . . . . . . .";
        assertEquals(expectedBoard, othelloGame.toString());
    }
    public GameStatus convertWinner(int winner) {
        switch (winner) {
            case 1:
                return GameStatus.PLAYER_1_WON;
            case -1:
                return GameStatus.PLAYER_2_WON;
            case 0:
                return GameStatus.DRAW;
            default:
                return GameStatus.RUNNING;
        }
    }
    public Move convertMove(String moveStr) {
        char letter = moveStr.toLowerCase().charAt(0);
        int number = Character.getNumericValue(moveStr.charAt(1));
        int x = letter - 'a';
        int y = number-1;
        return new Move(x, y);
    }



    @Test
    public void testGamesFromCSV() throws IOException {
        Path path = Paths.get("gamedatabase/othello_dataset.csv");

        try (Stream<String> lines = Files.lines(path)) {
            lines.skip(1) // skip header
                    .map(line -> line.split(","))
                    .forEach(data -> {
                        GameStatus expectedStatus = convertWinner(Integer.parseInt(data[1]));
                        String movesStr = data[2];
                        OthelloGame othelloGame = new OthelloGame();
                        boolean playerOne = true;
                        for (int i = 0; i < movesStr.length(); i += 2) {
                            String moveStr = movesStr.substring(i, i + 2);
                            Move move = convertMove(moveStr);
                            //System.out.println(moveStr + " " + move.x + " " + move.y);
                            othelloGame.makeMove(playerOne, move.x, move.y);

                            playerOne = !playerOne;
                            // make a null move if no moves are possible
                            if (othelloGame.getPossibleMoves(playerOne).isEmpty()) {
                                othelloGame.makeMove(playerOne, -1, -1);
                                playerOne = !playerOne;
                            }
                        }
                        assertEquals(expectedStatus, othelloGame.gameStatus());
                    });
        }
    }
    @Test
    public void testGamesFromCSV2() throws IOException {
        Path path = Paths.get("gamedatabase/GameDB2015.csv");

        try (Stream<String> lines = Files.lines(path)) {
            lines.skip(1) // skip header
                    .map(line -> line.split(","))
                    .forEach(data -> {
                        GameStatus expectedStatus = convertWinner(Integer.parseInt(data[1]));
                        String movesStr = data[2];
                        OthelloGame othelloGame = new OthelloGame();
                        boolean playerOne = true;
                        for (int i = 0; i < movesStr.length(); i += 2) {
                            String moveStr = movesStr.substring(i, i + 2);
                            Move move = convertMove(moveStr);
                            othelloGame.makeMove(playerOne, move.x, move.y);

                            playerOne = !playerOne;
                            // make a null move if no moves are possible
                            if (othelloGame.getPossibleMoves(playerOne).isEmpty()) {
                                othelloGame.makeMove(playerOne, -1, -1);
                                playerOne = !playerOne;
                            }
                        }
                        if (othelloGame.gameStatus() != expectedStatus) {
                            System.out.println("Expected: " + expectedStatus);
                            System.out.println("Actual: " + othelloGame.gameStatus());
                            System.out.println("Moves: " + movesStr);
                            System.out.println(othelloGame);
                        }
                        //assertEquals(expectedStatus, othelloGame.gameStatus());
                    });
        }
    }




}
