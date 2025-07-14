package com.chessbot.benchmark;

public class GameResult {
    public enum Result {
        WHITE_WIN, BLACK_WIN, DRAW
    }

    private Result result;
    private int movesPlayed;
    private String reason;

    public GameResult(Result result, int movesPlayed, String reason) {
        this.result = result;
        this.movesPlayed = movesPlayed;
        this.reason = reason;
    }

    public Result getResult() {
        return result;
    }

    public int getMovesPlayed() {
        return movesPlayed;
    }

    public String getReason() {
        return reason;
    }
}
