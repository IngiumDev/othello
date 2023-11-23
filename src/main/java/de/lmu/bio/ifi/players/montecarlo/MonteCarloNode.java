package de.lmu.bio.ifi.players.montecarlo;

import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;

import java.util.ArrayList;
import java.util.List;

public class MonteCarloNode {
    // Exploration parameter, normally around sqrt(2), but I found
    private final double C;
    private MonteCarloNode parent;
    private OthelloGame game;
    private List<MonteCarloNode> children;
    private long moveThatCreatedThisNode;
    private int visitCount;
    private int winScore;
    private List<Move> untriedMoves;
    private boolean hasBeenExpanded = false;


    public MonteCarloNode(MonteCarloNode parent, OthelloGame game, long moveThatCreatedThisNode, double C) {
        this.parent = parent;
        this.game = game;
        this.moveThatCreatedThisNode = moveThatCreatedThisNode;
        this.children = new ArrayList<>();
        this.C = C;
    }

    public MonteCarloNode(OthelloGame game, double C) {
        this.game = game;
        this.children = new ArrayList<>();
        this.C = C;
    }


    public MonteCarloNode getParent() {
        return parent;
    }

    public void setParent(MonteCarloNode parent) {
        this.parent = parent;
    }

    public OthelloGame getGame() {
        return game;
    }

    public void setGame(OthelloGame game) {
        this.game = game;
    }

    public List<MonteCarloNode> getChildren() {
        return children;
    }

    public void setChildren(List<MonteCarloNode> children) {
        this.children = children;
    }

    public long getMoveThatCreatedThisNode() {
        return moveThatCreatedThisNode;
    }

    public void setMoveThatCreatedThisNode(long moveThatCreatedThisNode) {
        this.moveThatCreatedThisNode = moveThatCreatedThisNode;
    }

    public void incrementVisitCount() {
        visitCount++;
    }

    public void addWinScore(int winScore) {
        this.winScore += winScore;
    }

    public List<Move> getUntriedMoves() {
        return untriedMoves;
    }

    public void setUntriedMoves(List<Move> untriedMoves) {
        this.untriedMoves = untriedMoves;
    }

    public void removeUntriedMove(Move move) {
        untriedMoves.remove(move);
    }

    public MonteCarloNode findBestNodeByUCT() {
        if (children.size() == 1) {
            return children.get(0);
        }
        int parentVisit = this.visitCount;
        MonteCarloNode bestNode = null;
        double bestUCT = Double.NEGATIVE_INFINITY;
        for (MonteCarloNode child : children) {
            double uctValue = child.UCTValue(parentVisit);
            if (uctValue > bestUCT) {
                bestUCT = uctValue;
                bestNode = child;
            }
        }

        return bestNode;
    }

    public double UCTValue(int totalVisit) {
        if (visitCount == 0) {
            return Integer.MAX_VALUE;
        }
        return ((double) winScore / (double) visitCount) + C * Math.sqrt(Math.log(totalVisit) / (double) visitCount);
    }

    public MonteCarloNode getRandomChildNode() {
        int size = children.size();
        int randomIndex = (int) (Math.random() * size);
        return children.get(randomIndex);
    }

    public MonteCarloNode getBestChildNode() {
        MonteCarloNode bestNode = children.get(0);
        double bestScore = (bestNode.getWinScore() / ((double) bestNode.getVisitCount()));
        for (int i = 1, childrenSize = children.size(); i < childrenSize; i++) {
            MonteCarloNode child = children.get(i);
            double score = (child.getWinScore() / ((double) child.getVisitCount()));
            if (score > bestScore) {
                bestScore = score;
                bestNode = child;
            }
        }
        return bestNode;
    }

    public int getWinScore() {
        return winScore;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public void setWinScore(int winScore) {
        this.winScore = winScore;
    }

    public void makeOrphan() {
        this.parent = null;
    }

    public void nowExpanded() {
        this.hasBeenExpanded = true;
    }

    public boolean hasBeenExpanded() {
        return this.hasBeenExpanded;
    }
}
