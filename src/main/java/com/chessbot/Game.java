package com.chessbot;

import java.util.List;
import java.util.ArrayList;

public class Game {
    public Board board;
    public int colorToMove;
    private MoveGenerator moveGenerator;

    public Game(boolean useUnicode) {
        board = new Board(useUnicode);
        colorToMove = Piece.WHITE;
        moveGenerator = new MoveGenerator();
    }

    public boolean makeMove(String moveString) {
        Move move = parseMove(moveString);
        if (move == null) {
            return false;
        }

        if (isLegalMove(move)) {
            executeMove(move);
            colorToMove = (colorToMove == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
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

    private boolean wouldLeaveKingInCheck(Move move) {
        // Sauvegarder l'état actuel
        int originalPiece = board.squares[move.startingSquare];
        int capturedPiece = board.squares[move.target];

        // Exécuter temporairement le coup
        board.squares[move.target] = originalPiece;
        board.squares[move.startingSquare] = Piece.NONE;

        // Vérifier si le roi est en échec
        boolean kingInCheck = isInCheck(colorToMove);

        // Restaurer l'état original
        board.squares[move.startingSquare] = originalPiece;
        board.squares[move.target] = capturedPiece;

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
        int opponentColor = (color == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        return isSquareAttacked(kingSquare, opponentColor);
    }

    private boolean isSquareAttacked(int square, int attackerColor) {
        // Temporairement changer le joueur pour générer les coups de l'adversaire
        int originalColor = colorToMove;
        colorToMove = attackerColor;

        List<Move> opponentMoves = moveGenerator.generateMoves(this);

        colorToMove = originalColor;

        for (Move move : opponentMoves) {
            if (move.target == square) {
                return true;
            }
        }

        return false;
    }

    public boolean isGameOver() {
        List<Move> pseudoLegalMoves = moveGenerator.generateMoves(this);

        // Filtrer pour ne garder que les coups légaux (qui ne laissent pas le roi en
        // échec)
        List<Move> legalMoves = new ArrayList<>();
        for (Move move : pseudoLegalMoves) {
            if (!wouldLeaveKingInCheck(move)) {
                legalMoves.add(move);
            }
        }

        if (legalMoves.isEmpty()) {
            if (isInCheck(colorToMove)) {
                String player = (colorToMove == Piece.WHITE) ? "Noirs" : "Blancs";
                System.out.println("ÉCHEC ET MAT ! Les " + player + " gagnent !");
            } else {
                System.out.println("PAT ! Match nul !");
            }
            return true;
        }

        return false;
    }
}
