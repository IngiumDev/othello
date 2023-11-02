package de.lmu.bio.ifi;

import szte.mi.Move;

public class PlayerMove extends Move {
    private boolean playerOne;
    public PlayerMove(boolean playerOne, int x, int y) {
        super(x, y);
        this.playerOne = playerOne;
    }

    public boolean isPlayerOne() {
        return playerOne;
    }
}
