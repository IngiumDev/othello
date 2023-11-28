package de.lmu.bio.ifi.players.montecarlo.movestrategies;

import de.lmu.bio.ifi.OthelloGame;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static de.lmu.bio.ifi.players.montecarlo.movestrategies.MatrixEvaluater.getWeightedPieceCount;

public class MatrixChanceMoveStrategy implements MoveStrategy {
    @Override
    public long getMove(OthelloGame game, boolean isPlayerOne, Random random) {
        long possibleMoves = game.getValidMoves(isPlayerOne);
        if (Long.bitCount(possibleMoves) <= 1) {
            return possibleMoves;
        } else {
            int myPlayerDisc = isPlayerOne ? OthelloGame.PLAYER_ONE : OthelloGame.PLAYER_TWO;
            int opponentDisc = isPlayerOne ? OthelloGame.PLAYER_TWO : OthelloGame.PLAYER_ONE;
            // Use Weight matrix to get the best move for the current player
            double bestScore = Double.NEGATIVE_INFINITY;
            long bestMove = 0L;
            long moves = possibleMoves;
            double totalN = 0.0;
            Map<Long, Double> moveScores = new HashMap<>();
            long playerBoard = isPlayerOne ? game.getPlayerOneBoard() : game.getPlayerTwoBoard();
            long opponentBoard = isPlayerOne ? game.getPlayerTwoBoard() : game.getPlayerOneBoard();
            double W = getWeightedPieceCount(myPlayerDisc, opponentDisc, playerBoard, opponentBoard);

            while (moves != 0) {
                long testMove = Long.lowestOneBit(moves); // get the lowest set bit
                moves ^= testMove; // unset the lowest bit
                if (testMove != 0L) {
                    OthelloGame tempGameMove = game.copy();
                    tempGameMove.forceMakeMove(isPlayerOne, testMove);
                    long tempPlayerBoard = isPlayerOne ? tempGameMove.getPlayerOneBoard() : tempGameMove.getPlayerTwoBoard();
                    long tempOpponentBoard = isPlayerOne ? tempGameMove.getPlayerTwoBoard() : tempGameMove.getPlayerOneBoard();
                    double Wi = getWeightedPieceCount(myPlayerDisc, opponentDisc, tempPlayerBoard, tempOpponentBoard);
                    double Di = Wi - W;
                    moveScores.put(testMove, Di);
                    if (Di > bestScore) {
                        bestScore = Di;
                        bestMove = testMove;
                    }
                }
            }
            double min = Collections.min(moveScores.values());
            for (double score : moveScores.values()) {
                double Ni = score - min + 1;
                totalN += Ni;
            }
            double p = random.nextDouble();
            double cumulativeProbability = 0.0;
            for (Map.Entry<Long, Double> entry : moveScores.entrySet()) {
                long move = entry.getKey();
                double score = entry.getValue();
                double Ni = score - min + 1;
                double pi = Ni / totalN;
                cumulativeProbability += pi;
                if (p <= cumulativeProbability) {
                    return move;
                }
            }
            return bestMove;
        }
    }


}
