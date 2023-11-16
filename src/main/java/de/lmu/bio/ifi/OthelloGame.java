package de.lmu.bio.ifi;

import szte.mi.Move;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OthelloGame {
    public final static int BOARD_SIZE = 8;
    public final static int[][] DIRECTIONS = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
    public final static int EMPTY = 0;
    public final static int PLAYER_ONE = 1;
    public final static int PLAYER_TWO = 2;
    public final static int[] BIT_DIRECTIONS = {-9, -8, -7, -1, 1, 7, 8, 9};
    // Longthello
    public final long UP_MASK = -256L;
    public final long DOWN_MASK = 72057594037927935L;
    /*private int[][] board = {
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, PLAYER_TWO, PLAYER_ONE, 0, 0, 0},
            {0, 0, 0, PLAYER_ONE, PLAYER_TWO, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}};*/
    public final long RIGHT_MASK = 9187201950435737471L;
    public final long LEFT_MASK = -72340172838076674L;
    private final ArrayList<PlayerMove> moveHistory;
    private int playerOneChips;
    private int playerTwoChips;
    private long playerOneBoard;
    private long playerTwoBoard;


    /**
     * Create a new Othello board with the default size of 8x8.
     */
    public OthelloGame() {
        // Initialize the board, and set the starting chips
        this.playerOneChips = 2;
        this.playerTwoChips = 2;
        this.moveHistory = new ArrayList<>();
        this.playerOneBoard = 0L;
        this.playerTwoBoard = 0L;
        setCell(PLAYER_ONE, 4, 3);
        setCell(PLAYER_ONE, 3, 4);
        setCell(PLAYER_TWO, 3, 3);
        setCell(PLAYER_TWO, 4, 4);
    }

    public void setCell(int player, int x, int y) {
        // get the index of the cell to set
        int indexToSet = y * BOARD_SIZE + x;
        // We need to set it at player one, and unset it at player two (if it was set) and vica vers
        if (player == PLAYER_ONE) {

            playerOneBoard |= 1L << indexToSet;
            playerTwoBoard &= ~(1L << indexToSet);
        } else if (player == PLAYER_TWO) {
            playerTwoBoard |= 1L << indexToSet;
            playerOneBoard &= ~(1L << indexToSet);
        }
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

    public List<String> parseValidMoves(long validMoves) {
        List<String> moves = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            if ((validMoves & (1L << i)) != 0) {
                int x = i / 8;
                int y = i % 8;
                moves.add("(" + x + "/" + y + ")");
            }
        }
        return moves;
    }

    public List<Move> parseValidMovestoMove(long validmoves) {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            if ((validmoves & (1L << i)) != 0) {
                int y = i / 8;
                int x = i % 8;
                moves.add(new Move(x, y));
            }
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
    public boolean makeMove(boolean isPlayerOne, int x, int y) {
        int player = isPlayerOne ? PLAYER_ONE : PLAYER_TWO;
        if (getPlayerTurnNumber() != player) {
            // Ain't your turn bro
            return false;
        }
        if (x == -1 && y == -1) {
            // Pass move
            moveHistory.add(new PlayerMove(isPlayerOne, -1, -1));
            return true; // A pass move is always valid
        }
        // Conver the x,y into a long of the moveToMake (only bit that is set to 1 is the move)
        long movetoMake = 1L << (x + y * 8);
        // Get a long were all the possible moves are set to 1
        long possibleMoves = getValidMoves(isPlayerOne);
        // AND the two longs, if the result is 0, then the move is not valid
        if ((movetoMake & possibleMoves) == 0L) {
            // that Ain't valid
            return false;
        }
        // Actually make the move
        if (isPlayerOne) {
            playerOneBoard |= movetoMake;
        } else {
            playerTwoBoard |= movetoMake;
        }
        // Flip the chips
        doFlip(isPlayerOne, movetoMake);
        moveHistory.add(new PlayerMove(isPlayerOne, x, y));
        return true;
    }

    public int getPlayerTurnNumber() {
        if (moveHistory.isEmpty()) {
            return 1;
        } else {
            return moveHistory.get(moveHistory.size() - 1).isPlayerOne() ? PLAYER_TWO : PLAYER_ONE;
        }
    }

    public long getValidMoves(boolean isPlayerOne) {
        // Start with 0 valid moves, get the current player's board, and the opponent's board
        long validMoves = 0L;
        long playerBoard = isPlayerOne ? playerOneBoard : playerTwoBoard;
        long opponentBoard = isPlayerOne ? playerTwoBoard : playerOneBoard;
        long emptyCells = getEmptyBoard();
        // For each direction, get the valid moves in that direction, and OR them with the current valid moves (we want to add them)
        validMoves |= getValidMovesInDirection(playerBoard, opponentBoard, emptyCells, (BOARD_SIZE), DOWN_MASK);
        validMoves |= getValidMovesInDirection(playerBoard, opponentBoard, emptyCells, -BOARD_SIZE, UP_MASK);
        validMoves |= getValidMovesInDirection(playerBoard, opponentBoard, emptyCells, 1, RIGHT_MASK);
        validMoves |= getValidMovesInDirection(playerBoard, opponentBoard, emptyCells, -1, LEFT_MASK);
        validMoves |= getValidMovesInDirection(playerBoard, opponentBoard, emptyCells, BOARD_SIZE + 1, RIGHT_MASK & DOWN_MASK);
        validMoves |= getValidMovesInDirection(playerBoard, opponentBoard, emptyCells, BOARD_SIZE - 1, LEFT_MASK & DOWN_MASK);
        validMoves |= getValidMovesInDirection(playerBoard, opponentBoard, emptyCells, -(BOARD_SIZE - 1), RIGHT_MASK & UP_MASK);
        validMoves |= getValidMovesInDirection(playerBoard, opponentBoard, emptyCells, -(BOARD_SIZE + 1), LEFT_MASK & UP_MASK);
        return validMoves;
    }

    /**
     * Update the chip count for both players.
     */

    /*public int[][] getBoard() {
        return this.board;
    }*/
    public void doFlip(boolean isPlayerOne, long move) {
        long chipsToFlip = 0L;
        long playerBoard = isPlayerOne ? playerOneBoard : playerTwoBoard;
        long opponentBoard = isPlayerOne ? playerTwoBoard : playerOneBoard;

        // Calculate the chips to flip in each direction
        chipsToFlip |= getChipsToFlipInDirection(playerBoard, opponentBoard, move, BOARD_SIZE, DOWN_MASK);
        chipsToFlip |= getChipsToFlipInDirection(playerBoard, opponentBoard, move, -BOARD_SIZE, UP_MASK);
        chipsToFlip |= getChipsToFlipInDirection(playerBoard, opponentBoard, move, 1, RIGHT_MASK);
        chipsToFlip |= getChipsToFlipInDirection(playerBoard, opponentBoard, move, -1, LEFT_MASK);
        chipsToFlip |= getChipsToFlipInDirection(playerBoard, opponentBoard, move, BOARD_SIZE + 1, RIGHT_MASK & DOWN_MASK);
        chipsToFlip |= getChipsToFlipInDirection(playerBoard, opponentBoard, move, BOARD_SIZE - 1, LEFT_MASK & DOWN_MASK);
        chipsToFlip |= getChipsToFlipInDirection(playerBoard, opponentBoard, move, -(BOARD_SIZE - 1), RIGHT_MASK & UP_MASK);
        chipsToFlip |= getChipsToFlipInDirection(playerBoard, opponentBoard, move, -(BOARD_SIZE + 1), LEFT_MASK & UP_MASK);

        // Flip the chips
        playerBoard ^= chipsToFlip; // XOR to flip the player's chips
        opponentBoard ^= chipsToFlip; // XOR to flip the opponent's chips

        // Update the boards
        if (isPlayerOne) {
            playerOneBoard = playerBoard;
            playerTwoBoard = opponentBoard;
        } else {
            playerOneBoard = opponentBoard;
            playerTwoBoard = playerBoard;
        }
    }

    public long getEmptyBoard() {
        return ~(playerOneBoard | playerTwoBoard);
    }

    public long getValidMovesInDirection(long playerBoard, long opponentBoard, long emptyCells, int shift, long mask) {
        long validMoves = 0L;
        // Ternary operator is required because of the way Java handles shifts, and unsigned longs
        // Continue doing shift masks while we don't hit an empty cell, and we hit an opponent's chip
        // Calculates potential moves at the first shift by shifting the players board in the correct direction
        // bitwise and the result with the mask to get rid of the bits that are not on the board and the opponent's chips
        // to get rid of the empty cells
        long potentialMoves = (shift > 0 ? playerBoard >> shift : playerBoard << -shift) & mask & opponentBoard;
        while (potentialMoves != 0L) {
            // potentialShift = potentialMoves shifted in the direction of the shift, continue the shift
            long potentialShift = (shift > 0 ? potentialMoves >> shift : potentialMoves << -shift) & mask;
            // updates the valid moves with the potential shift
            validMoves |= potentialShift & emptyCells;
            // Recheck if the shift still hits an opponent's chip and not an empty one
            potentialMoves = potentialShift & opponentBoard;
        }
        return validMoves;
    }

    /**
     * This method calculates the chips to be flipped in a specific direction on a bitboard for the game Othello (also known as Reversi).
     *
     * @param playerBoard   The bitboard representing the current player's discs.
     * @param opponentBoard The bitboard representing the opponent's discs.
     * @param move          The bitboard representing the move to be made.
     * @param shift         The direction in which to check for flips. This is represented as an integer where positive values shift to the right and negative values shift to the left.
     * @param mask          The mask to apply to the shifted bitboard to ensure that the shift does not wrap around to the other side of the board.
     * @return A long value where each bit represents a potential chip to be flipped. If a bit is set (1), then the corresponding position on the board is a chip to be flipped.
     */
    public long getChipsToFlipInDirection(long playerBoard, long opponentBoard, long move, int shift, long mask) {
        // Initialize the chips to flip as 0
        long chipsToFlip = 0L;
        // Calculate the potential flips by shifting the move in the given direction and then performing a bitwise AND operation with the mask
        long potentialFlips = (shift > 0 ? move >> shift : move << -shift) & mask;
        // Continue as long as there are potential flips and the potential flips are on the opponent's board
        while (potentialFlips != 0 && (potentialFlips & opponentBoard) != 0) {
            // Add the potential flips to the chips to flip
            chipsToFlip |= potentialFlips;
            // Calculate the next potential flips by shifting the current potential flips in the given direction and then performing a bitwise AND operation with the mask
            potentialFlips = (shift > 0 ? potentialFlips >> shift : potentialFlips << -shift) & mask;
        }
        // If the last potential flip is not on the player's board, then no chips will be flipped in this direction
        if ((potentialFlips & playerBoard) == 0) {
            chipsToFlip = 0;
        }
        // Return the chips to flip
        return chipsToFlip;
    }

    public boolean isValidMove(boolean isPlayerOne, int x, int y) {
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
            return false;
        }
        if (getCell(x, y) != 0) {
            return false;
        }
        ArrayList<int[]> adjacentEnemies = getAdjacentEnemies(isPlayerOne, x, y);
        if (adjacentEnemies.isEmpty()) {
            return false;
        }
        return CheckIfCanTrap(isPlayerOne, x, y, adjacentEnemies);
    }

    public GameStatus determineWinner() {
        int playerOneChips = Long.bitCount(playerOneBoard);
        int playerTwoChips = Long.bitCount(playerTwoBoard);

        if (playerOneChips > playerTwoChips) {
            return GameStatus.PLAYER_1_WON;
        } else if (playerTwoChips > playerOneChips) {
            return GameStatus.PLAYER_2_WON;
        } else {
            return GameStatus.DRAW;
        }
    }

    public boolean possibleMoveExists() {
        long emptyCells = getEmptyBoard();
        for (int i = 0; i < 64; i++) {
            if ((emptyCells & (1L << i)) != 0) {
                for (int direction : BIT_DIRECTIONS) {
                    if (canFlip(i, direction)) return true;
                }
            }
        }
        return false;
    }

    public boolean canFlip(int index, int direction) {
        if (index + direction < 0 || index + direction >= 64) return false;
        if ((playerOneBoard & (1L << index + direction)) == 0 && (playerTwoBoard & (1L << index + direction)) == 0)
            return false;
        long playerBoard = (playerOneBoard & (1L << index + direction)) == 0 ? playerOneBoard : playerTwoBoard;
        long opponentBoard = (playerOneBoard & (1L << index + direction)) == 0 ? playerTwoBoard : playerOneBoard;
        long flipped = 0L;
        int shift = index + direction;
        for (int i = shift; i >= 0 && i < 64; i += direction) {
            if ((playerBoard & (1L << i)) != 0) return flipped != 0L;
            else if ((opponentBoard & (1L << i)) != 0) {
                flipped |= (1L << i);
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Check and return the status of the game, if there is a winner, a draw or still running.
     *
     * @return the current game status.
     */
    /*@Override
    public GameStatus gameStatus() {
        if (playerOneChips == 0) return GameStatus.PLAYER_2_WON;
        if (playerTwoChips == 0) return GameStatus.PLAYER_1_WON;

        if (isGameOver()) return determineWinner();

        return GameStatus.RUNNING;
    }*/
    public GameStatus gameStatus() {
        if (playerOneBoard == 0L) return GameStatus.PLAYER_2_WON;
        if (playerTwoBoard == 0L) return GameStatus.PLAYER_1_WON;
        if (getEmptyBoard() == 0L) return determineWinner();
        if (moveHistory.size() >= 2 && moveHistory.get(moveHistory.size() - 1).x == -1 && moveHistory.get(moveHistory.size() - 2).x == -1)
            return determineWinner();
        if ((getValidMoves() == 0L)) return determineWinner();
        return GameStatus.RUNNING;
    }

    public boolean hasPossibleMoves(boolean isPlayerOne) {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                ArrayList<int[]> adjacentEnemies = getAdjacentEnemies(isPlayerOne, x, y);
                if (getCell(x, y) == 0 && !adjacentEnemies.isEmpty() && CheckIfCanTrap(isPlayerOne, x, y, adjacentEnemies)) {
                    return true;
                }
            }
        }
        return false;
    }

    public long getValidMoves() {
        long validMoves = 0L;
        long emptyCells = getEmptyBoard();

        validMoves |= getValidMovesInDirection(playerOneBoard, playerTwoBoard, emptyCells, (BOARD_SIZE), DOWN_MASK);
        validMoves |= getValidMovesInDirection(playerOneBoard, playerTwoBoard, emptyCells, -BOARD_SIZE, UP_MASK);
        validMoves |= getValidMovesInDirection(playerOneBoard, playerTwoBoard, emptyCells, 1, RIGHT_MASK);
        validMoves |= getValidMovesInDirection(playerOneBoard, playerTwoBoard, emptyCells, -1, LEFT_MASK);
        validMoves |= getValidMovesInDirection(playerOneBoard, playerTwoBoard, emptyCells, BOARD_SIZE + 1, RIGHT_MASK & DOWN_MASK);
        validMoves |= getValidMovesInDirection(playerOneBoard, playerTwoBoard, emptyCells, BOARD_SIZE - 1, LEFT_MASK & DOWN_MASK);
        validMoves |= getValidMovesInDirection(playerOneBoard, playerTwoBoard, emptyCells, -(BOARD_SIZE - 1), RIGHT_MASK & UP_MASK);
        validMoves |= getValidMovesInDirection(playerOneBoard, playerTwoBoard, emptyCells, -(BOARD_SIZE + 1), LEFT_MASK & UP_MASK);
        validMoves |= getValidMovesInDirection(playerTwoBoard, playerOneBoard, emptyCells, BOARD_SIZE, DOWN_MASK);
        validMoves |= getValidMovesInDirection(playerTwoBoard, playerOneBoard, emptyCells, -BOARD_SIZE, UP_MASK);
        validMoves |= getValidMovesInDirection(playerTwoBoard, playerOneBoard, emptyCells, 1, RIGHT_MASK);
        validMoves |= getValidMovesInDirection(playerTwoBoard, playerOneBoard, emptyCells, -1, LEFT_MASK);
        validMoves |= getValidMovesInDirection(playerTwoBoard, playerOneBoard, emptyCells, BOARD_SIZE + 1, RIGHT_MASK & DOWN_MASK);
        validMoves |= getValidMovesInDirection(playerTwoBoard, playerOneBoard, emptyCells, BOARD_SIZE - 1, LEFT_MASK & DOWN_MASK);
        validMoves |= getValidMovesInDirection(playerTwoBoard, playerOneBoard, emptyCells, -(BOARD_SIZE - 1), RIGHT_MASK & UP_MASK);
        validMoves |= getValidMovesInDirection(playerTwoBoard, playerOneBoard, emptyCells, -(BOARD_SIZE + 1), LEFT_MASK & UP_MASK);
        return validMoves;
    }



    /*@Override
    public List<Move> getPossibleMoves(boolean isPlayerOne) {

        if ((moveHistory.isEmpty() && !isPlayerOne) || (!moveHistory.isEmpty() && (moveHistory.get(moveHistory.size() - 1).isPlayerOne() == isPlayerOne)))
            return null;
        // Original version

        List<Move> moves = new ArrayList<>();
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (getCell(x, y) == EMPTY) {
                    ArrayList<int[]> adjacentEnemies = getAdjacentEnemies(isPlayerOne, x, y);
                    if ((adjacentEnemies.isEmpty()) || !CheckIfCanTrap(isPlayerOne, x, y, adjacentEnemies)) {
                        continue;
                    }
                } else {
                    continue;
                }
                moves.add(new Move(x, y));
            }
        }

        return moves;
    }*/

    /**
     * Get all adjacent enemy chips for a given move.
     *
     * @param isPlayerOne true if player 1, else player 2.
     * @param x           the x coordinate of the move.
     * @param y           the y coordinate of the move.
     * @return a list of all adjacent enemy chips.
     */
    public ArrayList<int[]> getAdjacentEnemies(boolean isPlayerOne, int x, int y) {
        int opponent = isPlayerOne ? PLAYER_TWO : PLAYER_ONE;
        ArrayList<int[]> adjacentEnemies = new ArrayList<>();

        for (int[] direction : OthelloGame.DIRECTIONS) {
            int newX = x + direction[0];
            int newY = y + direction[1];

            if (newX >= 0 && newX < OthelloGame.BOARD_SIZE && newY >= 0 && newY < OthelloGame.BOARD_SIZE) {
                if (getCell(newX, newY) == opponent) {
                    adjacentEnemies.add(new int[]{newY, newX});
                }
            }
        }

        return adjacentEnemies;
    }

    /**
     * This method retrieves the state of a cell on the Othello board.
     *
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     * @return The state of the cell. It returns PLAYER_ONE if the cell is occupied by player one's disc,
     *         PLAYER_TWO if the cell is occupied by player two's disc, and EMPTY if the cell is not occupied.
     */
    public int getCell(int x, int y) {
        int indexToGet = y * BOARD_SIZE + x;
        if (((playerOneBoard >>> indexToGet) & 1) == 1) {
            return PLAYER_ONE;
        } else if (((playerTwoBoard >>> indexToGet) & 1) == 1) {
            return PLAYER_TWO;
        } else {
            return EMPTY;
        }
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
     * Check whether the current player can trap the enemy chip at the given coordinates.
     *
     * @param playerOne true if player 1, else player 2.
     * @param x         the x coordinate of the move.
     * @param y         the y coordinate of the move.
     * @return true if the current player can trap the enemy chip at the given coordinates.
     */
    public boolean CheckIfCanTrap(boolean playerOne, int x, int y, ArrayList<int[]> adjacentEnemies) {
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

    public List<int[]> getAdjacentPositions(int x, int y) {
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
     * Check whether the given coordinates are adjacent to an enemy chip.
     *
     * @param isPlayerOne true if player 1, else player 2.
     * @param x           the x coordinate of the move.
     * @param y           the y coordinate of the move.
     * @return true if the given coordinates are adjacent to an enemy chip.
     */
    public boolean checkAdjacent(boolean isPlayerOne, int x, int y) {
        int opponent = isPlayerOne ? PLAYER_TWO : PLAYER_ONE;

        for (int[] direction : OthelloGame.DIRECTIONS) {
            int newX = x + direction[0];
            int newY = y + direction[1];

            if (newX >= 0 && newX < OthelloGame.BOARD_SIZE && newY >= 0 && newY < OthelloGame.BOARD_SIZE) {
                if (getCell(newX, newY) == opponent) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean CheckIfCanTrap(boolean playerOne, int x, int y, int[] adjacentEnemy) {
        int playerCell = playerOne ? 1 : 2;

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
        ArrayList<int[]> adjacentEnemies = getAdjacentEnemies(isPlayerOne, x, y);
        int playerChip = isPlayerOne ? PLAYER_ONE : PLAYER_TWO;
        for (int[] adjacentEnemy : adjacentEnemies) {
            int[] direction = {adjacentEnemy[0] - y, adjacentEnemy[1] - x};
            int newX = adjacentEnemy[1];
            int newY = adjacentEnemy[0];
            while (newX >= 0 && newX < OthelloGame.BOARD_SIZE && newY >= 0 && newY < OthelloGame.BOARD_SIZE) {
                int cell = getCell(newX, newY);
                if (cell == EMPTY) break;
                if (cell == playerChip) {
                    int flipX = adjacentEnemy[1];
                    int flipY = adjacentEnemy[0];
                    while (flipX != newX || flipY != newY) {
                        setCell(playerChip, flipX, flipY);
                        playerOneChips += (playerChip == PLAYER_ONE ? 1 : -1);
                        playerTwoChips += (playerChip == PLAYER_TWO ? 1 : -1);
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
            setCell((getCell(flipped[1], flipped[0]) == PLAYER_ONE ? PLAYER_TWO : PLAYER_ONE), flipped[1], flipped[0]);            // Update the chip count
            // playerOneChips += (board[flipped[0]][flipped[1]] == PLAYER_ONE ? 1 : -1);
            playerOneChips += (getCell(flipped[1], flipped[0]) == PLAYER_ONE ? 1 : -1);
            playerTwoChips += (getCell(flipped[1], flipped[0]) == PLAYER_TWO ? 1 : -1);
            // playerTwoChips += (board[flipped[0]][flipped[1]] == PLAYER_TWO ? 1 : -1);
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

    /*public GameStatus determineWinner() {
        if (playerOneChips > playerTwoChips) {
            return GameStatus.PLAYER_1_WON;
        } else if (playerOneChips < playerTwoChips) {
            return GameStatus.PLAYER_2_WON;
        } else {
            return GameStatus.DRAW;
        }
    }*/

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

    public OthelloGame copy() {
        OthelloGame copy = new OthelloGame();
        copy.playerOneBoard = playerOneBoard;
        copy.playerTwoBoard = playerTwoBoard;
        copy.playerOneChips = playerOneChips;
        copy.playerTwoChips = playerTwoChips;
        copy.moveHistory.addAll(moveHistory);
        return copy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerOneBoard, playerTwoBoard, moveHistory);
    }

    /**
     * Get a String representation of the board.
     *
     * @return a String representation of the board.
     */

    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                switch (getCell(x, y)) {
                    // Empty first because it's the most likely
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

    public long getUP_MASK() {
        return UP_MASK;
    }

    public long getDOWN_MASK() {
        return DOWN_MASK;
    }

    public long getRIGHT_MASK() {
        return RIGHT_MASK;
    }

    public long getLEFT_MASK() {
        return LEFT_MASK;
    }

    public long getPlayerOneBoard() {
        return playerOneBoard;
    }

    public long getPlayerTwoBoard() {
        return playerTwoBoard;
    }
}