package server;

public class GameState {

    public static final int GRID = 5;
    public static final int SHIPS = 3;

    private boolean[][] board1 = new boolean[GRID][GRID];
    private boolean[][] board2 = new boolean[GRID][GRID];

    private int shipsLeft1 = SHIPS;
    private int shipsLeft2 = SHIPS;

    private boolean placed1 = false;
    private boolean placed2 = false;

    public synchronized boolean placeShips(int playerId, int[] coords) {
        if (coords.length != SHIPS * 2) return false;

        boolean[][] board = (playerId == 1) ? board1 : board2;

        for (int i = 0; i < coords.length; i += 2) {
            int r = coords[i];
            int c = coords[i + 1];

            if (r < 0 || r >= GRID || c < 0 || c >= GRID) {
                return false; // out of bounds
            }

            board[r][c] = true;
        }

        if (playerId == 1) placed1 = true;
        else placed2 = true;

        return true;
    }

    public synchronized boolean bothPlaced() {
        return placed1 && placed2;
    }

    public synchronized String attack(int attackerId, int r, int c) {
        if (r < 0 || r >= GRID || c < 0 || c >= GRID) {
            return "MISS";
        }

        boolean[][] defenderBoard = (attackerId == 1) ? board2 : board1;

        if (defenderBoard[r][c]) {
            defenderBoard[r][c] = false;

            if (attackerId == 1) shipsLeft2--;
            else shipsLeft1--;

            return "HIT";
        }

        return "MISS";
    }

    public synchronized boolean isGameOver() {
        return shipsLeft1 == 0 || shipsLeft2 == 0;
    }

    public synchronized int winnerIdOr0() {
        if (shipsLeft2 == 0) return 1;
        if (shipsLeft1 == 0) return 2;
        return 0;
    }

    public static void main(String[] args) {
        GameState g = new GameState();

        g.placeShips(1, new int[]{0,0, 1,1, 2,2});
        g.placeShips(2, new int[]{0,1, 1,2, 2,3});

        System.out.println(g.attack(1,0,1)); // HIT
        System.out.println(g.attack(1,4,4)); // MISS
    }
}
