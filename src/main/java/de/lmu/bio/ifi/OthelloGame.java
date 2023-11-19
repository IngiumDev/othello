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
    public final long RIGHT_MASK = 9187201950435737471L;
    public final long LEFT_MASK = -72340172838076674L;
    private final ArrayList<PlayerMove> moveHistory;
    private final int playerOneChips;
    private final int playerTwoChips;
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
        this.playerOneBoard = 34628173824L;
        this.playerTwoBoard = 68853694464L;
        // Created with https://tearth.dev/bitboard-viewer/
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

    public boolean forceMakeMove(boolean isPlayerOne, long move) {
        if (move == 0L) {
            // Pass move
            moveHistory.add(new PlayerMove(isPlayerOne, -1, -1));
            return true; // A pass move is always valid
        }
        if (isPlayerOne) {
            playerOneBoard |= move;
        } else {
            playerTwoBoard |= move;
        }
        doFlip(isPlayerOne, move);
        //  long movetoMake = 1L << (x + y * 8);
        // Reverse the long into x and y
        int x = Long.numberOfTrailingZeros(move) % 8;
        int y = Long.numberOfTrailingZeros(move) / 8;
        moveHistory.add(new PlayerMove(isPlayerOne, x, y));
        return true;
    }

    public void forceMakeMove(boolean isPlayerOne, Move move) {
        if (move.x == -1 && move.y == -1) {
            // Pass move
            moveHistory.add(new PlayerMove(isPlayerOne, -1, -1));
            return; // A pass move is always valid
        }
        long movetoMake = 1L << (move.x + move.y * 8);
        if (isPlayerOne) {
            playerOneBoard |= movetoMake;
        } else {
            playerTwoBoard |= movetoMake;
        }
        doFlip(isPlayerOne, movetoMake);
        moveHistory.add(new PlayerMove(isPlayerOne, move.x, move.y));
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

    // Legacy code
    @Deprecated
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

    /**
     * Check and return the status of the game, if there is a winner, a draw or still running.
     *
     * @return the current game status.
     */
    public GameStatus gameStatus() {
        if (playerOneBoard == 0L) return GameStatus.PLAYER_2_WON;
        if (playerTwoBoard == 0L) return GameStatus.PLAYER_1_WON;
        if (getEmptyBoard() == 0L) return determineWinner();
        if (moveHistory.size() >= 2 && moveHistory.get(moveHistory.size() - 1).x == -1 && moveHistory.get(moveHistory.size() - 2).x == -1)
            return determineWinner();
        if ((getValidMoves() == 0L)) return determineWinner();
        return GameStatus.RUNNING;
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

    // Translation layer
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
     * Get all adjacent enemy chips for a given move.
     *
     * @param isPlayerOne true if player 1, else player 2.
     * @param x           the x coordinate of the move.
     * @param y           the y coordinate of the move.
     * @return a list of all adjacent enemy chips.
     */
    // Legacy
    @Deprecated
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
     * Check whether the current player can trap the enemy chip at the given coordinates.
     *
     * @param playerOne true if player 1, else player 2.
     * @param x         the x coordinate of the move.
     * @param y         the y coordinate of the move.
     * @return true if the current player can trap the enemy chip at the given coordinates.
     */
    // Legacy
    @Deprecated
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

    // Translation layer
    public void setCell(int player, int x, int y) {
        // Calculate the index of the cell to set based on the x and y coordinates
        int indexToSet = y * BOARD_SIZE + x;

        // If the player is PLAYER_ONE, set the cell on player one's board and unset it on player two's board
        if (player == PLAYER_ONE) {
            // Or the player one board with a long where only the bit at the index to set is set to 1
            playerOneBoard |= 1L << indexToSet;
            // And the player two board with a long where only the bit at the index to set is set to 0
            playerTwoBoard &= ~(1L << indexToSet);
        }
        // If the player is PLAYER_TWO, set the cell on player two's board and unset it on player one's board
        else if (player == PLAYER_TWO) {
            playerTwoBoard |= 1L << indexToSet;
            playerOneBoard &= ~(1L << indexToSet);
        }
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

    // Translation layer
    public List<Move> parseValidMovesToMoveList(long validMoves) {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            if ((validMoves & (1L << i)) != 0) {
                int y = i / 8;
                int x = i % 8;
                moves.add(new Move(x, y));
            }
        }
        return moves;
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

    public OthelloGame copy() {
        OthelloGame copy = new OthelloGame();
        copy.playerOneBoard = playerOneBoard;
        copy.playerTwoBoard = playerTwoBoard;
        copy.moveHistory.addAll(moveHistory);
        return copy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerOneBoard, playerTwoBoard, moveHistory.get(moveHistory.size() - 1).isPlayerOne());
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

    public int getAmountOfChipsPlaced() {
        return Long.bitCount(playerOneBoard) + Long.bitCount(playerTwoBoard);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OthelloGame othergame = (OthelloGame) o;
        return  playerOneBoard == othergame.playerOneBoard &&
                playerTwoBoard == othergame.playerTwoBoard &&
                moveHistory.get(moveHistory.size() - 1).isPlayerOne() == othergame.moveHistory.get(othergame.moveHistory.size() - 1).isPlayerOne(); }

    public static String moveHistoryToString(ArrayList<PlayerMove> moveHistory) {
        StringBuilder output = new StringBuilder();
        for (PlayerMove move : moveHistory) {
            char letter = (char) (move.x + 'a');
            int number = move.y + 1;
            if (move.isPlayerOne()) {
                letter = Character.toUpperCase(letter);
            } else {
                letter = Character.toLowerCase(letter);
            }
            output.append(letter).append(number);
        }
        return output.toString();
    }

    public static Move convertMove(String moveStr) {
        char letter = moveStr.toLowerCase().charAt(0);
        int number = Character.getNumericValue(moveStr.charAt(1));
        int x = letter - 'a';
        int y = number - 1;
        return new Move(x, y);
    }
}

