package nqueens;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NQueensVirtual {

    private static int N;
    private static ExecutorService EXECUTOR;

    // Contador REAL de virtual threads creados
    private static final AtomicInteger VT_COUNT = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {

        N = (args.length >= 1)
                ? Integer.parseInt(args[0])
                : 12;

        int threshold = (args.length >= 2)
                ? Integer.parseInt(args[1])
                : 3;

        System.out.println("=== NQueensVirtual (count VT) ===");
        System.out.println("N=" + N + ", threshold=" + threshold);

        // ================================================================
        // Crear ThreadFactory que cuenta virtual threads
        // ================================================================
        ThreadFactory baseFactory = Thread.ofVirtual()
                .name("vthread-", 0)
                .factory();

        ThreadFactory countingFactory = runnable -> {
            VT_COUNT.incrementAndGet();
            return baseFactory.newThread(runnable);
        };

        EXECUTOR = Executors.newThreadPerTaskExecutor(countingFactory);

        long start = System.nanoTime();

        // ================================================================
        // Crear tareas top-level
        // ================================================================
        List<Future<Long>> futures = new ArrayList<>();

        for (int col = 0; col < N; col++) {
            int[] board = new int[N];
            board[0] = col;

            futures.add(EXECUTOR.submit(() ->
                solveParallel(1, board, threshold)
            ));
        }

        long solutions = 0;
        for (Future<Long> f : futures)
            solutions += f.get();

        EXECUTOR.shutdown();

        long end = System.nanoTime();
        double ms = (end - start) / 1_000_000.0;

        // ================================================================
        // Imprimir resultados
        // ================================================================
        System.out.println("Tiempo (ms): " + ms);
        System.out.println("Soluciones: " + solutions);
        System.out.println("Virtual threads realmente creados: " + VT_COUNT.get());
    }

    // ========================================================================
    // solveParallel: paralelizamos hasta threshold
    // ========================================================================
    private static long solveParallel(int row, int[] board, int threshold) throws Exception {

        if (row == N) return 1;

        if (row < threshold) {
            List<Future<Long>> futures = new ArrayList<>();

            for (int col = 0; col < N; col++) {
                if (isSafe(board, row, col)) {
                    int[] newBoard = board.clone();
                    newBoard[row] = col;

                    futures.add(EXECUTOR.submit(() ->
                        solveParallel(row + 1, newBoard, threshold)
                    ));
                }
            }

            long count = 0;
            for (Future<Long> f : futures)
                count += f.get();
            return count;
        }

        return solveSequential(row, board);
    }

    // ========================================================================
    // Parte secuencial
    // ========================================================================
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
