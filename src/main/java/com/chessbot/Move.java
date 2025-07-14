package com.chessbot;

public class Move {
    public final int startingSquare;
    public final int target;

    public Move(int startingSquare, int target) {
        this.startingSquare = startingSquare;
        this.target = target;
    }

    @Override
    public String toString() {
        return squareToString(startingSquare) + squareToString(target);
    }

    private String squareToString(int square) {
        int file = square % 8;
        int rank = square / 8;
        return "" + (char) ('a' + file) + (rank + 1);
    }
}
