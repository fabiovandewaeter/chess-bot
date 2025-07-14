package com.chessbot.benchmark;

import com.chessbot.*;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class BenchmarkRunner {
    private String stockfishPath;
    private ExecutorService executor;

    public BenchmarkRunner(String stockfishPath, int threads) {
        this.stockfishPath = stockfishPath;
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public BenchmarkResults runBenchmark(int[] botLevels, int[] stockfishLevels, int gamesPerMatchup) {
        BenchmarkResults results = new BenchmarkResults();
        List<Future<MatchupResult>> futures = new ArrayList<>();

        for (int botLevel : botLevels) {
            for (int stockfishLevel : stockfishLevels) {
                Future<MatchupResult> future = executor
                        .submit(() -> runMatchup(botLevel, stockfishLevel, gamesPerMatchup));
                futures.add(future);
            }
        }

        for (Future<MatchupResult> future : futures) {
            try {
                MatchupResult matchupResult = future.get();
                results.addMatchupResult(matchupResult);
                System.out.println("Terminé: Bot " + matchupResult.botLevel +
                        " vs Stockfish " + matchupResult.stockfishLevel +
                        " - Taux de victoire: " + matchupResult.getWinRate() + "%");
            } catch (Exception e) {
                System.err.println("Erreur dans le benchmark: " + e.getMessage());
            }
        }

        return results;
    }

    private MatchupResult runMatchup(int botLevel, int stockfishLevel, int games) {
        MatchupResult result = new MatchupResult(botLevel, stockfishLevel);

        for (int i = 0; i < games; i++) {
            boolean botPlaysWhite = (i % 2 == 0);
            GameResult gameResult = runSingleGame(botLevel, stockfishLevel, botPlaysWhite);
            result.addGameResult(gameResult, botPlaysWhite);
        }

        return result;
    }

    private GameResult runSingleGame(int botLevel, int stockfishLevel, boolean botPlaysWhite) {
        Game game = new Game(false, botPlaysWhite); // Mode lettres pour simplicité
        SimpleBot bot = new SimpleBot(botLevel);
        UCIEngine stockfish = new UCIEngine(stockfishPath);

        if (!stockfish.start()) {
            return new GameResult(GameResult.Result.DRAW, 0, "Erreur moteur");
        }

        stockfish.setSkillLevel(stockfishLevel);
        stockfish.setTimeLimit(5000); // 5 secondes pour Stockfish niveau 10

        int moves = 0;
        final int MAX_MOVES = 200; // Éviter les parties infinies

        System.out.println("Début de la partie: Bot " + botLevel + " vs Stockfish " + stockfishLevel);
        System.out.println("Le bot joue les " + (botPlaysWhite ? "blancs" : "noirs"));

        try {
            while (!game.isGameOver() && moves < MAX_MOVES) {
                System.out.println("\nCoup #" + moves);
                System.out.println("Au tour des " + (game.colorToMove == Piece.WHITE ? "blancs" : "noirs"));
                String move;

                if ((game.colorToMove == Piece.WHITE && botPlaysWhite) ||
                        (game.colorToMove == Piece.BLACK && !botPlaysWhite)) {
                    // Tour du bot
                    System.out.println("Bot (" + botLevel + ") réfléchit...");
                    move = bot.getBestMove(game);
                    System.out.println("Bot joue: " + move);
                } else {
                    // Tour de Stockfish
                    String fen = gameToFEN(game);
                    System.out.println("Position envoyée à Stockfish:\n" + fen);
                    move = stockfish.getBestMove(fen);
                    System.out.println("Stockfish joue: " + move);
                }

                if (move == null || !game.makeMove(move)) {
                    System.out.println("Coup invalide ou null: " + move);
                    break;
                }

                game.board.print();
                moves++;
            }

            if (game.isGameOver()) {
                if (game.isInCheck(game.colorToMove)) {
                    if (game.colorToMove == Piece.WHITE) {
                        String winner = "Noirs";
                        System.out.println("ÉCHEC ET MAT ! Les " + winner + " gagnent !");
                        return new GameResult(GameResult.Result.BLACK_WIN, moves, "check mat");
                    } else {
                        String winner = "Blancs";
                        System.out.println("ÉCHEC ET MAT ! Les " + winner + " gagnent !");
                        return new GameResult(GameResult.Result.WHITE_WIN, moves, "check mat");
                    }
                } else {
                    System.out.println("PAT ! Match nul !");
                    return new GameResult(GameResult.Result.DRAW, moves, "pat");
                }
            } else if (moves >= MAX_MOVES) {
                System.out.println("Limite de coups atteinte");
                return new GameResult(GameResult.Result.DRAW, moves, "Limite de coups atteinte");
            }

            // Déterminer le résultat
            if (game.isInCheck(game.colorToMove)) {
                // Échec et mat
                boolean whiteWins = (game.colorToMove == Piece.BLACK);
                return new GameResult(
                        whiteWins ? GameResult.Result.WHITE_WIN : GameResult.Result.BLACK_WIN,
                        moves, "Échec et mat");
            } else {
                // Pat
                return new GameResult(GameResult.Result.DRAW, moves, "Pat");
            }

        } finally {
            stockfish.close();
        }
    }

    private String gameToFEN(Game game) {
        // Conversion simplifiée en FEN
        StringBuilder fen = new StringBuilder();

        // Position des pièces
        for (int rank = 7; rank >= 0; rank--) {
            int emptySquares = 0;
            for (int file = 0; file < 8; file++) {
                int piece = game.board.squares[rank * 8 + file];
                if (piece == Piece.NONE) {
                    emptySquares++;
                } else {
                    if (emptySquares > 0) {
                        fen.append(emptySquares);
                        emptySquares = 0;
                    }
                    char symbol = pieceToFENChar(piece);
                    fen.append(symbol);
                }
            }
            if (emptySquares > 0) {
                fen.append(emptySquares);
            }
            if (rank > 0) {
                fen.append('/');
            }
        }

        // Joueur actuel
        fen.append(game.colorToMove == Piece.WHITE ? " w " : " b ");

        // Roques (simplifié mais correct)
        boolean wKingside = canCastle(game, Piece.WHITE, true);
        boolean wQueenside = canCastle(game, Piece.WHITE, false);
        boolean bKingside = canCastle(game, Piece.BLACK, true);
        boolean bQueenside = canCastle(game, Piece.BLACK, false);

        if (wKingside || wQueenside || bKingside || bQueenside) {
            if (wKingside)
                fen.append('K');
            if (wQueenside)
                fen.append('Q');
            if (bKingside)
                fen.append('k');
            if (bQueenside)
                fen.append('q');
        } else {
            fen.append('-');
        }
        fen.append(' ');

        // En passant
        fen.append("- "); // Simplifié mais acceptable pour le benchmark

        // Compteurs
        fen.append("0 1"); // Demi-coups et nombre de coups

        return fen.toString();
    }

    private boolean canCastle(Game game, int color, boolean kingside) {
        int king = Piece.KING | color;
        int rook = Piece.ROOK | color;
        int backRank = color == Piece.WHITE ? 0 : 7;
        int kingSquare = backRank * 8 + 4;
        int rookSquare = kingside ? backRank * 8 + 7 : backRank * 8;

        return game.board.squares[kingSquare] == king &&
                game.board.squares[rookSquare] == rook;
    }

    private char pieceToFENChar(int piece) {
        char c = ' ';
        switch (Piece.getType(piece)) {
            case Piece.KING:
                c = 'k';
                break;
            case Piece.QUEEN:
                c = 'q';
                break;
            case Piece.ROOK:
                c = 'r';
                break;
            case Piece.BISHOP:
                c = 'b';
                break;
            case Piece.KNIGHT:
                c = 'n';
                break;
            case Piece.PAWN:
                c = 'p';
                break;
        }
        return Piece.isWhite(piece) ? Character.toUpperCase(c) : c;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
