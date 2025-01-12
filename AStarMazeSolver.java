import java.util.Comparator;
import java.util.PriorityQueue;

public class AStarMazeSolver implements MazeSolver {
    private MazeGUI gui;

    public AStarMazeSolver() {
        gui = new MazeGUI(this);
    }
    public void solve(char[][] maze, int startR, int startC, int endR, int endC) {
        Comparator<Cell> cellComparator = (p1, p2) -> Integer.compare(calcManhattanDistance(p1.getX(), endC, p1.getY(), endR), calcManhattanDistance(p2.getX(), endC, p2.getY(), endR));
        PriorityQueue<Cell> pq = new PriorityQueue<>(cellComparator);
        

        gui.drawMaze(maze);
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted!");
        }

    }
    private int calcManhattanDistance(int x1, int x2, int y1, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 + y2);
    }


}