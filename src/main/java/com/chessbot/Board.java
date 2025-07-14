package com.chessbot;

public class Board {
    private static final String RESET = "\u001B[0m";
    private static final String BG_LIGHT = bg(240, 217, 181);
    private static final String BG_DARK = bg(181, 136, 99);
    private static final String FG_WHITE = fg(255, 255, 255);
    private static final String FG_BLACK = fg(0, 0, 0);
    private static final String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public int[] squares;
    private boolean useUnicode;

    public Board(boolean useUnicode) {
        this.useUnicode = useUnicode;
        squares = new int[64];
        loadPositionFromFEN(startFEN);
    }

    private static String fg(int r, int g, int b) {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }

    private static String bg(int r, int g, int b) {
        return "\u001B[48;2;" + r + ";" + g + ";" + b + "m";
    }

    public void print() {
        System.out.println();
        for (int row = 7; row >= 0; row--) {
            System.out.print(" " + (1 + row) + " ");
            for (int col = 0; col < 8; col++) {
                boolean light = (row + col) % 2 == 0;
                String bgColor = light ? BG_LIGHT : BG_DARK;
                int piece = squares[row * 8 + col];
                String pieceSymbol = useUnicode ? Piece.pieceToUnicodeSymbol(piece) : Piece.pieceToLetterSymbol(piece);
                String fgColor = FG_BLACK;

                if (piece != Piece.NONE) {
                    if (Piece.isWhite(piece)) {
                        fgColor = FG_WHITE;
                        if (!useUnicode) {
                            pieceSymbol = pieceSymbol.toUpperCase();
                        }
                    }
                }

                System.out.print(bgColor + fgColor + " " + pieceSymbol + RESET);
            }
            System.out.println();
        }
        System.out.print("   ");
        for (char f = 'a'; f <= 'h'; f++) {
            System.out.print(" " + f);
        }
        System.out.println();
    }

    public void loadPositionFromFEN(String fen) {
        String fenBoard = fen.split(" ")[0];
        int file = 0, rank = 7;

        for (int i = 0; i < fenBoard.length(); i++) {
            char symbol = fenBoard.charAt(i);
            if (symbol == '/') {
                file = 0;
                rank--;
            } else {
                if (Character.isDigit(symbol)) {
                    file += Character.getNumericValue(symbol);
                } else {
                    int pieceColor = Character.isUpperCase(symbol) ? Piece.WHITE : Piece.BLACK;
                    int pieceType = Piece.symbolToPiece(Character.toLowerCase(symbol));
                    squares[rank * 8 + file] = pieceType | pieceColor;
                    file++;
                }
            }
        }
    }
}
