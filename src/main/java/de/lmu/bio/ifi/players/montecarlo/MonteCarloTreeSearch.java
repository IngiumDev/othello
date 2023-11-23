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
            int playoutResult = simulateCornerGameUntilEnd(nodeToExplore);
            recursiveUpdateScore(nodeToExplore, playoutResult);
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
                for (int i = 0; i < 64; i++) {
                    long bit = 1L << i;
                    long testMove = possibleMovesLong & bit;
                    if (testMove != 0L) {
                        OthelloGame newGame = nodeGame.copy();
                        newGame.forceMakeMove(isPlayerOne, testMove);
                        MonteCarloNode newNode = new MonteCarloNode(nodeToExpand, newGame, testMove, C);
                        nodeToExpand.getChildren().add(newNode);
                    }
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

    private int simulateCornerGameUntilEnd(MonteCarloNode nodeToExplore) {
        OthelloGame tempGame = nodeToExplore.getGame().copy();
        boolean isPlayerOne = tempGame.getPlayerTurnNumber() == 1;
        GameStatus gameStatus = tempGame.gameStatus();
        while (gameStatus == GameStatus.RUNNING) {
            long possibleMoves = tempGame.getValidMoves(isPlayerOne);
            if (possibleMoves == 0L) {
                tempGame.forceMakeMove(isPlayerOne, possibleMoves);
                gameStatus = tempGame.gameStatus();
                isPlayerOne = !isPlayerOne;
            } else if (Long.bitCount(possibleMoves) == 1) {
                tempGame.forceMakeMove(isPlayerOne, possibleMoves);
                gameStatus = tempGame.gameStatus();
                isPlayerOne = !isPlayerOne;
            } else {
                // Find if there is a corner move
                if ((possibleMoves & OthelloGame.ALL_CORNERS) != 0L) {
                    if ((possibleMoves & OthelloGame.TOP_LEFT_CORNER) != 0L) {
                        tempGame.forceMakeMove(isPlayerOne, OthelloGame.TOP_LEFT_CORNER);
                    } else if ((possibleMoves & OthelloGame.TOP_RIGHT_CORNER) != 0L) {
                        tempGame.forceMakeMove(isPlayerOne, OthelloGame.TOP_RIGHT_CORNER);
                    } else if ((possibleMoves & OthelloGame.BOTTOM_LEFT_CORNER) != 0L) {
                        tempGame.forceMakeMove(isPlayerOne, OthelloGame.BOTTOM_LEFT_CORNER);
                    } else if ((possibleMoves & OthelloGame.BOTTOM_RIGHT_CORNER) != 0L) {
                        tempGame.forceMakeMove(isPlayerOne, OthelloGame.BOTTOM_RIGHT_CORNER);
                    }
                    isPlayerOne = !isPlayerOne;
                    gameStatus = tempGame.gameStatus();
                    continue;
                }
                // Get a random set bit from possibleMoves
                int numberOfSetBits = Long.bitCount(possibleMoves);
                int randomBitIndex = RANDOM.nextInt(numberOfSetBits);
                long move = Long.highestOneBit(possibleMoves);
                for (int i = 0; i < randomBitIndex; i++) {
                    possibleMoves ^= move; // unset the current highest set bit
                    move = Long.highestOneBit(possibleMoves);
                }

                tempGame.forceMakeMove(isPlayerOne, move);
                isPlayerOne = !isPlayerOne;
                gameStatus = tempGame.gameStatus();

            }

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
                Move move = possibleMoves.get(RANDOM.nextInt(possibleMoves.size()));
                tempGame.forceMakeMove(isPlayerOne, move);
            }
            isPlayerOne = !isPlayerOne;
            gameStatus = tempGame.gameStatus();
        }
        return scoreGameStatus(gameStatus);
    }

    public MonteCarloNode getRootNode() {
        return rootNode;
    }

    private int simulateGreedyGameUntilEnd(MonteCarloNode nodeToExplore) {
        OthelloGame tempGame = nodeToExplore.getGame().copy();
        boolean isPlayerOne = tempGame.getPlayerTurnNumber() == 1;
        GameStatus gameStatus = tempGame.gameStatus();
        while (gameStatus == GameStatus.RUNNING) {
            List<Move> possibleMoves = tempGame.parseValidMovesToMoveList(tempGame.getValidMoves(isPlayerOne));
            if (possibleMoves.isEmpty()) {
                tempGame.forceMakeMove(isPlayerOne, new Move(-1, -1));
            } else {
                Move move = findMoveThatCapturesMostPieces(tempGame, possibleMoves);
                tempGame.forceMakeMove(isPlayerOne, move);
            }
            isPlayerOne = !isPlayerOne;
            gameStatus = tempGame.gameStatus();
        }
        return scoreGameStatus(gameStatus);
    }

    public static Move findMoveThatCapturesMostPieces(OthelloGame game, List<Move> moves) {
        boolean isPlayerOne = game.getPlayerTurnNumber() == 1;
        Move bestmove = moves.get(0);
        int bestScore = 0;
        for (Move move : moves) {
            OthelloGame tempGame = game.copy();
            tempGame.forceMakeMove(tempGame.getPlayerTurnNumber() == 1, move);
            int score = isPlayerOne ? tempGame.getPlayerOneChips() : tempGame.getPlayerTwoChips();
            if (score > bestScore) {
                bestScore = score;
                bestmove = move;
            }
        }
        return bestmove;
    }
}
