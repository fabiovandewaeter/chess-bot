package com.chessbot;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        boolean useUnicode = false;

        Game game = new Game(useUnicode, true);

        System.out.println("\nFormat des coups : e2e4, a7a5, etc.");
        System.out.println("Tapez 'quit' pour quitter\n");

        while (!game.isGameOver()) {
            game.board.print();

            String currentPlayer = (game.colorToMove == Piece.WHITE) ? "Blancs" : "Noirs";
            System.out.println("Tour des " + currentPlayer);
            System.out.print("Entrez votre coup : ");

            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit")) {
                break;
            }

            if (game.makeMove(input)) {
                System.out.println("Coup joué : " + input);
                if (game.isInCheck(game.colorToMove)) {
                    System.out.println("Échec !");
                }
            } else {
                System.out.println("Coup invalide ! Essayez encore.");
            }

            System.out.println();
        }

        scanner.close();
        System.out.println("Partie terminée !");
    }
}
