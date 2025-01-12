import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * The model for a maze.  A maze consists of a grid of cells.  Each
 * cell is either a wall or open space.  One of the (open space) cells is 
 * the start location, and another is the goal.  Mazes can be loaded
 * from text files.  The text file format is simply a set of lines,
 * each line representing a row of cells.  Each cell is a single 
 * character: '#' represents a wall, ' ' represents open space,
 * 'o' represents the starting point, and '*' represents the goal.
 *
 */
public class Maze {
    private int rows;
    private int cols;
    private char[][] maze;
    private int goalRow, goalCol;
    private int startRow, startCol;

    /**
     * Creates a new Maze by loading data from a file.
     *
     * @param fname the name of the file to load the maze data from.
     * @throws IOException if an error occurs during file reading.
     */
    public Maze(String fname) throws IOException {
        loadMaze(fname);
    }

    /**
     * Returns the total number of rows in this Maze.
     *
     * @return the number of rows in this Maze.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the total number of columns in this Maze.
     *
     * @return the number of columns in this Maze.
     */
    public int getCols() {
        return cols;
    }

    /**
     * Returns the row number of the start location.
     *
     * @return the row of the start location.
     */
    public int getStartRow() { return startRow; }

    /**
     * Returns the column number of the start location.
     *
     * @return the column of the start location.
     */
    public int getStartCol() { return startCol; }

    /**
     * Returns the row number of the goal location.
     *
     * @return the row of the goal location.
     */
    public int getGoalRow() { return goalRow; }

    /**
     * Returns the column number of the goal location.
     *
     * @return the column of the goal location.
     */
    public int getGoalCol() { return goalCol; }

    public char[][] getArray() {
        char[][] result = new char[this.rows][0];
        for( int r = 0; r < result.length; r++ ) {
            result[r] = Arrays.copyOf(this.maze[r], this.maze[r].length);
        }
        return result;
    }

    private void loadMaze(String fileName) throws IOException {
        Scanner fScan = new Scanner( new File(fileName) );

        ArrayList<String> lines = new ArrayList<String>();
        while(fScan.hasNextLine()) {
            String line = fScan.nextLine().trim();
            if( line.length() > 0 )
                lines.add(line);
        }

        if( lines.size() < 1 ) throw new IOException("No maze found in file.");

        cols = lines.get(0).length();
        if( lines.get(lines.size() - 1).length() != cols) {
            lines.remove(lines.size() - 1);
        }

        rows = lines.size();
        maze = new char[rows][cols];
        for( int r = 0; r < rows; r++ ) {
            String rowStr = lines.get(r);
            if(rowStr.length() != cols)
                throw new IOException("Found a row that is not the same length.");
            for( int c = 0 ; c < cols; c++ ) {
                char cell = rowStr.charAt(c);
                if( cell == 'o') {
                    this.startRow = r;
                    this.startCol = c;
                    cell = ' ';
                } else if( cell == '*' ) {
                    this.goalRow = r;
                    this.goalCol = c;
                    cell = ' ';
                }
                if( cell == ' ' || cell == '#')
                    maze[r][c] = cell;
                else
                    throw new IOException("Invalid maze character: " + cell);
            }
        }
    }
}
