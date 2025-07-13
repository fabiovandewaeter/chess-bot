package com.chessbot;

public class Board {
    public static int[] square;

    public Board() {
        square = new int[64];

        square[0] = Piece.WHITE | Piece.BISHOP;
        square[63] = Piece.BLACK | Piece.QUEEN;
    }

    // Helper : génère le code ANSI true-color pour le foreground
    private static String fg(int r, int g, int b) {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }

    // Helper : génère le code ANSI true-color pour le background
    private static String bg(int r, int g, int b) {
        return "\u001B[48;2;" + r + ";" + g + ";" + b + "m";
    }

    private static final String RESET = "\u001B[0m";

    private static String BG_LIGHT = bg(240, 217, 181);
    private static String BG_DARK = bg(181, 136, 99);
    private static String FG_WHITE = fg(255, 255, 255);
    private static String FG_BLACK = fg(0, 0, 0);

    public void print() {
        System.out.println();
        for (int row = 0; row < 8; row++) {
            System.out.print(" " + (8 - row) + " ");
            for (int col = 0; col < 8; col++) {
                boolean light = (row + col) % 2 == 0;
                String bgColor = light ? BG_LIGHT : BG_DARK;

                int piece = square[row + col * 8];
                String pieceIcon = Piece.pieceToIcon(piece);

                String fgColor = Piece.isWhite(piece) ? FG_WHITE : FG_BLACK;
                System.out.print(bgColor + fgColor + " " + pieceIcon + RESET);
            }
            System.out.println();
        }
        // fichiers en bas
        System.out.print("  ");
        for (char f = 'a'; f <= 'h'; f++) {
            System.out.print(" " + f);
        }
        System.out.println("\n");
    }
}
