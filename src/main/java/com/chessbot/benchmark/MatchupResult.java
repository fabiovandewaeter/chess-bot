package com.chessbot.benchmark;

import java.util.ArrayList;
import java.util.List;

public class MatchupResult {
    public final int botLevel;
    public final int stockfishLevel;
    private List<GameResult> gameResults;
    private int botWins = 0;
    private int stockfishWins = 0;
    private int draws = 0;

    public MatchupResult(int botLevel, int stockfishLevel) {
        this.botLevel = botLevel;
        this.stockfishLevel = stockfishLevel;
        this.gameResults = new ArrayList<>();
    }

    public void addGameResult(GameResult result, boolean botPlaysWhite) {
        gameResults.add(result);

        switch (result.getResult()) {
            case WHITE_WIN:
                if (botPlaysWhite)
                    botWins++;
                else
                    stockfishWins++;
                break;
            case BLACK_WIN:
                if (!botPlaysWhite)
                    botWins++;
                else
                    stockfishWins++;
                break;
            case DRAW:
                draws++;
                break;
        }
    }

    public double getWinRate() {
        int totalGames = gameResults.size();
        return totalGames > 0 ? (double) botWins / totalGames * 100 : 0;
    }

    public int getBotWins() {
        return botWins;
    }

    public int getStockfishWins() {
        return stockfishWins;
    }

    public int getDraws() {
        return draws;
    }

    public int getTotalGames() {
        return gameResults.size();
    }
}
