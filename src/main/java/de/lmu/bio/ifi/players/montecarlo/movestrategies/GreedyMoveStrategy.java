package de.lmu.bio.ifi.players.montecarlo.movestrategies;

import de.lmu.bio.ifi.OthelloGame;
import szte.mi.Move;

import java.util.List;
import java.util.Random;

public class GreedyMoveStrategy implements MoveStrategy {
    @Override
    public long getMove(OthelloGame game, boolean isPlayerOne, Random random) {
        long possibleMoves = game.getValidMoves(isPlayerOne);
        if (Long.bitCount(possibleMoves) <= 1) {
            return possibleMoves;
        } else {
            // Use Weight matrix to get the best move for the current player
            int bestScore = Integer.MIN_VALUE;
            // for each possible move
            long move = 0L;
            for (int i = 0; i < 64; i++) {
                long bit = 1L << i;
                long testMove = possibleMoves & bit;
                if (testMove != 0L) {
                    OthelloGame tempGameMove = game.copy();
                    int score = 0;
                    // Make the move
                    tempGameMove.forceMakeMove(isPlayerOne, testMove);
                    score = isPlayerOne ? tempGameMove.getPlayerOneChips() : tempGameMove.getPlayerTwoChips();
                    // Check if it is the best move
                    if (score > bestScore) {
                        bestScore = score;
                        move = testMove;
                    }
                }
            }
            return move;
        }
    }

    @Deprecated
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
