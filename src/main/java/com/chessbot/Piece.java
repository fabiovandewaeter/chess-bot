package com.chessbot;

public final class Piece {
    public static final int NONE = 0;
    public static final int KING = 1;
    public static final int PAWN = 2;
    public static final int KNIGHT = 3;
    public static final int BISHOP = 4;
    public static final int ROOK = 5;
    public static final int QUEEN = 6;

    public static final int WHITE = 8;
    public static final int BLACK = 16;

    public static String pieceToUnicodeSymbol(int piece) {
        int type = piece & 0b111;
        switch (type) {
            case NONE:
                return " ";
            case KING:
                return "♔";
            case PAWN:
                return "♙";
            case KNIGHT:
                return "♘";
            case BISHOP:
                return "♗";
            case ROOK:
                return "♖";
            case QUEEN:
                return "♕";
            default:
                return " ";
        }
    }

    public static String pieceToLetterSymbol(int piece) {
        int type = piece & 0b111;
        switch (type) {
            case NONE:
                return " ";
            case KING:
                return "k";
            case PAWN:
                return "p";
            case KNIGHT:
                return "n";
            case BISHOP:
                return "b";
            case ROOK:
                return "r";
            case QUEEN:
                return "q";
            default:
                return " ";
        }
    }

    public static int symbolToPiece(char symbol) {
        switch (symbol) {
            case 'k':
                return KING;
            case 'p':
                return PAWN;
            case 'n':
                return KNIGHT;
            case 'b':
                return BISHOP;
            case 'r':
                return ROOK;
            case 'q':
                return QUEEN;
            default:
                return NONE;
        }
    }

    public static boolean isWhite(int piece) {
        return (piece & WHITE) != 0;
    }

    public static int getColor(int piece) {
        return piece & 0b11000;
    }

    public static int getOpponentColor(int piece) {
        return Piece.isWhite(piece) ? Piece.BLACK : Piece.WHITE;
    }

    public static int getType(int piece) {
        return piece & 0b111;
    }

    public static boolean isType(int piece, int type) {
        return getType(piece) == type;
    }

    public static boolean isSlidingPiece(int piece) {
        int type = getType(piece);
        return type == BISHOP || type == QUEEN || type == ROOK;
    }
}
