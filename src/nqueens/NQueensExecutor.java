    package nqueens;

import java.util.concurrent.*;
import java.util.*;

public class NQueensExecutor {

    private static int N;
    private static ExecutorService executor;

    public static void main(String[] args) throws InterruptedException {
        int numThreads = (args.length >= 1) ? Integer.parseInt(args[0]) : Runtime.getRuntime().availableProcessors();
        N = (args.length >= 2) ? Integer.parseInt(args[1]) : 12;

        executor = Executors.newFixedThreadPool(numThreads);

        long start = System.nanoTime();

        List<Future<Long>> futures = new ArrayList<>();

        // Paralelizamos las elecciones en la primera fila
        for (int col = 0; col < N; col++) {
            final int firstCol = col;

            futures.add(executor.submit(() -> {
                int[] board = new int[N];
                board[0] = firstCol;
                return solve(1, board);
            }));
        }

        long solutions = 0;
        for (Future<Long> f : futures) {
            try {
                solutions += f.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        long end = System.nanoTime();
        double ms = (end - start) / 1_000_000.0;

        System.out.println("Soluciones para " + N + " reinas: " + solutions);
        System.out.println("Tiempo (ms): " + ms);
        System.out.println("Threads usados: " + numThreads);
    }

    private static long solve(int row, int[] board) {
        if (row == N) return 1;

        long count = 0;
        for (int col = 0; col < N; col++) {
            if (isSafe(board, row, col)) {
                board[row] = col;
                count += solve(row + 1, board);
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
