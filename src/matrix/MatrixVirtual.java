    package nqueens;

    import java.util.*;
    import java.util.concurrent.*;
    import java.util.concurrent.atomic.AtomicLong;

    public class NQueensVirtual {

        private static int N;

        // === Contadores de debug ===
        private static final AtomicLong TOP_TASKS = new AtomicLong(0);
        private static final AtomicLong LOCAL_EXECUTORS = new AtomicLong(0);
        private static final AtomicLong SUBTASKS = new AtomicLong(0);

        // Activar/desactivar prints detallados
        private static final boolean VERBOSE_THREADS = false;

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

            System.out.println("=== NQueensVirtual DEBUG ===");
            System.out.println("Args -> virtualThreads=" + virtualThreads +
                            ", N=" + N +
                            ", threshold=" + threshold);

            long start = System.nanoTime();

            // Ahora SÍ usamos virtualThreads para limitar el pool principal
            ExecutorService executor =
                    Executors.newFixedThreadPool(
                            virtualThreads,
                            Thread.ofVirtual().factory()
                    );

            List<Callable<Long>> tasks = new ArrayList<>();

            for (int col = 0; col < N; col++) {
                int[] board = new int[N];
                board[0] = col;
                final int initialCol = col;

                tasks.add(() -> {
                    if (VERBOSE_THREADS) {
                        System.out.printf("[TOP-TASK] col=%d, thread=%s, virtual=%b%n",
                                initialCol,
                                Thread.currentThread().getName(),
                                Thread.currentThread().isVirtual());
                    }
                    return solve(1, board, threshold);
                });

                TOP_TASKS.incrementAndGet();
            }

            System.out.println("DEBUG: top-level tasks creadas = " + TOP_TASKS.get());

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
            System.out.println("Virtual threads solicitados (pool size): " + virtualThreads);
            System.out.println("Threshold: " + threshold);

            // === Resumen de debug ===
            System.out.println("=== DEBUG STATS ===");
            System.out.println("Top-level tasks:      " + TOP_TASKS.get());
            System.out.println("Subtasks creadas:     " + SUBTASKS.get());
            System.out.println("Local executors:      " + LOCAL_EXECUTORS.get());
        }

        // ========================================================================
        // Solve con threshold para evitar explosión de recursión paralela
        // ========================================================================
        private static long solve(int row, int[] board, int threshold) {

            if (row == N) return 1;

            long count = 0;

            if (row < threshold) {
                // Mini-tareas paralelas con virtual threads
                LOCAL_EXECUTORS.incrementAndGet();

                ExecutorService localExec =
                        Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

                List<Future<Long>> futures = new ArrayList<>();

                for (int col = 0; col < N; col++) {
                    if (isSafe(board, row, col)) {
                        int[] newBoard = board.clone();
                        newBoard[row] = col;
                        final int rowFinal = row;
                        final int colFinal = col;

                        SUBTASKS.incrementAndGet();

                        futures.add(localExec.submit(() -> {
                            if (VERBOSE_THREADS) {
                                System.out.printf("[SUB-TASK] row=%d col=%d, thread=%s, virtual=%b%n",
                                        rowFinal,
                                        colFinal,
                                        Thread.currentThread().getName(),
                                        Thread.currentThread().isVirtual());
                            }
                            return solve(rowFinal + 1, newBoard, threshold);
                        }));
                    }
                }

                for (Future<Long> f : futures) {
                    try {
                        count += f.get();
                    } catch (Exception e) {
                        System.err.println("ERROR en sub-tarea: " + e.getMessage());
                    }
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
