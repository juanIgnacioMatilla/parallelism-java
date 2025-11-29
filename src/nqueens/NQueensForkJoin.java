package nqueens;

import java.util.concurrent.*;
import java.util.*;

public class NQueensForkJoin {

    private static int N;

    public static void main(String[] args) {
        int numThreads = (args.length >= 1)
                ? Integer.parseInt(args[0])
                : Runtime.getRuntime().availableProcessors();

        N = (args.length >= 2)
                ? Integer.parseInt(args[1])
                : 12;

        int threshold = (args.length >= 3)
                ? Integer.parseInt(args[2])
                : 1; // profundidad a la que se crean tareas ForkJoin

        ForkJoinPool pool = new ForkJoinPool(numThreads);

        long start = System.nanoTime();

        // Crear una tarea raíz que resolverá todo
        NQueensTask root = new NQueensTask(0, new int[N], threshold);
        long solutions = pool.invoke(root);

        long end = System.nanoTime();
        double ms = (end - start) / 1_000_000.0;

        System.out.println("Soluciones para " + N + " reinas: " + solutions);
        System.out.println("Tiempo (ms): " + ms);
        System.out.println("Threads usados: " + numThreads);
        System.out.println("Threshold usado: " + threshold);

        pool.shutdown();
    }

    // =========================================================================
    // RecursiveTask con threshold
    // =========================================================================
    static class NQueensTask extends RecursiveTask<Long> {

        private final int row;
        private final int[] board;
        private final int threshold;

        NQueensTask(int row, int[] board, int threshold) {
            this.row = row;
            this.board = board.clone();
            this.threshold = threshold;
        }

        @Override
        protected Long compute() {
            // Caso base
            if (row == N) return 1L;

            // Si estamos por encima del threshold → dividir (fork)
            if (row < threshold) {
                List<NQueensTask> subtasks = new ArrayList<>();

                for (int col = 0; col < N; col++) {
                    if (isSafe(board, row, col)) {
                        int[] newBoard = board.clone();
                        newBoard[row] = col;

                        NQueensTask task = new NQueensTask(row + 1, newBoard, threshold);
                        subtasks.add(task);
                        task.fork();
                    }
                }

                long total = 0;
                for (NQueensTask task : subtasks)
                    total += task.join();

                return total;
            }

            // Si estamos por debajo del threshold → resolver secuencialmente
            return solveSequential(row, board);
        }
    }

    // =========================================================================
    // Backtracking secuencial clásico
    // =========================================================================
    private static long solveSequential(int row, int[] board) {
        if (row == N) return 1;

        long count = 0;
        for (int col = 0; col < N; col++) {
            if (isSafe(board, row, col)) {
                board[row] = col;
                count += solveSequential(row + 1, board);
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
