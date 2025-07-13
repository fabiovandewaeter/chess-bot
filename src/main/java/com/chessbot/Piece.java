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

    public static String pieceToIcon(int piece) {
        int type = piece & 0b111;
        switch (type) {
            case 0:
                return " ";
            case 1:
                return "K";
            case 2:
                return "P";
            case 3:
                return "N";
            case 4:
                return "B";
            case 5:
                return "R";
            case 6:
                return "Q";
        }
        return " ";
    }

    public static boolean isWhite(int piece) {
        int color = piece >> 3;
        return (color & 0b11) == 0b1;
    }
}
