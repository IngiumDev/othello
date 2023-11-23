package de.lmu.bio.ifi.players;

import de.lmu.bio.ifi.OpeningBook;
import de.lmu.bio.ifi.OthelloGame;
import de.lmu.bio.ifi.PlayerMove;
import de.lmu.bio.ifi.players.montecarlo.MonteCarloNode;
import de.lmu.bio.ifi.players.montecarlo.MonteCarloTreeSearch;
import szte.mi.Move;
import szte.mi.Player;

import java.util.List;
import java.util.Random;

public class MonteCarloPlayer implements Player {


    private static double REDUCTION_FACTOR = 1;
    private final double C = 1.5;
    private OthelloGame mainGame;
    private boolean isPlayerOne;
    private MonteCarloTreeSearch monteCarloTreeSearch;
    public boolean stillInOpeningBook = true;
    private OpeningBook openingBook;


    /**
     * Performs initialization depending on the parameters.
     *
     * @param order Defines the order of the players. Value 0 means
     *              this is the first player to move, 1 means second and so on.
     *              For two-player games only values 0 and 1 are possible.
     * @param t     Gives the remaining overall running time of the player in
     *              ms. Initialization is also counted as running time.
     * @param rnd   source of randomness to be used wherever random
     *              numbers are needed
     */
    @Override
    public void init(int order, long t, Random rnd) {
        assert order == 0 || order == 1;
        this.mainGame = new OthelloGame();
        this.isPlayerOne = (order == 0);
        MonteCarloNode root = new MonteCarloNode(mainGame, C);
        this.monteCarloTreeSearch = new MonteCarloTreeSearch(isPlayerOne, root, rnd, C);
        this.monteCarloTreeSearch.expandNode(root);
        this.openingBook = new OpeningBook();
    }

    public void init(int order, long t, Random rnd, boolean useOpeningBook) {
        assert order == 0 || order == 1;
        this.mainGame = new OthelloGame();
        this.isPlayerOne = (order == 0);
        MonteCarloNode root = new MonteCarloNode(mainGame, C);
        this.monteCarloTreeSearch = new MonteCarloTreeSearch(isPlayerOne, root, rnd, C);
        this.monteCarloTreeSearch.expandNode(root);
        this.stillInOpeningBook = useOpeningBook;
    }

    /**
     * Calculates the next move of the player in a two player game.
     * It is assumed that the player is stateful and the game is
     * deterministic, so the parameters only
     * give the previous move of the other player and remaining times.
     *
     * @param prevMove  the previous move of the opponent. It can be null,
     *                  which means the opponent has not moved (or this is the first move).
     * @param tOpponent remaining time of the opponent
     * @param t         remaining time for this player
     */
    @Override
    public Move nextMove(Move prevMove, long tOpponent, long t) {
        // Start timer
        long startTime = System.currentTimeMillis();
        long prevMoveLong = OthelloGame.moveToLong(prevMove);
        if (prevMoveLong != 0L) {
            mainGame.forceMakeMove(!isPlayerOne, prevMoveLong);
            monteCarloTreeSearch.makeMove(prevMoveLong);
        } else {
            if (!mainGame.getMoveHistory().isEmpty()) {
                mainGame.forceMakeMove(!isPlayerOne, prevMoveLong);
                monteCarloTreeSearch.makeMove(0L);
            }
        }

        long possibleMoves = mainGame.getValidMoves(isPlayerOne);
        if (possibleMoves == 0L) {
            mainGame.forceMakeMove(isPlayerOne, possibleMoves);
            monteCarloTreeSearch.makeMove(possibleMoves);
            return null;
        } else if (Long.bitCount(possibleMoves) == 1) {
            mainGame.forceMakeMove(isPlayerOne, possibleMoves);
            monteCarloTreeSearch.makeMove(possibleMoves);
            return OthelloGame.longToMove(possibleMoves);
        }
        if (stillInOpeningBook) {
            List<PlayerMove> moveHistory = mainGame.getMoveHistory();
            PlayerMove playerMove = openingBook.getOpeningMove(moveHistory);
            if (playerMove != null) {
                long move = playerMove.toLong();
                if (move != 0L) {
                    mainGame.forceMakeMove(isPlayerOne, move);
                    monteCarloTreeSearch.makeMove(move);
                    return playerMove;
                }
            }
            stillInOpeningBook = false;
        }
        long bestMove;
        int remainingMoves = (64 - mainGame.getAmountOfChipsPlaced()) / 2;
        if (remainingMoves == 0) {
            remainingMoves = 1;
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        if (remainingMoves > 40) {
            REDUCTION_FACTOR = 1.1;
        } else if (remainingMoves > 20) {
            REDUCTION_FACTOR = 1.5;
        } else {
            REDUCTION_FACTOR = 1;
        }
        long timeToCalculateThisMove = (long) (((t - elapsedTime) / remainingMoves) * REDUCTION_FACTOR);

        bestMove = monteCarloTreeSearch.findNextMove(timeToCalculateThisMove);
        monteCarloTreeSearch.makeMove(bestMove);
        mainGame.forceMakeMove(isPlayerOne, bestMove);
        return OthelloGame.longToMove(bestMove);
    }


    public void init(int order, long t, Random rnd, double Ctotest) {
        assert order == 0 || order == 1;
        this.mainGame = new OthelloGame();
        this.isPlayerOne = (order == 0);
        MonteCarloNode root = new MonteCarloNode(mainGame, Ctotest);
        this.monteCarloTreeSearch = new MonteCarloTreeSearch(isPlayerOne, root, rnd, Ctotest);
        this.monteCarloTreeSearch.expandNode(root);
        this.openingBook = new OpeningBook();
    }

}
