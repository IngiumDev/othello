package de.lmu.bio.ifi.players.montecarlo;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;

import java.util.List;
import java.util.Random;

public class MonteCarloTreeSearch {
    // Obviously it uses time to continue one iteration of the search, so we need to reduce the time by a factor
    private final double REDUCTION_FACTOR = 0.75;
    private final Random RANDOM;
    private final boolean IS_PLAYING_AS_PLAYER_ONE;
    private final double TIME_TO_SUBTRACT_EACH_MOVE = 15;
    private final double C;
    private MonteCarloNode rootNode;


    public MonteCarloTreeSearch(boolean IS_PLAYING_AS_PLAYER_ONE, MonteCarloNode rootNode, Random rnd, double C) {
        this.IS_PLAYING_AS_PLAYER_ONE = IS_PLAYING_AS_PLAYER_ONE;
        this.rootNode = rootNode;
        this.RANDOM = rnd;
        this.C = C;
    }

    /*
        Selection: Start from root R and select successive child nodes until a leaf node L is reached. The root is the current game state and a leaf is any node that has a potential child from which no simulation (playout) has yet been initiated. The section below says more about a way of biasing choice of child nodes that lets the game tree expand towards the most promising moves, which is the essence of Monte Carlo tree search.
        Expansion: Unless L ends the game decisively (e.g. win/loss/draw) for either player, create one (or more) child nodes and choose node C from one of them. Child nodes are any valid moves from the game position defined by L.
        Simulation: Complete one random playout from node C. This step is sometimes also called playout or rollout. A playout may be as simple as choosing uniform random moves until the game is decided (for example in chess, the game is won, lost, or drawn).
        Backpropagation: Use the result of the playout to update information in the nodes on the path from C to R.
        */
    public Move findNextMove(long timetoCalcThisMove) {
        long startTimeForMove = System.currentTimeMillis();
        expandNode(rootNode);
        while ((System.currentTimeMillis() - startTimeForMove) < timetoCalcThisMove - TIME_TO_SUBTRACT_EACH_MOVE) {
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

    public static Move findMoveThatWouldCaptureMove(OthelloGame game, List<Move> moves) {
        boolean isPlayerOne = game.getPlayerTurnNumber() == 1;
        Move bestmove = moves.get(0);
        int bestScore = 0;
        for (Move move : moves) {
            OthelloGame tempGame = game.copy();
            tempGame.makeMove(tempGame.getPlayerTurnNumber() == 1, move.x, move.y);
            int score = isPlayerOne ? tempGame.getPlayerOneChips() : tempGame.getPlayerTwoChips();
            if (score > bestScore) {
                bestScore = score;
                bestmove = move;
            }
        }
        return bestmove;
    }

    // Selection
    private MonteCarloNode selectPromisingNode(MonteCarloNode rootNode) {
        MonteCarloNode bestNode;
        bestNode = rootNode.findBestNodeByUCT();
        return bestNode;
    }

    // Expansion
    public void expandNode(MonteCarloNode nodeToExpand) {
        if (!nodeToExpand.hasBeenExpanded()) {
        OthelloGame nodeGame = nodeToExpand.getGame();
        boolean isPlayerOne = nodeGame.getPlayerTurnNumber() == 1;
        List<Move> possibleMoves = nodeGame.parseValidMovesToMoveList(nodeGame.getValidMoves(isPlayerOne));
        if (possibleMoves.isEmpty()) {
            OthelloGame newGame = nodeGame.copy();
            newGame.makeMove(isPlayerOne, -1, -1);
            MonteCarloNode newNode = new MonteCarloNode(nodeToExpand, newGame, new Move(-1, -1), C);
            nodeToExpand.getChildren().add(newNode);
        } else {
            for (Move move : possibleMoves) {
                OthelloGame newGame = nodeGame.copy();
                newGame.makeMove(isPlayerOne, move.x, move.y);
                MonteCarloNode newNode = new MonteCarloNode(nodeToExpand, newGame, move, C);
                nodeToExpand.getChildren().add(newNode);
            }
        }
            nodeToExpand.nowExpanded();
        }

    }

    // Backpropagate
    private void recursiveUpdateScore(MonteCarloNode nodeToExplore, int endOfGameResult) {
        MonteCarloNode nodeToUpdate = nodeToExplore;
        while (nodeToUpdate != null) {
            nodeToUpdate.incrementVisitCount();
            nodeToUpdate.addWinScore(endOfGameResult);
            nodeToUpdate = nodeToUpdate.getParent();
        }
    }

    private int scoreGameStatus(OthelloGame game) {
        int score;
        GameStatus status = game.gameStatus();
        if (status == GameStatus.PLAYER_1_WON) {
            score = IS_PLAYING_AS_PLAYER_ONE ? 1 : -1;
        } else if (status == GameStatus.PLAYER_2_WON) {
            score = IS_PLAYING_AS_PLAYER_ONE ? -1 : 1;
        } else {
            score = 0;
        }
        return score;
    }

    public boolean makeMove(Move move) {
        // Search through root node's children to find the one that matches the move;
        if (rootNode.getChildren() == null || rootNode.getChildren().isEmpty()) {
            expandNode(rootNode);
        }
        for (MonteCarloNode child : rootNode.getChildren()) {
            Move childMove = child.getMoveThatCreatedThisNode();
            OthelloGame childGame = child.getGame();
            if (childMove.x == move.x && childMove.y == move.y) {
                // Remove the parent node and make the child the new root node
                child.makeOrphan();
                rootNode = child;
                return true;
            }
        }
        return false;
    }

    // TODO: Optimize with bit: use valid moves from long and iterate bits
    // Simulate
    private int simulateRandomGameUntilEnd(MonteCarloNode nodeToExplore) {
        OthelloGame tempGame = nodeToExplore.getGame().copy();
        boolean isPlayerOne = tempGame.getPlayerTurnNumber() == 1;
        GameStatus gameStatus = tempGame.gameStatus();
        while (gameStatus == GameStatus.RUNNING) {
            List<Move> possibleMoves = tempGame.parseValidMovesToMoveList(tempGame.getValidMoves(isPlayerOne));
            if (possibleMoves.isEmpty()) {
                tempGame.forceMakeMove(isPlayerOne, new Move(-1, -1));
            } else {
                Move randomMove = possibleMoves.get(RANDOM.nextInt(possibleMoves.size()));
                tempGame.forceMakeMove(isPlayerOne, randomMove);
            }
            isPlayerOne = !isPlayerOne;
            gameStatus = tempGame.gameStatus();
        }
        return scoreGameStatus(tempGame);
    }

    public MonteCarloNode getRootNode() {
        return rootNode;
    }
}
