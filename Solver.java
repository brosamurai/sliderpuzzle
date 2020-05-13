/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.Queue;

import java.util.Comparator;

public class Solver {
    private int moves = 0;

    private MinPQ<Board> gamePQ;
    private Queue<Board> movesQueue = new Queue<>();
    private boolean cachedIsSolvable;
    private boolean solutionAttempted = false;
    private Board initialBoard;

    // find a solution to the initial board (use A*)
    public Solver(Board initial) {
        // initialize the gamePQ
        gamePQ = new MinPQ<Board>(sortByManhattan());
        initialBoard = initial;
        isSolvable();
    }

    // is the initial board solvable?
    public boolean isSolvable() {
        if (solutionAttempted) return cachedIsSolvable;
        else {
            // try to solve the current board and the boards twin in lockstep
            // if the current board reaches the goal board first, then it's solvable
            // if the twin board reaches the goal board first, then it's unsolvable!

            solutionAttempted = true;
            cachedIsSolvable = runAstarInLockStep();

            return cachedIsSolvable;
        }
        // return false;
    }

    private boolean runAstarInLockStep() {
        boolean lockstep = true; // used to alternate between twin and initial A* searches

        gamePQ.insert(initialBoard);

        // create the twinPQ which will run A* in lockstep with the gamePQ
        MinPQ<Board> twinPQ = new MinPQ<>(sortByManhattan());
        Board initalTwinBoard = initialBoard.twin();
        twinPQ.insert(initalTwinBoard);

        Board previousGameBoard = null;
        Board previousTwinBoard = null;

        while (true) {
            // if lockstep true, run the A* iteration on initial game board
            if (lockstep) {
                movesQueue.enqueue(gamePQ.min());
                Board latestEntry = gamePQ.min();
                gamePQ.delMin();
                if (latestEntry.isGoal()) {
                    return true;
                }
                moves++;
                for (Board b : latestEntry.neighbors()) {
                    if (!(b.equals(previousGameBoard))) gamePQ.insert(b);
                }

                previousGameBoard = latestEntry;

                lockstep = false;
            }
            else {
                Board latestEntry = twinPQ.min();
                twinPQ.delMin();
                if (latestEntry.isGoal()) return false;

                for (Board b : latestEntry.neighbors()) {
                    if (!(b.equals(previousTwinBoard))) twinPQ.insert(b);
                }

                previousTwinBoard = latestEntry;

                lockstep = true;
            }
        }
    }

    private Comparator<Board> sortByManhattan() {
        return new ByManhattanPriority(moves);
    }

    private static class ByManhattanPriority implements Comparator<Board> {
        private int moves;

        public ByManhattanPriority(int moves) {
            this.moves = moves;
        }

        public int compare(Board o1, Board o2) {
            int priority1 = o1.manhattan() + moves;
            int priority2 = o2.manhattan() + moves;

            if (priority1 < priority2) return -1;
            else if (priority1 > priority2) return +1;
            else return 0;
            // if (current.slopeTo(o1) == current.slopeTo(o2)) return 0;
            // else if (current.slopeTo(o1) > current.slopeTo(o2)) return +1;
            // else return -1;
        }
    }

    // min number of moves to solve initial board
    public int moves() {
        return moves;
    }

    // sequence of boards in a shortest solution
    public Iterable<Board> solution() {
        return movesQueue;
    }

    public static void main(String[] args) {
        for (String filename : args) {

            // read in the board specified in the filename
            In in = new In(filename);
            int n = in.readInt();
            int[][] tiles = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    tiles[i][j] = in.readInt();
                }
            }

            // solve the slider puzzle
            Board initial = new Board(tiles);
            Solver solver = new Solver(initial);

            if (solver.isSolvable()) {
                System.out.println("SOLVABLE!");
            }
            else {
                System.out.println("UNSOLVABLE!");
            }
            // Solver solver = new Solver(initial);
            // StdOut.println(filename + ": " + solver.moves());
        }
    }
}
