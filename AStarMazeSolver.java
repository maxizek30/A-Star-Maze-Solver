import java.awt.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class AStarMazeSolver implements MazeSolver {
    private MazeGUI gui;

    public AStarMazeSolver() {
        gui = new MazeGUI(this);
    }

    public void solve(char[][] maze, int startR, int startC, int endR, int endC) {
        Long startTime = System.currentTimeMillis();
        boolean solvable = false;
        int height = maze.length;
        int width = maze[0].length;
        Set<String> closedSet = new HashSet<>();
        Comparator<Cell> cellComparator = Comparator.comparingInt(Cell::getF);

        PriorityQueue<Cell> OpenSet = new PriorityQueue<>(cellComparator);
        OpenSet.add(new Cell(startC, startR, null, endC, endR, 0));
        Cell currCell = null;
        while (!OpenSet.isEmpty()) {
            //grab the newest cell from the queue
            currCell = OpenSet.remove();
            int cx = currCell.getX();
            int cy = currCell.getY();
            //color the maze
            maze[cy][cx] = '@';

            //look around and add surrounding cells to pq
            int[][] moves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] m : moves) {
                int nx = cx + m[0];
                int ny = cy + m[1];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (maze[ny][nx] == ' ') {
                        int stepCost = 1; // or whatever the cost is for each step
                        Cell neighbor = new Cell(nx, ny, currCell, endC, endR, stepCost);
                        OpenSet.add(neighbor);
                    }
                }
            }
            if (cx == endC && cy == endR) {
                solvable = true;
                break;
            }

        }
        //color the optimal path
        while(currCell != null) {
            int cx = currCell.getX();
            int cy  = currCell.getY();
            maze[cy][cx] = '%';
            currCell = currCell.getParent();

        }
        if (!solvable) {
            System.out.println("not solvable");
        }
        gui.drawMaze(maze);
        Long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);

    }
    private static class Cell {
        private int x, y;
        private Cell parent;
        // cost so far (distance from the start)
        private int g;
        // heuristic estimate to the end
        private int h;



        public Cell(int x, int y, Cell parent, int endX, int endY, int gFromParent) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            // If parent is not null, g = parent's g + 1 (assuming uniform step cost)
            // Otherwise, if this is the start cell, g = 0
            if (parent == null) {
                this.g = 0;
            } else {
                this.g = parent.getG() + gFromParent;
            }

            // h = manhattan distance to goal
            this.h = Math.abs(x - endX) + Math.abs(y - endY);
        }
        // getters
        public int getF() {
            return g + h;
        }
        public int getG() {
            return g;
        }
        public int getH() {
            return h;
        }
        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Cell getParent() {
            return parent;
        }
    }
}
