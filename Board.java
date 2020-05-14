/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Stack;

import java.util.Arrays;

public class Board {
    private final int[][] currentBoard;
    private int missingTileX = -1;
    private int missingTileY = -1;
    private int cachedManhattanDistance = -1;
    private int cachedHammingDistance = -1;

    // create a board from an n-by-b array of tiles,
    // where tiles[row][col] = tile at (row, col)
    public Board(int[][] tiles) {
        currentBoard = tiles.clone();
        findMissingTile();
    }

    // string representation of this board
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(dimension() + "\n");
        for (int i = 0; i < dimension(); i++) {
            for (int j = 0; j < dimension(); j++) {
                s.append(String.format("%2d ", currentBoard[i][j]));
            }
            s.append("\n");
        }
        return s.toString();
    }

    // board dimension n
    public int dimension() {
        return currentBoard.length;
    }

    // number of tiles out of place
    public int hamming() {
        // if cachedHammingDistance is -1 then calculate it, otherwise return the cached
        if (cachedHammingDistance == -1) {
            int hamming = 0;
            int expectedValue = 1;
            for (int i = 0; i < dimension(); i++) {
                for (int j = 0; j < dimension(); j++) {
                    if (currentBoard[i][j] != expectedValue && (currentBoard[i][j] != 0)) hamming++;
                    expectedValue++;
                }
            }
            cachedHammingDistance = hamming;
        }
        return cachedHammingDistance;
    }

    // sum of Manhattan distances between tiles and goal
    public int manhattan() {
        // if the cachedManhattanDistance is not null, then calculate it otherwise return cached
        if (cachedManhattanDistance == -1) {
            int manhattan = 0;
            int expectedValue = 1;

            for (int i = 0; i < dimension(); i++) {
                for (int j = 0; j < dimension(); j++) {
                    if (currentBoard[i][j] != expectedValue && currentBoard[i][j] != 0) {
                        manhattan += calcManhattanDistance(currentBoard[i][j], i, j);
                    }
                    expectedValue++;
                }
            }
            cachedManhattanDistance = manhattan;
        }
        return cachedManhattanDistance;
    }

    // is this board the goal board?
    public boolean isGoal() {
        if (manhattan() == 0 || hamming() == 0) {
            return true;
        }
        return false;
    }

    // does this board equal y?
    public boolean equals(Object other) {
        if (this == other) return true;
        else if (other == null || this.getClass() != other.getClass()) return false;
        Board boardToCompore = (Board) other;
        return (Arrays.deepEquals(this.getCurrentBoard(), boardToCompore.getCurrentBoard()));
    }

    // all neighboring boards
    public Iterable<Board> neighbors() {
        Stack<Board> neighbors = new Stack<Board>();

        boolean neighborAllowedAbove = false;
        boolean neighborALlowedBelow = false;
        boolean neighborAllowedLeft = false;
        boolean neighborAllowedRight = false;

        if (missingTileX >= 0 && missingTileX < dimension() - 1) neighborALlowedBelow = true;
        if (missingTileX <= dimension() - 1 && missingTileX > 0) neighborAllowedAbove = true;
        if (missingTileY >= 0 && missingTileY < dimension() - 1) neighborAllowedRight = true;
        if (missingTileY > 0 && missingTileY <= dimension() - 1) neighborAllowedLeft = true;

        if (neighborAllowedAbove) {
            // exchange missing tile with tile directly above it:
            int[][] neighbor = cloneArray(currentBoard);
            // move zero tile up <=> move tile above the zero tile down
            neighbor[missingTileX][missingTileY] = neighbor[missingTileX - 1][missingTileY];
            neighbor[missingTileX - 1][missingTileY] = 0;

            Board neighborBoard = new Board(neighbor);
            if (findGoalRow(currentBoard[missingTileX - 1][missingTileY]) == (missingTileX - 1)) {
                neighborBoard.cachedManhattanDistance = manhattan() + 1;
            }
            else if (findGoalRow(currentBoard[missingTileX - 1][missingTileY]) > (missingTileX
                    - 1)) {
                neighborBoard.cachedManhattanDistance = manhattan() - 1;
            }
            else neighborBoard.cachedManhattanDistance = manhattan() + 1;
            neighborBoard.missingTileX = missingTileX - 1;
            neighborBoard.missingTileY = missingTileY;
            neighbors.push(new Board(neighbor));
        }

        if (neighborALlowedBelow) {
            // exchange missing tile with tile directly below it:
            int[][] neighbor = cloneArray(currentBoard);
            // move zero tile down <=> move tile below the zero tile up
            neighbor[missingTileX][missingTileY] = neighbor[missingTileX + 1][missingTileY];
            neighbor[missingTileX + 1][missingTileY] = 0;

            Board neighborBoard = new Board(neighbor);
            if (findGoalRow(currentBoard[missingTileX + 1][missingTileY]) == (missingTileX + 1)) {
                neighborBoard.cachedManhattanDistance = manhattan() + 1;
            }
            else if (findGoalRow(currentBoard[missingTileX + 1][missingTileY]) > (missingTileX
                    + 1)) {
                neighborBoard.cachedManhattanDistance = manhattan() + 1;
            }
            else neighborBoard.cachedManhattanDistance = manhattan() - 1;
            neighborBoard.missingTileX = missingTileX + 1;
            neighborBoard.missingTileY = missingTileY;
            neighbors.push(new Board(neighbor));
        }

        if (neighborAllowedLeft) {
            // exchange missing tile with tile directly left of it:
            int[][] neighbor = cloneArray(currentBoard);
            // move zero tile to the left <=> left tile moves right
            neighbor[missingTileX][missingTileY] = neighbor[missingTileX][missingTileY - 1];
            neighbor[missingTileX][missingTileY - 1] = 0;

            Board neighborBoard = new Board(neighbor);
            if (findGoalCol(currentBoard[missingTileX][missingTileY - 1]) == (missingTileY - 1)) {
                neighborBoard.cachedManhattanDistance = manhattan() + 1;
            }
            else if (findGoalCol(currentBoard[missingTileX][missingTileY - 1]) > (missingTileY
                    - 1)) {
                neighborBoard.cachedManhattanDistance = manhattan() - 1;
            }
            else neighborBoard.cachedManhattanDistance = manhattan() + 1;
            neighborBoard.missingTileX = missingTileX;
            neighborBoard.missingTileY = missingTileY - 1;
            neighbors.push(new Board(neighbor));
        }

        if (neighborAllowedRight) {
            // exchange missing tile with tile directly to right:
            int[][] neighbor = cloneArray(currentBoard);
            // missing tile moves right <=> right tile moves left
            neighbor[missingTileX][missingTileY] = neighbor[missingTileX][missingTileY + 1];
            neighbor[missingTileX][missingTileY + 1] = 0;

            Board neighborBoard = new Board(neighbor);
            if (findGoalCol(currentBoard[missingTileX][missingTileY + 1]) == (missingTileY + 1)) {
                neighborBoard.cachedManhattanDistance = manhattan() + 1;
            }
            else if (findGoalCol(currentBoard[missingTileX][missingTileY + 1]) > (missingTileY
                    + 1)) {
                neighborBoard.cachedManhattanDistance = manhattan() + 1;
            }
            else neighborBoard.cachedManhattanDistance = manhattan() - 1;
            neighbors.push(new Board(neighbor));
            neighborBoard.missingTileX = missingTileX;
            neighborBoard.missingTileY = missingTileY + 1;
        }

        return neighbors;
    }

    private int findGoalRow(int displacedNumber) {
        // find the goalRow and goalColumn for our displacedNumber.
        if (displacedNumber % dimension() == 0) {
            return displacedNumber / dimension() - 1;
        }
        // for every other position
        else {
            return displacedNumber / dimension();
        }
    }

    private int findGoalCol(int displacedNumber) {
        // find the goalRow and goalColumn for our displacedNumber.
        if (displacedNumber % dimension() == 0) {
            return dimension() - 1;
        }
        // for every other position
        else {
            return (displacedNumber % dimension()) - 1;
        }
    }

    // a board that is obtained by exchanging any pair of tiles
    public Board twin() {
        int[][] twin = cloneArray(currentBoard);
        int i;
        if (missingTileX == 0) i = 1;
        else i = 0;
        int temp = twin[i][0];
        twin[i][0] = twin[i][1];
        twin[i][1] = temp;
        return new Board(twin);
    }

    // unit testing (ungraded)
    public static void main(String[] args) {
    }

    private int[][] getCurrentBoard() {
        return currentBoard;
    }

    private void findMissingTile() {
        if (missingTileX == -1) {
            for (int i = 0; i < currentBoard.length; i++) {
                for (int j = 0; j < currentBoard.length; j++) {
                    if (currentBoard[i][j] == 0) {
                        missingTileX = i;
                        missingTileY = j;
                    }
                }
            }
        }
    }

    private int[][] cloneArray(int[][] arrayToCopy) {
        int[][] neighbor = new int[arrayToCopy.length][];
        for (int i = 0; i < arrayToCopy.length; i++) {
            neighbor[i] = arrayToCopy[i].clone();
        }
        return neighbor;
    }

    private int calcManhattanDistance(int displacedNumber, int currentRow, int currentColumn) {
        int goalRow;
        int goalColumn;

        goalRow = findGoalRow(displacedNumber);
        goalColumn = findGoalCol(displacedNumber);

        int distanceFromGoalRow = Math.abs(currentRow - goalRow);
        int distanceFromGoalColumn = Math.abs(currentColumn - goalColumn);

        return distanceFromGoalColumn + distanceFromGoalRow;
    }
}
