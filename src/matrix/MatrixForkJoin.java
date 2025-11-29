package matrix;

import java.util.Random;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;

public class MatrixForkJoin {

    private static final int SIZE = 1024;
    private static final long SEED = 6834723L;

    public static void main(String[] args) {

        double[][] A = new double[SIZE][SIZE];
        double[][] B = new double[SIZE][SIZE];
        double[][] C = new double[SIZE][SIZE];

        Random random = new Random(SEED);

        // Inicialización
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                A[i][j] = random.nextDouble();
                B[i][j] = random.nextDouble();
                C[i][j] = 0.0;
            }
        }

        // Leer cantidad de hilos desde args[0]
        int numThreads;
        if (args.length > 0) {
            numThreads = Integer.parseInt(args[0]);
        } else {
            numThreads = Runtime.getRuntime().availableProcessors();
        }

        // Leer threshold desde args[1]
        int threshold;
        if (args.length > 1) {
            threshold = Integer.parseInt(args[1]);
        } else {
            threshold = 64; // valor por defecto
        }

        ForkJoinPool pool = new ForkJoinPool(numThreads);

        long start = System.nanoTime();

        // Ejecutar tarea principal
        pool.invoke(new MultiplyTask(A, B, C, 0, SIZE, threshold));

        long end = System.nanoTime();
        double millis = (end - start) / 1_000_000.0;

        System.out.println("Fin: " + C[0][0]);
        System.out.println("Tiempo (ms): " + millis);
        System.out.println("Threads usados: " + numThreads);
        System.out.println("Threshold: " + threshold);
    }

    // Tarea Fork/Join configurable
    static class MultiplyTask extends RecursiveAction {

        private final double[][] A, B, C;
        private final int startRow, endRow;
        private final int threshold;

        MultiplyTask(double[][] A, double[][] B, double[][] C, int startRow, int endRow, int threshold) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.startRow = startRow;
            this.endRow = endRow;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            int rows = endRow - startRow;

            if (rows <= threshold) {
                // Computo directo
                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j < SIZE; j++) {
                        double sum = 0.0;
                        for (int k = 0; k < SIZE; k++) {
                            sum += A[i][k] * B[k][j];
                        }
                        C[i][j] = sum;
                    }
                }
            } else {
                // Dividir tarea (divide & conquer)
                int mid = (startRow + endRow) / 2;

                MultiplyTask left = new MultiplyTask(A, B, C, startRow, mid, threshold);
                MultiplyTask right = new MultiplyTask(A, B, C, mid, endRow, threshold);

                invokeAll(left, right);
            }
        }
    }
}

