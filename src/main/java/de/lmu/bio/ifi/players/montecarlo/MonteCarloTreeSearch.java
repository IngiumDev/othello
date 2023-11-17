package de.lmu.bio.ifi.players.montecarlo;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;

import java.util.List;

public class MonteCarloTreeSearch {
    // Obviously it uses time to continue one iteration of the search, so we need to reduce the time by a factor
    final double REDUCTION_FACTOR = 0.88;
    boolean isPlayeringasPlayerOne;

    /*
    Selection: Start from root R and select successive child nodes until a leaf node L is reached. The root is the current game state and a leaf is any node that has a potential child from which no simulation (playout) has yet been initiated. The section below says more about a way of biasing choice of child nodes that lets the game tree expand towards the most promising moves, which is the essence of Monte Carlo tree search.
    Expansion: Unless L ends the game decisively (e.g. win/loss/draw) for either player, create one (or more) child nodes and choose node C from one of them. Child nodes are any valid moves from the game position defined by L.
    Simulation: Complete one random playout from node C. This step is sometimes also called playout or rollout. A playout may be as simple as choosing uniform random moves until the game is decided (for example in chess, the game is won, lost, or drawn).
    Backpropagation: Use the result of the playout to update information in the nodes on the path from C to R.
    */

    public Move findNextMove(OthelloGame mainGame, boolean isPlayerOne, long timetoCalcThisMove) {
        this.isPlayeringasPlayerOne = isPlayerOne;
        long startTimeForMove = System.currentTimeMillis();
        MonteCarloNode rootNode = new MonteCarloNode(mainGame);
        expandNode(rootNode);
        while ((System.currentTimeMillis() - startTimeForMove) < timetoCalcThisMove * REDUCTION_FACTOR) {
            MonteCarloNode promisingNode = selectPromisingNode(rootNode);
            if (promisingNode.getGame().gameStatus() == GameStatus.RUNNING) {
                expandNode(promisingNode);
            }
            MonteCarloNode nodeToExplore = promisingNode;
            if (!promisingNode.getChildren().isEmpty()) {
                nodeToExplore = promisingNode.getRandomChildNode();
            }
            // 1: My player won, 0: Draw, -1: My player lost
            int playoutResult = simulateRandomGameUntilEnd(nodeToExplore);
            recursiveUpdateScore(nodeToExplore, playoutResult);
        }
        return rootNode.getBestChildNode().getMoveThatCreatedThisNode();
    }

    private void expandNode(MonteCarloNode nodeToExpand) {
        OthelloGame nodeGame = nodeToExpand.getGame();
        boolean isPlayerOne = nodeGame.getPlayerTurnNumber() == 1;
        List<Move> possibleMoves = nodeGame.parseValidMovesToMoveList(nodeGame.getValidMoves(isPlayerOne));
        if (possibleMoves.isEmpty()) {
            OthelloGame newGame = nodeGame.copy();
            newGame.makeMove(isPlayerOne, -1, -1);
            MonteCarloNode newNode = new MonteCarloNode(nodeToExpand, newGame, new Move(-1, -1));
            nodeToExpand.getChildren().add(newNode);
        } else {
            for (Move move : possibleMoves) {
                OthelloGame newGame = nodeGame.copy();
                newGame.makeMove(isPlayerOne, move.x, move.y);
                MonteCarloNode newNode = new MonteCarloNode(nodeToExpand, newGame, move);
                nodeToExpand.getChildren().add(newNode);
            }
        }

    }

    private MonteCarloNode selectPromisingNode(MonteCarloNode rootNode) {
        MonteCarloNode bestNode;
        bestNode = rootNode.findBestNodeByUCT();
        return bestNode;
    }

    private int simulateRandomGameUntilEnd(MonteCarloNode nodeToExplore) {
        OthelloGame tempGame = nodeToExplore.getGame().copy();
        boolean isPlayerOne = tempGame.getPlayerTurnNumber() == 1;
        GameStatus gameStatus = tempGame.gameStatus();
        while (gameStatus == GameStatus.RUNNING) {
            List<Move> possibleMoves = tempGame.parseValidMovesToMoveList(tempGame.getValidMoves(isPlayerOne));
            if (possibleMoves.isEmpty()) {
                tempGame.makeMove(isPlayerOne, -1, -1);
            } else {
                Move randomMove = possibleMoves.get((int) (Math.random() * possibleMoves.size()));
                tempGame.makeMove(isPlayerOne, randomMove.x, randomMove.y);
            }
            isPlayerOne = !isPlayerOne;
            gameStatus = tempGame.gameStatus();
        }
        return scoreBoard(tempGame);
    }

    private void recursiveUpdateScore(MonteCarloNode nodeToExplore, int endOfGameResult) {
        MonteCarloNode nodeToUpdate = nodeToExplore;
        while (nodeToUpdate != null) {
            nodeToUpdate.incrementVisitCount();
            nodeToUpdate.addWinScore(endOfGameResult);
            nodeToUpdate = nodeToUpdate.getParent();
        }
    }

    private int scoreBoard(OthelloGame game) {
        int score;
        GameStatus status = game.gameStatus();
        if (status == GameStatus.PLAYER_1_WON) {
            score = isPlayeringasPlayerOne ? 1 : -1;
        } else if (status == GameStatus.PLAYER_2_WON) {
            score = isPlayeringasPlayerOne ? -1 : 1;
        } else {
            score = 0;
        }
        return score;
    }
}
