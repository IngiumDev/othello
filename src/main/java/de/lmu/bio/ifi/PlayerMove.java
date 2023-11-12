package de.lmu.bio.ifi;

import szte.mi.Move;

public class PlayerMove extends Move {
    private final boolean playerOne;

    public PlayerMove(boolean playerOne, int x, int y) {
        super(x, y);
        this.playerOne = playerOne;
    }

    public boolean isPlayerOne() {
        return playerOne;
    }

    public boolean equals(PlayerMove o) {
        return this.x == o.x && this.y == o.y && this.playerOne == o.playerOne;
    }

}
