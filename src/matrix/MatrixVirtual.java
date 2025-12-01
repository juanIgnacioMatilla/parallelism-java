package matrix;

import java.util.Random;

public class MatrixVirtual {

    private static final int SIZE = 1024;
    private static final long SEED = 6834723L;

    public static void main(String[] args) throws InterruptedException {

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

        // Cantidad de hilos virtuales (uno por fila)
        int numThreads;
        if (args.length > 0) {
            numThreads = Integer.parseInt(args[0]);
        } else {
            numThreads = SIZE; // una virtual thread por fila
        }

        Thread[] threads = new Thread[numThreads];

        // Asignar filas en bloques para permitir cambiar numThreads
        int blockSize = SIZE / numThreads;

        long start = System.nanoTime();

        for (int t = 0; t < numThreads; t++) {
            int startRow = t * blockSize;
            int endRow = (t == numThreads - 1) ? SIZE : startRow + blockSize;

            threads[t] = Thread.startVirtualThread(() -> {
                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j < SIZE; j++) {
                        double sum = 0.0;
                        for (int k = 0; k < SIZE; k++) {
                            sum += A[i][k] * B[k][j];
                        }
                        C[i][j] = sum;
                    }
                }
            });
        }

        // Esperar a que terminen
        for (Thread thread : threads) {
            thread.join();
        }

        long end = System.nanoTime();
        double millis = (end - start) / 1_000_000.0;

        System.out.println("Fin: " + C[0][0]);
        System.out.println("Tiempo (ms): " + millis);
        System.out.println("Virtual threads usados: " + numThreads);
    }
}

