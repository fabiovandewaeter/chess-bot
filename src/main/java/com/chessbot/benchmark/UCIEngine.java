package com.chessbot.benchmark;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class UCIEngine {
    private Process process;
    private BufferedReader reader;
    private PrintWriter writer;
    private String enginePath;

    public UCIEngine(String enginePath) {
        this.enginePath = enginePath;
    }

    public boolean start() {
        try {
            ProcessBuilder pb = new ProcessBuilder(enginePath);
            pb.redirectErrorStream(true);
            process = pb.start();

            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);

            // Initialiser UCI
            sendCommand("uci");
            waitForResponse("uciok");

            sendCommand("isready");
            waitForResponse("readyok");

            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage du moteur: " + e.getMessage());
            return false;
        }
    }

    public void setSkillLevel(int level) {
        // Pour Stockfish, niveau 0-20
        sendCommand("setoption name Skill Level value " + level);
        sendCommand("isready");
        waitForResponse("readyok");
    }

    public void setTimeLimit(int milliseconds) {
        sendCommand("setoption name movetime value " + milliseconds);
    }

    public String getBestMove(String position) {
        sendCommand("position fen " + position);
        sendCommand("go movetime 5000"); // 5 secondes

        String bestMove = null;
        String line;
        try {
            long startTime = System.currentTimeMillis();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 2) {
                        bestMove = parts[1];
                        break;
                    }
                }

                // Timeout de sécurité
                if (System.currentTimeMillis() - startTime > 10000) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture: " + e.getMessage());
        }

        return bestMove != null ? bestMove : "0000"; // Move null en cas d'erreur
    }

    public void sendCommand(String command) {
        writer.println(command);
        writer.flush();
    }

    private void waitForResponse(String expected) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(expected)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'attente: " + e.getMessage());
        }
    }

    public void close() {
        try {
            sendCommand("quit");
            if (process != null) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture: " + e.getMessage());
        }
    }
}
