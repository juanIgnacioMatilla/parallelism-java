package nqueens;

import java.util.*;
import java.util.concurrent.*;

public class NQueensVirtual {

    private static int N;

    public static void main(String[] args) throws Exception {

        int virtualThreads = (args.length >= 1)
                ? Integer.parseInt(args[0])
                : 100;  

        N = (args.length >= 2)
                ? Integer.parseInt(args[1])
                : 12;

        int threshold = (args.length >= 3)
                ? Integer.parseInt(args[2])
                : 2;

        long start = System.nanoTime();

        ExecutorService executor =
                Executors.newThreadPerTaskExecutor(
                        Thread.ofVirtual().factory()
                );

        // Crear tareas iniciales hasta virtualThreads o N, lo que sea menor
        List<Callable<Long>> tasks = new ArrayList<>();

        for (int col = 0; col < N; col++) {
            int[] board = new int[N];
            board[0] = col;

            tasks.add(() -> solve(1, board, threshold));
        }

        // Ejecutamos en paralelo
        List<Future<Long>> futures = executor.invokeAll(tasks);

        long solutions = 0;
        for (Future<Long> f : futures) {
            solutions += f.get();
        }

        executor.shutdown();

        long end = System.nanoTime();
        double ms = (end - start) / 1_000_000.0;

        System.out.println("Soluciones para " + N + " reinas: " + solutions);
        System.out.println("Tiempo (ms): " + ms);
        System.out.println("Virtual threads solicitados: " + virtualThreads);
        System.out.println("Threshold: " + threshold);
    }

    // ========================================================================
    // Solve con threshold para evitar explosión de recursión paralela
    // ========================================================================
    private static long solve(int row, int[] board, int threshold) {

        if (row == N) return 1;

        long count = 0;

        if (row < threshold) {
            // Mini-tareas paralelas con virtual threads
            List<Future<Long>> futures = new ArrayList<>();
            ExecutorService localExec =
                    Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

            for (int col = 0; col < N; col++) {
                if (isSafe(board, row, col)) {
                    int[] newBoard = board.clone();
                    newBoard[row] = col;

                    futures.add(localExec.submit(() -> solve(row + 1, newBoard, threshold)));
                }
            }

            for (Future<Long> f : futures) {
                try {
                    count += f.get();
                } catch (Exception ignored) {}
            }

            localExec.shutdown();
        } else {
            // Secuencial
            for (int col = 0; col < N; col++) {
                if (isSafe(board, row, col)) {
                    board[row] = col;
                    count += solve(row + 1, board, threshold);
                }
            }
        }

        return count;
    }

    private static boolean isSafe(int[] board, int row, int col) {
        for (int i = 0; i < row; i++) {
            if (board[i] == col ||
                board[i] - i == col - row ||
                board[i] + i == col + row)
                return false;
        }
        return true;
    }
}
