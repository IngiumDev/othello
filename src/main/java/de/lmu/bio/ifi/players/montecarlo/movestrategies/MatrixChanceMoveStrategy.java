package de.lmu.bio.ifi.players.montecarlo.movestrategies;

import de.lmu.bio.ifi.OthelloGame;

import java.util.Random;

import static de.lmu.bio.ifi.OthelloGame.forceTempMakeMove;
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
            double bestScore = Double.NEGATIVE_INFINITY;
            long bestMove = 0L;
            long moves = possibleMoves;
            long playerBoard = isPlayerOne ? game.getPlayerOneBoard() : game.getPlayerTwoBoard();
            long opponentBoard = isPlayerOne ? game.getPlayerTwoBoard() : game.getPlayerOneBoard();
            double W = getWeightedPieceCount(playerBoard, opponentBoard);

            while (moves != 0) {
                long testMove = Long.lowestOneBit(moves); // get the lowest set bit
                moves ^= testMove; // unset the lowest bit
                if (testMove != 0L) {
                    long[] discs = forceTempMakeMove(isPlayerOne, playerBoard, opponentBoard, testMove);
                    long tempPlayerBoard = discs[0];
                    long tempOpponentBoard = discs[1];
                    double Wi = getWeightedPieceCount(tempPlayerBoard, tempOpponentBoard);
                    double Di = Wi - W;
                    if (Di > bestScore) {
                        bestScore = Di;
                        bestMove = testMove;
                    }
                }
            }
            return bestMove;
        }
    }


}
