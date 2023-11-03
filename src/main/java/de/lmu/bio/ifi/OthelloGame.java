package de.lmu.bio.ifi;

import de.lmu.bio.ifi.basicpackage.BasicBoard;
import szte.mi.Move;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OthelloGame extends BasicBoard implements Game {
    private final static int BOARD_SIZE = 8;
    private final static int[][] DIRECTIONS = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
    private final ArrayList<PlayerMove> moveHistory;
    private int playerOneChips;
    private int playerTwoChips;


    /**
     * Create a new Othello board with the default size of 8x8.
     */
    public OthelloGame() {
        super();
        super.board = createDefaultBoard();
        playerOneChips = 2;
        playerTwoChips = 2;
        moveHistory = new ArrayList<>();
    }

    /**
     * @return a default board with 2 black and 2 white stones in the middle. (default othello)
     */
    public static int[][] createDefaultBoard() {
        int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
        board[3][3] = 2;
        board[3][4] = 1;
        board[4][3] = 1;
        board[4][4] = 2;
        return board;
    }

    /**
     * @param filename the name of the file to read
     * @return a list of lines in the file
     */
    public static ArrayList<String> readFileToLines(String filename) {
        File file = new File(filename);
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
        return lines;
    }

    /**
     * @param movesList a list of moves in the format "(x/y)"
     * @return a list of moves in Move format
     */
    public static ArrayList<Move> getMovesFromList(ArrayList<String> movesList) {
        // Moves given in the format "(x/y)"
        ArrayList<Move> moves = new ArrayList<>();
        for (String line : movesList) {
            String[] split = line.split("/");
            int x = Integer.parseInt(split[0].substring(1));
            int y = Integer.parseInt(split[1].substring(0, split[1].length() - 1));
            moves.add(new Move(x, y));
        }
        return moves;
    }

    /**
     * Make a move for the given player at the given position.
     *
     * @param isPlayerOne true if player 1, else player 2.
     * @param x         the x coordinate of the move.
     * @param y         the y coordinate of the move.
     * @return true if the move was valid, else false.
     */
    @Override
    public boolean makeMove(boolean isPlayerOne, int x, int y) {
        int player = isPlayerOne ? 1 : 2;
        List<Move> possibleMoves = getPossibleMoves(isPlayerOne);
        if (possibleMoves == null) {
            moveHistory.add(new PlayerMove(isPlayerOne, -1, -1));
            return false;
        }
        // Check if the move is valid
        boolean validMove = false;
        for (Move move : possibleMoves) {
            if (move.x == x && move.y == y) {
                validMove = true;
                break;
            }
        }
        if (!validMove) {
            return false;
        }
        // Make the move
        getBoard()[y][x] = player;
        // Flip the chips
        doFlipChips(isPlayerOne, x, y);
        addMoveToHistory(new PlayerMove(isPlayerOne, x, y));
        // Update the chip count
        updateChipCount();
        return true;
    }

    /**
     * Update the chip count for both players.
     */
    public void updateChipCount() {
        playerOneChips = 0;
        playerTwoChips = 0;
        for (int[] row : getBoard()) {
            for (int i : row) {
                if (i == 1) {
                    playerOneChips++;
                } else if (i == 2) {
                    playerTwoChips++;
                }
            }
        }
    }

    /**
     * Check and return the status of the game, if there is a winner, a draw or still running.
     *
     * @return the current game status.
     */
    @Override
    public GameStatus gameStatus() {
        if (moveHistory.size() >= 2 &&
                moveHistory.get(moveHistory.size() - 1).x == -1 &&
                moveHistory.get(moveHistory.size() - 2).x == -1) {
            updateChipCount();
            if (playerOneChips > playerTwoChips) {
                return GameStatus.PLAYER_1_WON;
            } else if (playerOneChips < playerTwoChips) {
                return GameStatus.PLAYER_2_WON;
            } else {
                return GameStatus.DRAW;
            }
        }
        return GameStatus.RUNNING;
    }


    /**
     * Get all possible moves for the current player.
     * Return null if it is not the turn of the given player.
     * The list is empty if there are no possible moves.
     *
     * @param isPlayerOne true if player 1, else player 2.
     * @return a list of all possible moves.
     */
    @Override
    public List<Move> getPossibleMoves(boolean isPlayerOne) {
        if ((moveHistory.isEmpty() && !isPlayerOne) || (!moveHistory.isEmpty() && (moveHistory.get(moveHistory.size() - 1).isPlayerOne() == isPlayerOne)))
            return null;
        ArrayList<Move> moves = new ArrayList<>();
        for (int y = 0; y < getBoard().length; y++) {
            for (int x = 0; x < getBoard()[y].length; x++) {
                // Check if the current field is empty
                if (getBoard()[y][x] != 0) {
                    continue;
                }
                // Check if we are adjacent to an enemy chip
                if (!checkAdjacent(isPlayerOne, x, y)) {
                    continue;
                }
                // Check whether trapping the enemy is possible
                if (!CheckIfCanTrap(isPlayerOne, x, y)) {
                    continue;
                }
                moves.add(new Move(x, y));
            }
        }
        return moves;
    }

    /**
     * Get a String representation of the board.
     *
     * @return a String representation of the board.
     */
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int[] row : getBoard()) {
            for (int i : row) {
                switch (i) {
                    case 0:
                        output.append(".");
                        break;
                    case 1:
                        output.append("X");
                        break;
                    case 2:
                        output.append("O");
                        break;
                }
                output.append(" ");
            }
            // Remove last space
            output.deleteCharAt(output.length() - 1);
            output.append("\n");
        }
        // Remove last newline
        output.deleteCharAt(output.length() - 1);
        return output.toString();

    }

    /**
     * Check whether the given coordinates are adjacent to an enemy chip.
     *
     * @param isPlayerOne true if player 1, else player 2.
     * @param x         the x coordinate of the move.
     * @param y         the y coordinate of the move.
     * @return true if the given coordinates are adjacent to an enemy chip.
     */
    public boolean checkAdjacent(boolean isPlayerOne, int x, int y) {
        int opponent = isPlayerOne ? 2 : 1;

        for (int[] direction : OthelloGame.DIRECTIONS) {
            int newX = x + direction[0];
            int newY = y + direction[1];

            if (newX >= 0 && newX < OthelloGame.BOARD_SIZE && newY >= 0 && newY < OthelloGame.BOARD_SIZE) {
                if (getBoard()[newY][newX] == opponent) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get all adjacent enemy chips for a given move.
     *
     * @param isPlayerOne true if player 1, else player 2.
     * @param x         the x coordinate of the move.
     * @param y         the y coordinate of the move.
     * @return a list of all adjacent enemy chips.
     */
    public ArrayList<int[]> getAdjacentEnemies(boolean isPlayerOne, int x, int y) {
        int opponent = isPlayerOne ? 2 : 1;
        ArrayList<int[]> adjacentEnemies = new ArrayList<>();

        for (int[] direction : OthelloGame.DIRECTIONS) {
            int newX = x + direction[0];
            int newY = y + direction[1];

            if (newX >= 0 && newX < OthelloGame.BOARD_SIZE && newY >= 0 && newY < OthelloGame.BOARD_SIZE) {
                if (getBoard()[newY][newX] == opponent) {
                    adjacentEnemies.add(new int[]{newY, newX});
                }
            }
        }

        return adjacentEnemies;
    }

    /**
     * Check whether the current player can trap the enemy chip at the given coordinates.
     *
     * @param playerOne true if player 1, else player 2.
     * @param x         the x coordinate of the move.
     * @param y         the y coordinate of the move.
     * @return true if the current player can trap the enemy chip at the given coordinates.
     */
    public boolean CheckIfCanTrap(boolean playerOne, int x, int y) {
        ArrayList<int[]> adjacentEnemies = getAdjacentEnemies(playerOne, x, y);
        for (int[] adjacentEnemy : adjacentEnemies) {
            int[] direction = {adjacentEnemy[0] - y, adjacentEnemy[1] - x};
            int newX = adjacentEnemy[1];
            int newY = adjacentEnemy[0];
            while (true) {
                newX += direction[1];
                newY += direction[0];
                if (newX < 0 || newX >= OthelloGame.BOARD_SIZE || newY < 0 || newY >= OthelloGame.BOARD_SIZE || getBoard()[newY][newX] == 0) {
                    break;
                }
                if (getBoard()[newY][newX] == (playerOne ? 1 : 2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Flip the chips for a given move, if possible. Used recursively to flip all chips who are trapped. If a chip is flipped, the method is called again for that chip.
     *
     * @param isPlayerOne true if player 1, else player 2.
     * @param x         the x coordinate of the move.
     * @param y         the y coordinate of the move.
     */
    public void doFlipChips(boolean isPlayerOne, int x, int y) {
        ArrayList<int[]> flippedChips = new ArrayList<>();
        ArrayList<int[]> adjacentEnemies = getAdjacentEnemies(isPlayerOne, x, y);
        for (int[] adjacentEnemy : adjacentEnemies) {
            ArrayList<int[]> chipsToFlip = new ArrayList<>();
            int[] direction = {adjacentEnemy[0] - y, adjacentEnemy[1] - x};
            int newX = adjacentEnemy[1];
            int newY = adjacentEnemy[0];
            while (true) {
                if (newX < 0 || newX >= OthelloGame.BOARD_SIZE || newY < 0 || newY >= OthelloGame.BOARD_SIZE || getBoard()[newY][newX] == 0) {
                    break;
                }
                if (getBoard()[newY][newX] == (isPlayerOne ? 1 : 2)) {
                    flipChips(chipsToFlip);
                    flippedChips.addAll(chipsToFlip);
                    break;
                }
                chipsToFlip.add(new int[]{newY, newX});
                newX += direction[1];
                newY += direction[0];
            }
        }
        for (int[] flipped : flippedChips) {
            if (getBoard()[flipped[0]][flipped[1]] != (isPlayerOne ? 1 : 2)) {
                doFlipChips(isPlayerOne, flipped[0], flipped[1]);
            }
        }

    }

    /**
     * Flip the chips in the given list.
     *
     * @param chipsToFlip a list of chips to flip
     */
    public void flipChips(ArrayList<int[]> chipsToFlip) {
        for (int[] flipped : chipsToFlip) {
            getBoard()[flipped[0]][flipped[1]] = (getBoard()[flipped[0]][flipped[1]] == 1 ? 2 : 1);
        }
    }

    /**
     * Get the history of moves.
     *
     * @return the history of moves.
     */
    public ArrayList<PlayerMove> getMoveHistory() {
        return moveHistory;
    }

    /**
     * Add a move to the history.
     *
     * @param move the move to add to the history.
     */
    public void addMoveToHistory(PlayerMove move) {
        moveHistory.add(move);
    }

    /**
     * Get the number of chips for player 1.
     *
     * @return the number of chips player 1 has.
     */
    public int getPlayerOneChips() {
        return playerOneChips;
    }

    /**
     * Get the number of chips for player 2.
     *
     * @return the number of chips player 2 has.
     */
    public int getPlayerTwoChips() {
        return playerTwoChips;
    }
}