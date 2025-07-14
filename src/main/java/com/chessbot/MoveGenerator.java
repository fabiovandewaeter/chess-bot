package com.chessbot;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    private static final int[] directionOffsets = { 8, -8, -1, 1, 7, -7, 9, -9 };
    private static int[][] numSquaresToEdge;

    static {
        precomputeMoveData();
    }

    private static void precomputeMoveData() {
        numSquaresToEdge = new int[64][8];

        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                int numNorth = 7 - rank;
                int numSouth = rank;
                int numWest = file;
                int numEast = 7 - file;

                int squareIndex = rank * 8 + file;

                numSquaresToEdge[squareIndex] = new int[] {
                        numNorth, numSouth, numWest, numEast,
                        Math.min(numNorth, numWest), Math.min(numSouth, numEast),
                        Math.min(numNorth, numEast), Math.min(numSouth, numWest)
                };
            }
        }
    }

    public List<Move> generateMoves(Game game) {
        List<Move> moves = new ArrayList<>();

        for (int startSquare = 0; startSquare < 64; startSquare++) {
            int piece = game.board.squares[startSquare];
            if (Piece.getColor(piece) == game.colorToMove) {
                int pieceType = Piece.getType(piece);

                switch (pieceType) {
                    case Piece.PAWN:
                        generatePawnMoves(startSquare, piece, game, moves);
                        break;
                    case Piece.KNIGHT:
                        generateKnightMoves(startSquare, piece, game, moves);
                        break;
                    case Piece.KING:
                        generateKingMoves(startSquare, piece, game, moves);
                        break;
                    case Piece.BISHOP:
                    case Piece.ROOK:
                    case Piece.QUEEN:
                        generateSlidingMoves(startSquare, piece, game, moves);
                        break;
                }
            }
        }

        return moves;
    }

    private void generatePawnMoves(int startSquare, int piece, Game game, List<Move> moves) {
        int direction = Piece.isWhite(piece) ? 1 : -1;
        int startRank = Piece.isWhite(piece) ? 1 : 6;

        int file = startSquare % 8;
        int rank = startSquare / 8;

        // Mouvement vers l'avant
        int oneStep = startSquare + direction * 8;
        if (oneStep >= 0 && oneStep < 64 && game.board.squares[oneStep] == Piece.NONE) {
            moves.add(new Move(startSquare, oneStep));

            // Mouvement de deux cases depuis la position initiale
            if (rank == startRank) {
                int twoStep = startSquare + direction * 16;
                if (twoStep >= 0 && twoStep < 64 && game.board.squares[twoStep] == Piece.NONE) {
                    moves.add(new Move(startSquare, twoStep));
                }
            }
        }

        // Captures en diagonal
        int[] captureDirs = { direction * 7, direction * 9 };
        for (int captureDir : captureDirs) {
            int captureSquare = startSquare + captureDir;
            if (captureSquare >= 0 && captureSquare < 64) {
                int captureFile = captureSquare % 8;
                // Vérifier qu'on ne wrappe pas autour du plateau
                if (Math.abs(captureFile - file) == 1) {
                    int capturedPiece = game.board.squares[captureSquare];
                    if (capturedPiece != Piece.NONE &&
                            Piece.getColor(capturedPiece) != Piece.getColor(piece)) {
                        moves.add(new Move(startSquare, captureSquare));
                    }
                }
            }
        }
    }

    private void generateKnightMoves(int startSquare, int piece, Game game, List<Move> moves) {
        int[] knightMoves = { 15, 17, -15, -17, 10, 6, -10, -6 };
        int file = startSquare % 8;
        int rank = startSquare / 8;

        for (int moveOffset : knightMoves) {
            int targetSquare = startSquare + moveOffset;
            if (targetSquare >= 0 && targetSquare < 64) {
                int targetFile = targetSquare % 8;
                int targetRank = targetSquare / 8;

                // Vérifier que le mouvement est valide (pas de wrap-around)
                if (Math.abs(targetFile - file) <= 2 && Math.abs(targetRank - rank) <= 2) {
                    int targetPiece = game.board.squares[targetSquare];
                    if (targetPiece == Piece.NONE ||
                            Piece.getColor(targetPiece) != Piece.getColor(piece)) {
                        moves.add(new Move(startSquare, targetSquare));
                    }
                }
            }
        }
    }

    private void generateKingMoves(int startSquare, int piece, Game game, List<Move> moves) {
        for (int directionIndex = 0; directionIndex < 8; directionIndex++) {
            int targetSquare = startSquare + directionOffsets[directionIndex];
            if (targetSquare >= 0 && targetSquare < 64) {
                int file = startSquare % 8;
                int targetFile = targetSquare % 8;

                // Vérifier qu'on ne wrappe pas autour du plateau
                if (Math.abs(targetFile - file) <= 1) {
                    int targetPiece = game.board.squares[targetSquare];
                    if (targetPiece == Piece.NONE ||
                            Piece.getColor(targetPiece) != Piece.getColor(piece)) {
                        moves.add(new Move(startSquare, targetSquare));
                    }
                }
            }
        }

        // Roque
        if (!game.isInCheck(Piece.getColor(piece))) {
            generateCastlingMoves(startSquare, piece, game, moves);
        }
    }

    private void generateCastlingMoves(int startSquare, int piece, Game game, List<Move> moves) {
        int color = Piece.getColor(piece);
        int backRank = color == Piece.WHITE ? 0 : 7;

        // Kingside
        if (canCastle(game, color, true)) {
            moves.add(new Move(startSquare, backRank * 8 + 6));
        }

        // Queenside
        if (canCastle(game, color, false)) {
            moves.add(new Move(startSquare, backRank * 8 + 2));
        }
    }

    private boolean canCastle(Game game, int color, boolean kingside) {
        int king = Piece.KING | color;
        int rook = Piece.ROOK | color;
        int backRank = color == Piece.WHITE ? 0 : 7;
        int kingSquare = backRank * 8 + 4;
        int rookSquare = kingside ? backRank * 8 + 7 : backRank * 8;

        // Vérification des pièces
        if (game.board.squares[kingSquare] != king ||
                game.board.squares[rookSquare] != rook) {
            return false;
        }

        // Vérification des cases vides
        int start = kingside ? 5 : 1;
        int end = kingside ? 6 : 3;
        for (int file = start; file <= end; file++) {
            if (game.board.squares[backRank * 8 + file] != Piece.NONE) {
                return false;
            }
        }

        // Vérification des attaques - utiliser la nouvelle méthode directe
        int opponentColor = Piece.getOpponentColor(color);
        for (int file = 4; file <= (kingside ? 6 : 2); file++) {
            int square = backRank * 8 + file;
            if (game.isSquareAttacked(square, opponentColor)) {
                return false;
            }
        }

        return true;
    }

    private void generateSlidingMoves(int startSquare, int piece, Game game, List<Move> moves) {
        int startDirIndex = Piece.isType(piece, Piece.BISHOP) ? 4 : 0;
        int endDirIndex = Piece.isType(piece, Piece.ROOK) ? 4 : 8;

        for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
            for (int n = 0; n < numSquaresToEdge[startSquare][directionIndex]; n++) {
                int targetSquare = startSquare + directionOffsets[directionIndex] * (n + 1);
                int pieceOnTargetSquare = game.board.squares[targetSquare];

                if (Piece.getColor(pieceOnTargetSquare) == Piece.getColor(piece)) {
                    break;
                }

                moves.add(new Move(startSquare, targetSquare));

                if (Piece.getColor(pieceOnTargetSquare) == Piece.getOpponentColor(piece)) {
                    break;
                }
            }
        }
    }

    public boolean isSquareAttackedDirectly(Board board, int square, int attackerColor) {
        int[] directions = { 8, -8, -1, 1, 7, -7, 9, -9 }; // Toutes les directions

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

                    // Vérification des pièces attaquantes
                    if (i == 1 && type == Piece.KING)
                        return true;
                    if (type == Piece.QUEEN)
                        return true;
                    if (type == Piece.ROOK && direction < 4)
                        return true; // Directions orthogonales
                    if (type == Piece.BISHOP && direction >= 4)
                        return true; // Directions diagonales
                }
                break; // Une pièce bloque le chemin
            }
        }

        // Vérifier les attaques de pions
        int pawnDirection = attackerColor == Piece.WHITE ? 1 : -1;
        int[] pawnAttacks = { 7 * pawnDirection, 9 * pawnDirection };
        for (int attack : pawnAttacks) {
            int pawnSquare = square - attack;
            if (pawnSquare < 0 || pawnSquare >= 64)
                continue;

            int piece = board.squares[pawnSquare];
            if (Piece.getType(piece) == Piece.PAWN &&
                    Piece.getColor(piece) == attackerColor) {
                return true;
            }
        }

        // Vérifier les attaques de cavaliers
        int[] knightMoves = { 15, 17, -15, -17, 10, 6, -10, -6 };
        for (int move : knightMoves) {
            int knightSquare = square + move;
            if (knightSquare < 0 || knightSquare >= 64)
                continue;

            int piece = board.squares[knightSquare];
            if (Piece.getType(piece) == Piece.KNIGHT &&
                    Piece.getColor(piece) == attackerColor) {
                return true;
            }
        }

        return false;
    }
}
