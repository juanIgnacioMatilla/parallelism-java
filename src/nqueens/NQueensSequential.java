package nqueens;

public class NQueensSequential {
    private static long solutions = 0;
    private static int N;

    public static void main(String[] args) {
        if (args.length > 0) {
            N = Integer.parseInt(args[0]);
        } else {
            N = 12; // valor por defecto
        }

        int[] board = new int[N];
        long start = System.nanoTime();

        solve(0, board);

        long end = System.nanoTime();
        double ms = (end - start) / 1_000_000.0;

        System.out.println("Soluciones para " + N + " reinas: " + solutions);
        System.out.println("Tiempo (ms): " + ms);
    }

    private static void solve(int row, int[] board) {
        if (row == N) {
            solutions++;
            return;
        }

        for (int col = 0; col < N; col++) {
            if (isSafe(board, row, col)) {
                board[row] = col;
                solve(row + 1, board);
            }
        }
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
