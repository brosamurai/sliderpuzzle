/* *****************************************************************************
 *  Description: Solves an n x n 8puzzle using A*
 **************************************************************************** */

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdOut;

import java.util.Comparator;

public class Solver {
    private int moves = 0;

    private Stack<Board> movesStack = new Stack<>();
    private boolean cachedIsSolvable;
    private boolean solutionAttempted = false;
    private Board initialBoard;

    // find a solution to the initial board (use A*)
    public Solver(Board initial) {
        if (initial == null) {
            throw new IllegalArgumentException("argument cannot be null");
        }
        // load the input as the initalBoard
        initialBoard = initial;
        isSolvable();
    }

    // Inner class used to define a 'node' for the A* search.
    // board is the nxn board at this node
    // movesToReachThisBoard is the number of moves from initial node to this node
    // previousNode is the previousSearchNode. For the initial node, this is null
    private class SearchNode {
        private Board board;
        private int movesToReachThisBoard;
        private SearchNode previousNode;

        private SearchNode(Board board, int moves, SearchNode previousNode) {
            this.board = board;
            this.movesToReachThisBoard = moves;
            if (previousNode != null) this.previousNode = previousNode;
        }

        private SearchNode getPreviousNode() {
            return previousNode;
        }

        private int getMovesToReachThisBoard() {
            return movesToReachThisBoard;
        }

        private Board getBoard() {
            return board;
        }
    }

    // is the initial board solvable? This is determined by running two A* searches
    // in lockstep. One search for the original input, and the other search with the
    // input's twin. If the twin search reaches the goal then it is not solvable.
    // Otherwise it's solvable and we've found the solution
    public boolean isSolvable() {
        if (solutionAttempted) return cachedIsSolvable;
        else {
            solutionAttempted = true;
            cachedIsSolvable = runAstarInLockStep();
            return cachedIsSolvable;
        }
    }

    private boolean runAstarInLockStep() {
        boolean lockstep = true; // used to alternate between twin and initial A* searches
        // initialize the gamePQ
        MinPQ<SearchNode> gamePQ = new MinPQ<SearchNode>(sortByManhattan());
        SearchNode initialSearchNode = new SearchNode(initialBoard, 0, null);
        gamePQ.insert(initialSearchNode);

        // create the twinPQ which will run A* in lockstep with the gamePQ
        MinPQ<SearchNode> twinPQ = new MinPQ<>(sortByManhattan());
        SearchNode initialTwinSearchNode = new SearchNode(initialBoard.twin(), 0, null);
        twinPQ.insert(initialTwinSearchNode);

        SearchNode previousSearchNode = null;
        SearchNode previousTwinNode = null;

        while (true) {
            // if lockstep true, run the A* iteration on initial game board
            if (lockstep) {
                SearchNode latestSearchNode = gamePQ.min();
                gamePQ.delMin();
                if (latestSearchNode.getBoard().isGoal()) {
                    moves = latestSearchNode.getMovesToReachThisBoard();
                    populateMovesStack(latestSearchNode);
                    return true;
                }
                for (Board b : latestSearchNode.getBoard().neighbors()) {
                    if (previousSearchNode == null) {
                        SearchNode neighbor = new SearchNode(b, latestSearchNode
                                .getMovesToReachThisBoard() + 1, latestSearchNode);
                        gamePQ.insert(neighbor);
                    }
                    else if (!(b.equals(previousSearchNode.getBoard()))) {
                        SearchNode neighbor = new SearchNode(b, latestSearchNode
                                .getMovesToReachThisBoard() + 1,
                                                             latestSearchNode);
                        gamePQ.insert(neighbor);
                    }
                }
                previousSearchNode = latestSearchNode;
                lockstep = false;
            }
            else {
                SearchNode latestTwinNode = twinPQ.min();
                twinPQ.delMin();
                if (latestTwinNode.getBoard().isGoal()) return false;

                for (Board b : latestTwinNode.getBoard().neighbors()) {
                    if (previousTwinNode == null) {
                        SearchNode neighbor = new SearchNode(b, latestTwinNode
                                .getMovesToReachThisBoard() + 1, latestTwinNode);
                        twinPQ.insert(neighbor);
                    }
                    else if (!(b.equals(previousTwinNode.getBoard()))) {
                        SearchNode neighbor = new SearchNode(b, latestTwinNode
                                .getMovesToReachThisBoard() + 1, latestTwinNode);
                        twinPQ.insert(neighbor);
                    }
                }
                previousTwinNode = latestTwinNode;
                lockstep = true;
            }
        }
    }

    private void populateMovesStack(SearchNode node) {
        while (node.getPreviousNode() != null) {
            movesStack.push(node.getBoard());
            node = node.getPreviousNode();
        }
        movesStack.push(node.getBoard());
    }

    private Comparator<SearchNode> sortByManhattan() {
        return new ByManhattanPriority();
    }

    private class ByManhattanPriority implements Comparator<SearchNode> {

        public int compare(SearchNode o1, SearchNode o2) {
            int priority1 = o1.getBoard().manhattan() + o1.getMovesToReachThisBoard();
            int priority2 = o2.getBoard().manhattan() + o2.getMovesToReachThisBoard();

            if (priority1 < priority2) return -1;
            else if (priority1 > priority2) return +1;
            else {
                if (o1.getBoard().hamming() < o2.getBoard().hamming()) return -1;
                else if (o1.getBoard().hamming() > o2.getBoard().hamming()) return +1;
                else return 0;
            }
        }
    }

    // min number of moves to solve initial board
    public int moves() {
        return moves;
    }

    // sequence of boards in a shortest solution
    public Iterable<Board> solution() {
        return movesStack;
    }

    public static void main(String[] args) {
        // create initial board from file
        In in = new In(args[0]);
        int n = in.readInt();
        int[][] tiles = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                tiles[i][j] = in.readInt();
        Board initial = new Board(tiles);

        // solve the puzzle
        Solver solver = new Solver(initial);

        // print solution to standard output
        if (!solver.isSolvable())
            StdOut.println("No solution possible");
        else {
            StdOut.println("Minimum number of moves = " + solver.moves());
            for (Board board : solver.solution())
                StdOut.println(board);
        }
    }
}
