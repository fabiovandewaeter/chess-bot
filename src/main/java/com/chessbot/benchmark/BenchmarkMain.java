package com.chessbot.benchmark;

public class BenchmarkMain {
    public static void main(String[] args) {
        // Configuration
        // String stockfishPath = "stockfish"; // Chemin vers l'exécutable Stockfish
        String stockfishPath = ".\\stockfish\\stockfish-windows-x86-64-avx2.exe"; // Chemin vers l'exécutable Stockfish
        int threads = Runtime.getRuntime().availableProcessors();

        // Niveaux à tester
        // int[] botLevels = { 1, 2, 3, 4, 5 };
        // int[] stockfishLevels = { 1, 3, 5, 7, 10 };
        int[] botLevels = { 5 };
        int[] stockfishLevels = { 10 };
        int gamesPerMatchup = 1; // Nombre de parties par matchup

        System.out.println("=== BENCHMARK BOT D'ÉCHECS ===");
        System.out.println("Niveaux bot: " + java.util.Arrays.toString(botLevels));
        System.out.println("Niveaux Stockfish: " + java.util.Arrays.toString(stockfishLevels));
        System.out.println("Parties par matchup: " + gamesPerMatchup);
        System.out.println("Threads: " + threads);
        System.out.println("Stockfish: " + stockfishPath);
        System.out.println();

        System.out.println("Démarrage du benchmark...");
        System.out.println("Vérification Stockfish: " + testStockfish(stockfishPath));
        BenchmarkRunner runner = new BenchmarkRunner(stockfishPath, threads);
        BenchmarkResults results = runner.runBenchmark(botLevels, stockfishLevels, gamesPerMatchup);

        // Afficher les résultats
        results.printSummary();

        // Générer les fichiers de sortie
        results.generateCSV("benchmark_results.csv");
        results.generatePythonVisualization("visualize_results.py");

        runner.shutdown();

        System.out.println("\nBenchmark terminé !");
        System.out.println("Lancez 'python visualize_results.py' pour voir les graphiques.");
    }

    private static boolean testStockfish(String path) {
        try {
            UCIEngine engine = new UCIEngine(path);
            if (!engine.start())
                return false;

            engine.sendCommand("uci");
            engine.sendCommand("isready");
            engine.sendCommand("quit");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
