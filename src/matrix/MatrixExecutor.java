package matrix;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixExecutor {

    private static final int SIZE = 1024;
    private static final long SEED = 6834723L;

    public static void main(String[] args) throws InterruptedException {

        double[][] A = new double[SIZE][SIZE];
        double[][] B = new double[SIZE][SIZE];
        double[][] C = new double[SIZE][SIZE];

        Random random = new Random(SEED);

        // Initialize matrices
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                A[i][j] = random.nextDouble();
                B[i][j] = random.nextDouble();
                C[i][j] = 0.0;
            }
        }

        // Read number of threads from args, or default to CPU count
        int numThreads;
        if (args.length > 0) {
            numThreads = Integer.parseInt(args[0]);
        } else {
            numThreads = Runtime.getRuntime().availableProcessors();
        }

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        long start = System.nanoTime();

        // Submit 1 task per row
        for (int i = 0; i < SIZE; i++) {
            final int row = i;
            executor.submit(() -> {
                for (int j = 0; j < SIZE; j++) {
                    double sum = 0.0;
                    for (int k = 0; k < SIZE; k++) {
                        sum += A[row][k] * B[k][j];
                    }
                    C[row][j] = sum;
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        long end = System.nanoTime();
        double millis = (end - start) / 1_000_000.0;

        System.out.println("Fin: " + C[0][0]);
        System.out.println("Tiempo (ms): " + millis);
        System.out.println("Threads usados: " + numThreads);
    }
}

