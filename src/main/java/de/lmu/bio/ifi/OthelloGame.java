package de.lmu.bio.ifi;

import szte.mi.Move;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class OthelloGame implements Game {
    public final static int BOARD_SIZE = 8;
    public final static int[][] DIRECTIONS = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
    private final ArrayList<PlayerMove> moveHistory;
    private int playerOneChips;
    private int playerTwoChips;
    public final static int EMPTY = 0;
    public final static int PLAYER_ONE = 1;
    public final static int PLAYER_TWO = 2;
    private int[][] board = {{0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, PLAYER_TWO, PLAYER_ONE, 0, 0, 0},
            {0, 0, 0, PLAYER_ONE, PLAYER_TWO, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}};



    /**
     * Create a new Othello board with the default size of 8x8.
     */
    public OthelloGame() {

        playerOneChips = 2;
        playerTwoChips = 2;
        moveHistory = new ArrayList<>();
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
     * @param x           the x coordinate of the move.
     * @param y           the y coordinate of the move.
     * @return true if the move was valid, else false.
     */
    @Override
    public boolean makeMove(boolean isPlayerOne, int x, int y) {
        int player = isPlayerOne ? PLAYER_ONE : PLAYER_TWO;
        if (x == -1 && y == -1) {
            moveHistory.add(new PlayerMove(isPlayerOne, -1, -1));
            return true; // A pass move is always valid
        }
        List<Move> possibleMoves = getPossibleMoves(isPlayerOne);
        if (possibleMoves == null || possibleMoves.isEmpty()) {
            moveHistory.add(new PlayerMove(isPlayerOne, -1, -1));
            return false;
        }
        if (!isValidMove(isPlayerOne, x, y)) {
            return false;
        }
        // Make the move
        board[y][x] = player;
        // Add one to the chip count
        if (isPlayerOne) {
            playerOneChips++;
        } else {
            playerTwoChips++;
        }
        // Flip the chips
        doFlipChips(isPlayerOne, x, y);
        addMoveToHistory(new PlayerMove(isPlayerOne, x, y));
        // Update the chip count
        return true;
    }

    public boolean isValidMove(boolean isPlayerOne, int x, int y) {
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
            return false;
        }
        if (getCell(x, y) != 0) {
            return false;
        }
        ArrayList<int[]> adjacentEnemies = getAdjacentEnemies(isPlayerOne, x, y, board);
        if (adjacentEnemies.isEmpty()) {
            return false;
        }
        return CheckIfCanTrap(isPlayerOne, x, y, board, adjacentEnemies);
    }

    /**
     * Update the chip count for both players.
     */
    public void updateChipCount() {
        playerOneChips = 0;
        playerTwoChips = 0;
        for (int[] row : getBoard()) {
            for (int i : row) {
                if (i == PLAYER_ONE) {
                    playerOneChips++;
                } else if (i == PLAYER_TWO) {
                    playerTwoChips++;
                }
            }
        }
    }

    public int[][] getBoard() {
        return this.board;
    }

    /**
     * Check and return the status of the game, if there is a winner, a draw or still running.
     *
     * @return the current game status.
     */
    @Override
    public GameStatus gameStatus() {
        if (playerOneChips == 0) return GameStatus.PLAYER_2_WON;
        if (playerTwoChips == 0) return GameStatus.PLAYER_1_WON;

        if (isGameOver()) return determineWinner();

        return GameStatus.RUNNING;
    }

    private boolean isGameOver() {
        //System.out.println("Player one has possible moves: " + hasPossibleMoves(true));
        //System.out.println("Player two has possible moves: " + hasPossibleMoves(false));
        return isBoardFull() || isLastTwoMovesPass() || !possibleMoveExists();
    }

    private boolean isBoardFull() {
        return playerOneChips + playerTwoChips == 64;
    }

    private boolean isLastTwoMovesPass() {
        return moveHistory.size() >= 2 &&
                moveHistory.get(moveHistory.size() - 1).x == -1 &&
                moveHistory.get(moveHistory.size() - 2).x == -1;
    }

    private boolean possibleMoveExists() {
        int[][] board = getBoard();
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                int disc = board[y][x];
                if (disc == EMPTY) {
                    ArrayList<int[]> adjacentPlayerOne = new ArrayList<>();
                    ArrayList<int[]> adjacentPlayerTwo = new ArrayList<>();
                    for (int[] direction : OthelloGame.DIRECTIONS) {
                        int newX = x + direction[0];
                        int newY = y + direction[1];

                        if (newX >= 0 && newX < OthelloGame.BOARD_SIZE && newY >= 0 && newY < OthelloGame.BOARD_SIZE) {
                            int cell = board[newY][newX];
                            if (cell != OthelloGame.EMPTY) {
                                if (cell == OthelloGame.PLAYER_ONE) {
                                    adjacentPlayerOne.add(new int[]{newY, newX});
                                } else {
                                    adjacentPlayerTwo.add(new int[]{newY, newX});
                                }
                            }
                        }
                    }
                    if (adjacentPlayerOne.isEmpty() && adjacentPlayerTwo.isEmpty()) {
                        continue;
                    }
                    if (CheckIfCanTrap(false, x, y, board, adjacentPlayerOne) || CheckIfCanTrap(true, x, y, board, adjacentPlayerTwo)) {
                        return true;
                    }

                }
            }
        }
        return false;
    }

    public boolean hasPossibleMoves(boolean isPlayerOne) {
        int[][] board = getBoard();
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                ArrayList<int[]> adjacentEnemies = getAdjacentEnemies(isPlayerOne, x, y, board);
                if (board[y][x] == 0 && !adjacentEnemies.isEmpty() && CheckIfCanTrap(isPlayerOne, x, y, board, adjacentEnemies)) {
                    return true;
                }
            }
        }
        return false;
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
        // Original version

        List<Move> moves = new ArrayList<>();
        int[][] board = getBoard();
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                if (board[y][x] == EMPTY) {
                    ArrayList<int[]> adjacentEnemies = getAdjacentEnemies(isPlayerOne, x, y, board);
                    if ((adjacentEnemies.isEmpty()) || !CheckIfCanTrap(isPlayerOne, x, y, board, adjacentEnemies)) {
                        continue;
                    }
                } else {
                    continue;
                }
                moves.add(new Move(x, y));
            }
        }

        return moves;
        // Attempted to optimize
      /*
        List<Move> moves = new ArrayList<>();
        int[][] board = getBoard();
        int enemyChip = isPlayerOne ? 2 : 1;
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                if (board[y][x] == enemyChip) {
                    List<int[]> adjacentPositions = getAdjacentPositions(x, y, board);
                    for (int[] pos : adjacentPositions) {
                        if (board[pos[0]][pos[1]] == 0 && CheckIfCanTrap(isPlayerOne, pos[1], pos[0], board, new int[]{y,x})) {
                            moves.add(new Move(pos[1], pos[0]));
                        }
                    }
                }
            }
        }
        return moves;*/

    }

    private List<int[]> getAdjacentPositions(int x, int y, int[][] board) {
        List<int[]> adjacentPositions = new ArrayList<>();
        for (int[] direction : DIRECTIONS) {
            int newX = x + direction[1];
            int newY = y + direction[0];
            if (newX >= 0 && newX < OthelloGame.BOARD_SIZE && newY >= 0 && newY < OthelloGame.BOARD_SIZE) {
                adjacentPositions.add(new int[]{newY, newX});
            }
        }
        return adjacentPositions;
    }


    /**
     * Get a String representation of the board.
     *
     * @return a String representation of the board.
     */

    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int[] row : getBoard()) {
            for (int i : row) {
                switch (i) {
                    case EMPTY:
                        output.append(".");
                        break;
                    case PLAYER_ONE:
                        output.append("X");
                        break;
                    case PLAYER_TWO:
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
    /*
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        int[] rowNumbers = getBoard()[0];
        // Print column numbers
        output.append("  ");
        for (int i = 0; i < rowNumbers.length; i++) {
            output.append(i + " ");
        }
        output.append("\n");

        int rowCount = 0;
        for (int[] row : getBoard()) {
            // Print row number
            output.append(rowCount + " ");
            rowCount++;
            for (int i : row) {
                switch (i) {
                    case 0:
                        output.append(". ");
                        break;
                    case 1:
                        output.append("X ");
                        break;
                    case 2:
                        output.append("O ");
                        break;
                }
            }
            // Remove last space and add newline
            output.deleteCharAt(output.length() - 1);
            output.append("\n");
        }
        // Remove last newline
        output.deleteCharAt(output.length() - 1);
        return output.toString();
    }
*/

    /**
     * Check whether the given coordinates are adjacent to an enemy chip.
     *
     * @param isPlayerOne true if player 1, else player 2.
     * @param x           the x coordinate of the move.
     * @param y           the y coordinate of the move.
     * @return true if the given coordinates are adjacent to an enemy chip.
     */
    public boolean checkAdjacent(boolean isPlayerOne, int x, int y, int[][] board) {
        int opponent = isPlayerOne ? PLAYER_TWO : PLAYER_ONE;

        for (int[] direction : OthelloGame.DIRECTIONS) {
            int newX = x + direction[0];
            int newY = y + direction[1];

            if (newX >= 0 && newX < OthelloGame.BOARD_SIZE && newY >= 0 && newY < OthelloGame.BOARD_SIZE) {
                if (board[newY][newX] == opponent) {
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
     * @param x           the x coordinate of the move.
     * @param y           the y coordinate of the move.
     * @return a list of all adjacent enemy chips.
     */
    public ArrayList<int[]> getAdjacentEnemies(boolean isPlayerOne, int x, int y, int[][] board) {
        int opponent = isPlayerOne ? PLAYER_TWO : PLAYER_ONE;
        ArrayList<int[]> adjacentEnemies = new ArrayList<>();

        for (int[] direction : OthelloGame.DIRECTIONS) {
            int newX = x + direction[0];
            int newY = y + direction[1];

            if (newX >= 0 && newX < OthelloGame.BOARD_SIZE && newY >= 0 && newY < OthelloGame.BOARD_SIZE) {
                if (board[newY][newX] == opponent) {
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
    public boolean CheckIfCanTrap(boolean playerOne, int x, int y, int[][] board, ArrayList<int[]> adjacentEnemies) {
        int playerCell = playerOne ? PLAYER_ONE : PLAYER_TWO;
        for (int[] adjacentEnemy : adjacentEnemies) {
            int[] direction = {adjacentEnemy[0] - y, adjacentEnemy[1] - x};
            int newX = adjacentEnemy[1] + direction[1];
            int newY = adjacentEnemy[0] + direction[0];
            boolean isXValid = newX >= 0 && newX < OthelloGame.BOARD_SIZE;
            boolean isYValid = newY >= 0 && newY < OthelloGame.BOARD_SIZE;
            while (isXValid && isYValid) {
                int cell = getCell(newX, newY);
                if (cell == EMPTY) break;
                if (cell == playerCell) {
                    return true;
                }
                newX += direction[1];
                newY += direction[0];
                isXValid = newX >= 0 && newX < OthelloGame.BOARD_SIZE;
                isYValid = newY >= 0 && newY < OthelloGame.BOARD_SIZE;
            }
        }
        return false;
    }

    public boolean CheckIfCanTrap(boolean playerOne, int x, int y, int[][] board, int[] adjacentEnemy) {
        int playerCell = playerOne ? 1 : 2;

        int[] direction = {adjacentEnemy[0] - y, adjacentEnemy[1] - x};
        int newX = adjacentEnemy[1] + direction[1];
        int newY = adjacentEnemy[0] + direction[0];
        boolean isXValid = newX >= 0 && newX < OthelloGame.BOARD_SIZE;
        boolean isYValid = newY >= 0 && newY < OthelloGame.BOARD_SIZE;
        while (isXValid && isYValid) {
            int cell = board[newY][newX];
            if (cell == EMPTY) break;
            if (cell == playerCell) {
                return true;
            }
            newX += direction[1];
            newY += direction[0];
            isXValid = newX >= 0 && newX < OthelloGame.BOARD_SIZE;
            isYValid = newY >= 0 && newY < OthelloGame.BOARD_SIZE;
        }

        return false;
    }

    /**
     * Flip the chips for a given move, if possible. Used recursively to flip all chips who are trapped. If a chip is flipped, the method is called again for that chip.
     *
     * @param isPlayerOne true if player 1, else player 2.
     * @param x           the x coordinate of the move.
     * @param y           the y coordinate of the move.
     */
    public void doFlipChips(boolean isPlayerOne, int x, int y) {
        ArrayList<int[]> adjacentEnemies = getAdjacentEnemies(isPlayerOne, x, y, board);
        int playerChip = isPlayerOne ? PLAYER_ONE : PLAYER_TWO;
        for (int[] adjacentEnemy : adjacentEnemies) {
            int[] direction = {adjacentEnemy[0] - y, adjacentEnemy[1] - x};
            int newX = adjacentEnemy[1];
            int newY = adjacentEnemy[0];
            while (newX >= 0 && newX < OthelloGame.BOARD_SIZE && newY >= 0 && newY < OthelloGame.BOARD_SIZE) {
                int cell = board[newY][newX];
                if (cell == EMPTY) break;
                if (cell == playerChip) {
                    int flipX = adjacentEnemy[1];
                    int flipY = adjacentEnemy[0];
                    while (flipX != newX || flipY != newY) {
                        board[flipY][flipX] = playerChip;
                        playerOneChips += (board[flipY][flipX] == PLAYER_ONE ? 1 : -1);
                        playerTwoChips += (board[flipY][flipX] == PLAYER_TWO ? 1 : -1);
                        flipX += direction[1];
                        flipY += direction[0];
                    }
                    break;
                }
                newX += direction[1];
                newY += direction[0];
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
            board[flipped[0]][flipped[1]] = (board[flipped[0]][flipped[1]] == PLAYER_ONE ? PLAYER_TWO : PLAYER_ONE);
            // Update the chip count
            playerOneChips += (board[flipped[0]][flipped[1]] == PLAYER_ONE ? 1 : -1);
            playerTwoChips += (board[flipped[0]][flipped[1]] == PLAYER_TWO ? 1 : -1);
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

    public String getPlayerTurn() {
        if (moveHistory.isEmpty()) {
            return "Player 1";
        } else {
            return moveHistory.get(moveHistory.size() - 1).isPlayerOne() ? "Player 2" : "Player 1";
        }
    }

    public int getPlayerTurnNumber() {
        if (moveHistory.isEmpty()) {
            return 1;
        } else {
            return moveHistory.get(moveHistory.size() - 1).isPlayerOne() ? PLAYER_TWO : PLAYER_ONE;
        }
    }

    private GameStatus determineWinner() {
        if (playerOneChips > playerTwoChips) {
            return GameStatus.PLAYER_1_WON;
        } else if (playerOneChips < playerTwoChips) {
            return GameStatus.PLAYER_2_WON;
        } else {
            return GameStatus.DRAW;
        }
    }

    public int getCell(int x, int y) {
        return board[y][x];
    }

    public OthelloGame copy() {
        OthelloGame copy = new OthelloGame();
        copy.board = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
            copy.board[i] = board[i].clone();
        }
        copy.playerOneChips = playerOneChips;
        copy.playerTwoChips = playerTwoChips;
        copy.moveHistory.addAll(moveHistory);
        return copy;
    }



    @Override
    public int hashCode() {
        return Objects.hash(Arrays.deepHashCode(board), moveHistory);
    }
}