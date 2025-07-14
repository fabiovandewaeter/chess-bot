package com.chessbot;

import java.util.List;
import java.util.ArrayList;

public class Game {
    public Board board;
    public int colorToMove;
    private boolean botPlaysWhite;
    private MoveGenerator moveGenerator;

    public Game(boolean useUnicode, boolean botPlaysWhite) {
        board = new Board(useUnicode);
        colorToMove = Piece.WHITE;
        this.botPlaysWhite = botPlaysWhite;
        moveGenerator = new MoveGenerator();
    }

    public boolean makeMove(String moveString) {
        Move move = parseMove(moveString);
        if (move == null) {
            return false;
        }

        if (isLegalMove(move)) {
            executeMove(move);
            // colorToMove = (colorToMove == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
            colorToMove = Piece.getOpponentColor(colorToMove); // Correction
            return true;
        }

        return false;
    }

    private Move parseMove(String moveString) {
        if (moveString.length() != 4) {
            return null;
        }

        try {
            int fromFile = moveString.charAt(0) - 'a';
            int fromRank = moveString.charAt(1) - '1';
            int toFile = moveString.charAt(2) - 'a';
            int toRank = moveString.charAt(3) - '1';

            if (fromFile < 0 || fromFile > 7 || fromRank < 0 || fromRank > 7 ||
                    toFile < 0 || toFile > 7 || toRank < 0 || toRank > 7) {
                return null;
            }

            int fromSquare = fromRank * 8 + fromFile;
            int toSquare = toRank * 8 + toFile;

            return new Move(fromSquare, toSquare);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isLegalMove(Move move) {
        // Vérifier qu'il y a une pièce sur la case de départ
        int piece = board.squares[move.startingSquare];
        if (piece == Piece.NONE) {
            return false;
        }

        // Vérifier que c'est une pièce de la bonne couleur
        if (Piece.getColor(piece) != colorToMove) {
            return false;
        }

        // Générer tous les coups pseudo-légaux
        List<Move> pseudoLegalMoves = moveGenerator.generateMoves(this);

        // Vérifier si le coup est dans la liste des coups pseudo-légaux
        boolean isPseudoLegal = false;
        for (Move pseudoLegalMove : pseudoLegalMoves) {
            if (pseudoLegalMove.startingSquare == move.startingSquare &&
                    pseudoLegalMove.target == move.target) {
                isPseudoLegal = true;
                break;
            }
        }

        if (!isPseudoLegal) {
            return false;
        }

        // Vérifier que le coup ne laisse pas le roi en échec
        return !wouldLeaveKingInCheck(move);
    }

    public boolean wouldLeaveKingInCheck(Move move) {
        // Sauvegarde de l'état
        int originalPiece = board.squares[move.startingSquare];
        int capturedPiece = board.squares[move.target];
        int originalColor = colorToMove;

        // Exécute temporairement le coup
        board.squares[move.target] = originalPiece;
        board.squares[move.startingSquare] = Piece.NONE;
        colorToMove = Piece.getOpponentColor(originalColor); // Tour suivant

        // Vérifie si le roi (du joueur ayant joué le coup) est en échec
        boolean kingInCheck = isInCheck(originalColor); // Utiliser originalColor

        // Restauration
        board.squares[move.startingSquare] = originalPiece;
        board.squares[move.target] = capturedPiece;
        colorToMove = originalColor;

        return kingInCheck;
    }

    private void executeMove(Move move) {
        int piece = board.squares[move.startingSquare];
        board.squares[move.target] = piece;
        board.squares[move.startingSquare] = Piece.NONE;
    }

    public boolean isInCheck(int color) {
        // Trouver le roi
        int kingSquare = -1;
        for (int i = 0; i < 64; i++) {
            int piece = board.squares[i];
            if (Piece.getType(piece) == Piece.KING && Piece.getColor(piece) == color) {
                kingSquare = i;
                break;
            }
        }

        if (kingSquare == -1)
            return false;

        // Vérifier si le roi est attaqué
        return isSquareAttacked(kingSquare, Piece.getOpponentColor(color));
    }

    public boolean isSquareAttacked(int square, int attackerColor) {
        // Vérifier les attaques par les pions
        int pawnDirection = attackerColor == Piece.WHITE ? -1 : 1;
        int[] pawnAttacks = { 7, 9 };
        for (int attack : pawnAttacks) {
            int pawnSquare = square + pawnDirection * attack;
            if (pawnSquare >= 0 && pawnSquare < 64) {
                int piece = board.squares[pawnSquare];
                if (Piece.getType(piece) == Piece.PAWN && Piece.getColor(piece) == attackerColor) {
                    return true;
                }
            }
        }

        // Vérifier les attaques par les cavaliers
        int[] knightMoves = { 15, 17, -15, -17, 10, 6, -10, -6 };
        for (int move : knightMoves) {
            int knightSquare = square + move;
            if (knightSquare >= 0 && knightSquare < 64) {
                int piece = board.squares[knightSquare];
                if (Piece.getType(piece) == Piece.KNIGHT && Piece.getColor(piece) == attackerColor) {
                    return true;
                }
            }
        }

        // Vérifier les attaques en ligne droite
        int[] directions = { 8, -8, -1, 1, 7, -7, 9, -9 };
        for (int direction : directions) {
            for (int i = 1; i < 8; i++) {
                int targetSquare = square + direction * i;
                if (targetSquare < 0 || targetSquare >= 64)
                    break;

                int piece = board.squares[targetSquare];
                if (piece == Piece.NONE)
                    continue;

                if (Piece.getColor(piece) == attackerColor) {
                    int type = Piece.getType(piece);

                    // Vérifier les pièces attaquantes
                    if (i == 1 && type == Piece.KING)
                        return true;
                    if (type == Piece.QUEEN)
                        return true;
                    if (type == Piece.ROOK && (direction == 8 || direction == -8 || direction == -1 || direction == 1))
                        return true;
                    if (type == Piece.BISHOP
                            && (direction == 7 || direction == -7 || direction == 9 || direction == -9))
                        return true;
                }
                break; // Une pièce bloque le chemin
            }
        }

        return false;
    }

    public boolean isGameOver() {
        List<Move> pseudoLegalMoves = moveGenerator.generateMoves(this);
        List<Move> legalMoves = new ArrayList<>();
        for (Move move : pseudoLegalMoves) {
            if (!wouldLeaveKingInCheck(move)) {
                legalMoves.add(move);
            }
        }
        return legalMoves.isEmpty(); // Retirer tout System.out.println
    }
}
