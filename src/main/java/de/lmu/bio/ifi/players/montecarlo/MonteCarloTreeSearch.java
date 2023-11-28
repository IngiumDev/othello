package de.lmu.bio.ifi.players.montecarlo;

import de.lmu.bio.ifi.GameStatus;
import de.lmu.bio.ifi.OthelloGame;
import de.lmu.bio.ifi.players.montecarlo.movestrategies.*;

import java.util.Random;

public class MonteCarloTreeSearch {
    public static final double EPSILON = 0.2;
    // Obviously it uses time to continue one iteration of the search, so we need to reduce the time by a factor
    private final double REDUCTION_FACTOR = 0.75;
    private final Random RANDOM;
    private final boolean IS_PLAYING_AS_PLAYER_ONE;
    private final double TIME_TO_SUBTRACT_EACH_MOVE = 15;
    private final double C;
    private MonteCarloNode rootNode;
    private int totalSimulations = 0;


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
    public long findNextMove(long timetoCalcThisMove) {
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
            int simulatedGameResult = simulateGameUntilEnd(nodeToExplore);
            recursiveUpdateScore(nodeToExplore, simulatedGameResult);
            //totalSimulations++;
        }
        return rootNode.getBestChildNode().getMoveThatCreatedThisNode();
    }

    // Expansion
    public void expandNode(MonteCarloNode nodeToExpand) {
        if (!nodeToExpand.hasBeenExpanded()) {
            OthelloGame nodeGame = nodeToExpand.getGame();
            boolean isPlayerOne = nodeGame.getPlayerTurnNumber() == 1;
            long possibleMovesLong = nodeGame.getValidMoves(isPlayerOne);
            if (possibleMovesLong == 0L) {
                OthelloGame newGame = nodeGame.copy();
                newGame.forceMakeMove(isPlayerOne, possibleMovesLong);
                MonteCarloNode newNode = new MonteCarloNode(nodeToExpand, newGame, possibleMovesLong, C);
                nodeToExpand.getChildren().add(newNode);
            } else {
                // iterate over the set bits in possibleMovesLong
                while (possibleMovesLong != 0L) {
                    // find the index of the next set bit
                    int i = Long.numberOfTrailingZeros(possibleMovesLong);
                    // create a long value with only that bit set
                    long bit = Long.lowestOneBit(possibleMovesLong);
                    // make the move corresponding to that bit
                    OthelloGame newGame = nodeGame.copy();
                    newGame.forceMakeMove(isPlayerOne, bit);
                    MonteCarloNode newNode = new MonteCarloNode(nodeToExpand, newGame, bit, C);
                    nodeToExpand.getChildren().add(newNode);
                    // clear that bit from possibleMovesLong
                    possibleMovesLong ^= bit;
                }

            }
            nodeToExpand.nowExpanded();
        }
    }

    // Selection
    private MonteCarloNode selectPromisingNode(MonteCarloNode rootNode) {
        MonteCarloNode bestNode;
        bestNode = rootNode.findBestNodeByUCT();
        return bestNode;
    }

    private int simulateGameUntilEnd(MonteCarloNode nodeToExplore) {
        OthelloGame tempGame = nodeToExplore.getGame().copy();
        boolean isPlayerOne = tempGame.getPlayerTurnNumber() == 1;
        GameStatus gameStatus = GameStatus.RUNNING;
        MoveStrategy randomMoveStrategy = new RandomMoveStrategy();
//        MoveStrategy cornerMoveStrategy = new CornerMoveStrategy();
//        MoveStrategy matrixMoveStrategy = new MatrixMoveStrategy();
        MoveStrategy matrixChanceMoveStrategy = new MatrixChanceMoveStrategy();
        while (gameStatus == GameStatus.RUNNING) {
            long move;
            // epsilon so that it doesn't always choose the best move
            if (RANDOM.nextDouble() < EPSILON) {
                move = matrixChanceMoveStrategy.getMove(tempGame, isPlayerOne, RANDOM);
            } else {
                move = randomMoveStrategy.getMove(tempGame, isPlayerOne, RANDOM);
            }
//            move = cornerMoveStrategy.getMove(tempGame, isPlayerOne, RANDOM);
            tempGame.forceMakeMove(isPlayerOne, move);
            isPlayerOne = !isPlayerOne;
            gameStatus = tempGame.gameStatus();
        }
        return scoreGameStatus(gameStatus);
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

    private int scoreGameStatus(GameStatus status) {
        if (status == GameStatus.PLAYER_1_WON) {
            return IS_PLAYING_AS_PLAYER_ONE ? 1 : -1;
        } else if (status == GameStatus.PLAYER_2_WON) {
            return IS_PLAYING_AS_PLAYER_ONE ? -1 : 1;
        } else {
            return 0;
        }
    }

    // Simulate

    public boolean makeMove(long move) {
        // Search through root node's children to find the one that matches the move;
        if (rootNode.getChildren() == null || rootNode.getChildren().isEmpty()) {
            expandNode(rootNode);
        }
        for (MonteCarloNode child : rootNode.getChildren()) {
            long childMove = child.getMoveThatCreatedThisNode();
            if (childMove == move) {
                // Remove the parent node and make the child the new root node
                child.makeOrphan();
                rootNode = child;
                return true;
            }
        }
        return false;
    }

    public MonteCarloNode getRootNode() {
        return rootNode;
    }

    public int getTotalSimulations() {
        return totalSimulations;
    }
}
