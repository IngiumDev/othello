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
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
public class GameTests {

    @Test
    public void testShortestGame() {
        OthelloGame othelloGame = new OthelloGame();
        // load in from the first input arg
        String filename = "src/main/java/de/lmu/bio/ifi/data/shortestgame.txt"; // replace with your filename
        ArrayList<Move> moves = OthelloGame.getMovesFromList(OthelloGame.readFileToLines(filename));
        boolean playerOne = true;
        for (Move move : moves) {
            System.out.println(othelloGame);
            System.out.println("Player " + (playerOne ? "1" : "2") + " move: x=" + move.x + "y=" + move.y);
            othelloGame.makeMove(playerOne, move.x, move.y);
            playerOne = !playerOne;
        }
        System.out.println(othelloGame);
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
        String filename = "src/main/java/de/lmu/bio/ifi/data/samplemoves.txt"; // replace with your filename
        ArrayList<Move> moves = OthelloGame.getMovesFromList(OthelloGame.readFileToLines(filename));
        boolean playerOne = true;
        for (Move move : moves) {
            othelloGame.makeMove(playerOne, move.x, move.y);
            playerOne = !playerOne;
        }
        System.out.println(othelloGame);
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
        Path path = Paths.get("src/main/java/de/lmu/bio/ifi/data//othello_dataset.csv");

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
                            if (othelloGame.getValidMoves(playerOne) == 0L) {
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
        Path path = Paths.get("src/main/java/de/lmu/bio/ifi/data//GameDB2015.csv");

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
                            if (othelloGame.getValidMoves(playerOne) == 0L) {
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
                        assertEquals(expectedStatus, othelloGame.gameStatus());
                    });
        }
    }




}
