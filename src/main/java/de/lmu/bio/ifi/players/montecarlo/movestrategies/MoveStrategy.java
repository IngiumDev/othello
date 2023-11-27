package de.lmu.bio.ifi.players.montecarlo.movestrategies;

import de.lmu.bio.ifi.OthelloGame;

import java.util.Random;

public interface MoveStrategy {
    long getMove(OthelloGame game, boolean isPlayerOne, Random random);
}