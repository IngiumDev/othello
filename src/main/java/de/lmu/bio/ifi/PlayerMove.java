package de.lmu.bio.ifi;

import szte.mi.Move;

import java.util.Objects;

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

    @Override
    public String toString() {
        return "(" + (x + 1) + ", " + (y + 1) + ", " + (playerOne ? "Black" : "White") + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerOne);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerMove that = (PlayerMove) o;
        return playerOne == that.playerOne && x == that.x && y == that.y;
    }

    public long toLong() {
        return 1L << (x + y * 8);
    }

}
