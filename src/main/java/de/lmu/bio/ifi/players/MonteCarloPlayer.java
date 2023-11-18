package de.lmu.bio.ifi.players;

import de.lmu.bio.ifi.OthelloGame;
import de.lmu.bio.ifi.players.montecarlo.MonteCarloNode;
import de.lmu.bio.ifi.players.montecarlo.MonteCarloTreeSearch;
import szte.mi.Move;
import szte.mi.Player;

import java.util.List;
import java.util.Random;

public class MonteCarloPlayer implements Player {


    private OthelloGame mainGame;

    private boolean isPlayerOne;
   private MonteCarloTreeSearch monteCarloTreeSearch;


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
        MonteCarloNode root = new MonteCarloNode(mainGame);
        this.monteCarloTreeSearch = new MonteCarloTreeSearch(isPlayerOne, root, rnd);
        this.monteCarloTreeSearch.expandNode(root);
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
        if (prevMove != null) {
            mainGame.makeMove(!isPlayerOne, prevMove.x, prevMove.y);
            monteCarloTreeSearch.makeMove(prevMove);
        }
        if (prevMove == null) {
            if (!mainGame.getMoveHistory().isEmpty()) {
                mainGame.makeMove(!isPlayerOne, -1, -1);
                monteCarloTreeSearch.makeMove(new Move(-1, -1));
            }
        }

        List<Move> moves = mainGame.parseValidMovesToMoveList(mainGame.getValidMoves(isPlayerOne));
        if (moves == null || moves.isEmpty()) {
            mainGame.makeMove(isPlayerOne, -1, -1);
            monteCarloTreeSearch.makeMove(new Move(-1, -1));
            return null;
        }

        Move bestMove;
        int remainingMoves = (64 - mainGame.getAmountOfChipsPlaced()) / 2;
        if (remainingMoves == 0) {
            remainingMoves = 1;
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        long timeToCalculateThisMove = (long) (((t - elapsedTime) / remainingMoves) * 0.7);

        // TODO: Save the tree between moves
        bestMove = monteCarloTreeSearch.findNextMove(timeToCalculateThisMove);
        monteCarloTreeSearch.makeMove(bestMove);
        mainGame.makeMove(isPlayerOne, bestMove.x, bestMove.y);
        return bestMove;
    }

}
