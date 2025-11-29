package matrix;

import java.util.Random;

public class MatrixSequential {

    private static final int SIZE = 1024;
    private static final long SEED = 6834723L;

    public static void main(String[] args) {
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

        long start = System.nanoTime();

        // Matrix multiplication (classic triple loop)
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                double sum = 0.0;
                for (int k = 0; k < SIZE; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
            }
        }

        long end = System.nanoTime();
        double millis = (end - start) / 1_000_000.0;

        System.out.println("Fin: " + C[0][0]);
        System.out.println("Tiempo (ms): " + millis);
    }
}

