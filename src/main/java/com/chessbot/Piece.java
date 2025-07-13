package com.chessbot;

public final class Piece {
    // Piece Types
    public static final int NONE = 0;
    public static final int KING = 1;
    public static final int PAWN = 2;
    public static final int KNIGHT = 3;
    public static final int BISHOP = 4;
    public static final int ROOK = 5;
    public static final int QUEEN = 6;

    // Piece Colours
    public static final int WHITE = 8;
    public static final int BLACK = 16;

    public static String pieceToSymbol(int piece) {
        int type = piece & 0b111;
        switch (type) {
            case 0:
                return " ";
            case 1:
                return "k";
            case 2:
                return "p";
            case 3:
                return "n";
            case 4:
                return "b";
            case 5:
                return "r";
            case 6:
                return "q";
        }
        return " ";
    }

    public static int symbolToPiece(char symbol) {
        switch (symbol) {
            case 'k':
                return 1;
            case 'p':
                return 2;
            case 'n':
                return 3;
            case 'b':
                return 4;
            case 'r':
                return 5;
            case 'q':
                return 6;
        }
        return 0;
    }

    public static boolean isWhite(int piece) {
        int color = piece >> 3;
        return (color & 0b11) == 0b1;
    }
}
