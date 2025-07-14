package com.chessbot.benchmark;

import com.chessbot.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimpleBot {
    private Random random;
    private int difficulty; // 1-5

    public SimpleBot(int difficulty) {
        this.difficulty = difficulty;
        this.random = new Random();
    }

    public String getBestMove(Game game) {
        MoveGenerator moveGenerator = new MoveGenerator();
        List<Move> moves = moveGenerator.generateMoves(game);

        // Filtrer les coups légaux
        List<Move> legalMoves = new java.util.ArrayList<>();
        for (Move move : moves) {
            if (!wouldLeaveKingInCheck(game, move)) {
                legalMoves.add(move);
            }
        }

        if (legalMoves.isEmpty()) {
            return null;
        }

        Move bestMove = null;

        switch (difficulty) {
            case 1: // Complètement aléatoire
                bestMove = legalMoves.get(random.nextInt(legalMoves.size()));
                break;

            case 2: // Préfère les captures
                bestMove = preferCaptures(game, legalMoves);
                break;

            case 3: // Évite de perdre des pièces
                bestMove = avoidLossingPieces(game, legalMoves);
                break;

            case 4: // Évaluation simple
                bestMove = simpleEvaluation(game, legalMoves);
                break;

            case 5: // Évaluation avec profondeur 2
                bestMove = deeperEvaluation(game, legalMoves);
                break;

            default:
                bestMove = legalMoves.get(random.nextInt(legalMoves.size()));
        }

        return bestMove.toString();
    }

    private Move preferCaptures(Game game, List<Move> moves) {
        // Chercher les captures
        for (Move move : moves) {
            if (game.board.squares[move.target] != Piece.NONE) {
                return move;
            }
        }
        return moves.get(random.nextInt(moves.size()));
    }

    private Move avoidLossingPieces(Game game, List<Move> moves) {
        // Implémentation simple : éviter les cases attaquées
        for (Move move : moves) {
            if (!isSquareAttacked(game, move.target, game.colorToMove == Piece.WHITE ? Piece.BLACK : Piece.WHITE)) {
                return move;
            }
        }
        return moves.get(random.nextInt(moves.size()));
    }

    private Move simpleEvaluation(Game game, List<Move> moves) {
        Move bestMove = moves.get(0);
        int bestScore = Integer.MIN_VALUE;

        for (Move move : moves) {
            int score = evaluateMove(game, move);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private Move deeperEvaluation(Game game, List<Move> moves) {
        Move bestMove = moves.get(0);
        int bestScore = Integer.MIN_VALUE;

        for (Move move : moves) {
            int score = minimax(game, move, 2, false);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private int evaluateMove(Game game, Move move) {
        int score = 0;

        // Points pour les captures
        int capturedPiece = game.board.squares[move.target];
        if (capturedPiece != Piece.NONE) {
            score += getPieceValue(capturedPiece);
        }

        // Points pour le contrôle du centre
        int file = move.target % 8;
        int rank = move.target / 8;
        if (file >= 2 && file <= 5 && rank >= 2 && rank <= 5) {
            score += 10;
        }

        return score;
    }

    private int minimax(Game game, Move move, int depth, boolean isMaximizing) {
        // Sauvegarde de l'état
        int[] originalBoard = game.board.squares.clone();
        int originalColor = game.colorToMove;

        executeMove(game, move);
        int score = 0;

        // Vérifier si la partie est terminée ou profondeur atteinte
        if (depth == 0 || game.isGameOver()) {
            score = evaluatePosition(game);
        } else {
            // Générer les coups suivants
            List<Move> nextMoves = new MoveGenerator().generateMoves(game);
            List<Move> legalMoves = new ArrayList<>();
            for (Move m : nextMoves) {
                if (!wouldLeaveKingInCheck(game, m)) {
                    legalMoves.add(m);
                }
            }

            if (legalMoves.isEmpty()) {
                score = evaluatePosition(game);
            } else {
                score = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

                for (Move nextMove : legalMoves) {
                    int currentScore = minimax(game, nextMove, depth - 1, !isMaximizing);

                    if (isMaximizing) {
                        score = Math.max(score, currentScore);
                    } else {
                        score = Math.min(score, currentScore);
                    }
                }
            }
        }

        // Restauration de l'état
        game.board.squares = originalBoard;
        game.colorToMove = originalColor; // CORRECTION CRITIQUE

        return score;
    }

    private int evaluatePosition(Game game) {
        int score = 0;
        int perspective = (game.colorToMove == Piece.WHITE) ? 1 : -1; // CORRECTION IMPORTANTE

        for (int i = 0; i < 64; i++) {
            int piece = game.board.squares[i];
            if (piece != Piece.NONE) {
                int value = getPieceValue(piece);
                if (Piece.isWhite(piece)) {
                    score += value;
                } else {
                    score -= value;
                }
            }
        }

        return score * perspective; // CORRECTION : tenir compte du camp
    }

    private int getPieceValue(int piece) {
        switch (Piece.getType(piece)) {
            case Piece.PAWN:
                return 100;
            case Piece.KNIGHT:
                return 300;
            case Piece.BISHOP:
                return 300;
            case Piece.ROOK:
                return 500;
            case Piece.QUEEN:
                return 900;
            case Piece.KING:
                return 10000;
            default:
                return 0;
        }
    }

    private boolean wouldLeaveKingInCheck(Game game, Move move) {
        // Sauvegarder l'état avant le coup
        int[] originalBoard = game.board.squares.clone();
        int originalColor = game.colorToMove;

        // Exécuter temporairement le coup
        game.board.squares[move.target] = game.board.squares[move.startingSquare];
        game.board.squares[move.startingSquare] = Piece.NONE;
        game.colorToMove = Piece.getOpponentColor(originalColor);

        // Trouver la position du roi
        int kingSquare = -1;
        for (int i = 0; i < 64; i++) {
            int piece = game.board.squares[i];
            if (Piece.getType(piece) == Piece.KING && Piece.getColor(piece) == originalColor) {
                kingSquare = i;
                break;
            }
        }

        // Vérifier si le roi est attaqué
        boolean kingInCheck = false;
        if (kingSquare != -1) {
            kingInCheck = game.isSquareAttacked(kingSquare, Piece.getOpponentColor(originalColor));
        }

        // Restaurer l'état original
        game.board.squares = originalBoard;
        game.colorToMove = originalColor;

        return kingInCheck;
    }

    private boolean isInCheck(Game game, int color) {
        // Implémentation simplifiée
        int kingSquare = -1;
        for (int i = 0; i < 64; i++) {
            int piece = game.board.squares[i];
            if (Piece.getType(piece) == Piece.KING && Piece.getColor(piece) == color) {
                kingSquare = i;
                break;
            }
        }

        if (kingSquare == -1)
            return false;

        int opponentColor = (color == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        return isSquareAttacked(game, kingSquare, opponentColor);
    }

    private void executeMove(Game game, Move move) {
        game.board.squares[move.target] = game.board.squares[move.startingSquare];
        game.board.squares[move.startingSquare] = Piece.NONE;
        game.colorToMove = Piece.getOpponentColor(game.colorToMove);
    }

    private boolean isSquareAttacked(Game game, int square, int attackerColor) {
        return new MoveGenerator().isSquareAttackedDirectly(game.board, square, attackerColor);
    }

    private void undoMove(Game game, Move move) {
        // Implémentation simplifiée - nécessiterait de sauvegarder l'état
    }
}
