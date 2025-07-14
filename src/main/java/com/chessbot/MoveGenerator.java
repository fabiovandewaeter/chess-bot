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
}
