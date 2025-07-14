package com.chessbot.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class BenchmarkResults {
    private List<MatchupResult> results;

    public BenchmarkResults() {
        this.results = new ArrayList<>();
    }

    public void addMatchupResult(MatchupResult result) {
        results.add(result);
    }

    public void printSummary() {
        System.out.println("\n=== RÉSULTATS DU BENCHMARK ===");
        System.out.println("Bot Level | Stockfish Level | Victoires | Défaites | Nuls | Taux de victoire");
        System.out.println("-".repeat(80));

        for (MatchupResult result : results) {
            System.out.printf("    %d     |       %d        |    %d     |    %d     |  %d  |     %.1f%%\n",
                    result.botLevel, result.stockfishLevel, result.getBotWins(),
                    result.getStockfishWins(), result.getDraws(), result.getWinRate());
        }
    }

    public void generateCSV(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("BotLevel,StockfishLevel,BotWins,StockfishWins,Draws,WinRate\n");

            for (MatchupResult result : results) {
                writer.write(String.format("%d,%d,%d,%d,%d,%.2f\n",
                        result.botLevel, result.stockfishLevel, result.getBotWins(),
                        result.getStockfishWins(), result.getDraws(), result.getWinRate()));
            }

            System.out.println("Résultats sauvegardés dans " + filename);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    public void generatePythonVisualization(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("import matplotlib.pyplot as plt\n");
            writer.write("import numpy as np\n");
            writer.write("import pandas as pd\n\n");

            writer.write("# Données du benchmark\n");
            writer.write("data = {\n");
            writer.write("    'BotLevel': [");
            for (int i = 0; i < results.size(); i++) {
                writer.write(String.valueOf(results.get(i).botLevel));
                if (i < results.size() - 1)
                    writer.write(", ");
            }
            writer.write("],\n");

            writer.write("    'StockfishLevel': [");
            for (int i = 0; i < results.size(); i++) {
                writer.write(String.valueOf(results.get(i).stockfishLevel));
                if (i < results.size() - 1)
                    writer.write(", ");
            }
            writer.write("],\n");

            writer.write("    'WinRate': [");
            for (int i = 0; i < results.size(); i++) {
                writer.write(String.format("%.2f", results.get(i).getWinRate()));
                if (i < results.size() - 1)
                    writer.write(", ");
            }
            writer.write("]\n");
            writer.write("}\n\n");

            writer.write("df = pd.DataFrame(data)\n\n");

            writer.write("# Graphique en barres\n");
            writer.write("plt.figure(figsize=(12, 8))\n");
            writer.write("for bot_level in sorted(df['BotLevel'].unique()):\n");
            writer.write("    subset = df[df['BotLevel'] == bot_level]\n");
            writer.write("    plt.plot(subset['StockfishLevel'], subset['WinRate'], \n");
            writer.write("             marker='o', label=f'Bot Level {bot_level}')\n\n");

            writer.write("plt.xlabel('Niveau Stockfish')\n");
            writer.write("plt.ylabel('Taux de victoire du bot (%)')\n");
            writer.write("plt.title('Performance du bot vs Stockfish')\n");
            writer.write("plt.legend()\n");
            writer.write("plt.grid(True, alpha=0.3)\n");
            writer.write("plt.show()\n\n");

            writer.write("# Heatmap\n");
            writer.write("pivot_table = df.pivot(index='BotLevel', columns='StockfishLevel', values='WinRate')\n");
            writer.write("plt.figure(figsize=(10, 6))\n");
            writer.write("plt.imshow(pivot_table.values, cmap='RdYlGn', aspect='auto')\n");
            writer.write("plt.colorbar(label='Taux de victoire (%)')\n");
            writer.write("plt.xlabel('Niveau Stockfish')\n");
            writer.write("plt.ylabel('Niveau Bot')\n");
            writer.write("plt.title('Heatmap des performances')\n");
            writer.write("plt.xticks(range(len(pivot_table.columns)), pivot_table.columns)\n");
            writer.write("plt.yticks(range(len(pivot_table.index)), pivot_table.index)\n");
            writer.write("plt.show()\n");

            System.out.println("Script Python de visualisation généré: " + filename);
        } catch (IOException e) {
            System.err.println("Erreur lors de la génération du script: " + e.getMessage());
        }
    }
}
