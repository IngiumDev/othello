package de.lmu.bio.ifi.players.montecarlo;

import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MonteCarloNode {
    // Exploration parameter, normally around sqrt(2), but I found
    private final double C = 1.42;
    private MonteCarloNode parent;
    private OthelloGame game;
    private List<MonteCarloNode> children;
    private Move moveThatCreatedThisNode;
    private int visitCount;
    private int winScore;
    private List<Move> untriedMoves;


    public MonteCarloNode(MonteCarloNode parent, OthelloGame game, Move moveThatCreatedThisNode) {
        this.parent = parent;
        this.game = game;
        this.moveThatCreatedThisNode = moveThatCreatedThisNode;
        this.children = new ArrayList<>();
    }

    public MonteCarloNode(OthelloGame game) {
        this.game = game;
        this.children = new ArrayList<>();
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

    public Move getMoveThatCreatedThisNode() {
        return moveThatCreatedThisNode;
    }

    public void setMoveThatCreatedThisNode(Move moveThatCreatedThisNode) {
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
        int parentVisit = this.visitCount;

        List<Double> uctValues = new ArrayList<>();

        for (MonteCarloNode child : children) {
            double uctValue = child.UCTValue(parentVisit);
            uctValues.add(uctValue);
        }

        int maxIndex = uctValues.indexOf(Collections.max(uctValues));

        return children.get(maxIndex);
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
        for (MonteCarloNode child : children) {
            if ((child.getWinScore() / ((double) child.getVisitCount())) > (bestNode.getWinScore() / ((double) bestNode.getVisitCount()))) {
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
}
