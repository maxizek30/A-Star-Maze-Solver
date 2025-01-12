import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.lang.reflect.InvocationTargetException;

/**
 * A simple GUI for displaying and animating maze solvers.
 *
 * @author David Wolff
 */
@SuppressWarnings("serial")
public class MazeGUI extends JPanel {

    private MazeView mazeView;
    private JTextField mazeFileTF;
    private JButton loadButton;
    private JLabel infoLabel;
    private JButton startButton;
    private MazeSolver solver;

    /**
     * Creates and displays the Maze GUI.
     */
    public MazeGUI(MazeSolver slvr) {
        this.solver = slvr;
        buildGUI();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Set the text of the status label displayed at the bottom of
     * this window.
     *
     * @param text the text for the status bar.
     */
    public void setStatusText( String text ) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                infoLabel.setText(text);
            }
        });
    }

    private void buildGUI() {
        this.setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout( new BorderLayout() );
        ActionListener listener = new MazeGUIActionListener();

        mazeFileTF = new JTextField();
        mazeFileTF.addActionListener(listener);
        inputPanel.add(mazeFileTF, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        loadButton = new JButton("Load");
        loadButton.addActionListener(listener);
        buttonPanel.add( loadButton );

        startButton = new JButton("Start");
        startButton.setEnabled(false);
        startButton.addActionListener(listener);
        buttonPanel.add( startButton );

        inputPanel.add(buttonPanel, BorderLayout.EAST);
        this.add(inputPanel, BorderLayout.NORTH);

        mazeView = new MazeView();
        this.add(mazeView, BorderLayout.CENTER);
        JPanel infoPanel = new JPanel();
        this.infoLabel = new JLabel("Enter file name above.");
        infoPanel.add(infoLabel);
        this.add(infoPanel,BorderLayout.SOUTH);
    }

    /**
     * This will cause the maze view to be re-drawn.  It will use
     * the data in the parameter array to draw the maze.  It
     * recognizes the following characters:
     *   '#' - a wall
     *   ' ' - an open space
     *   '@' - an explored space
     *   '%' - an ideal path
     *
     * @param m the array containing the maze data.
     */
    public void drawMaze(char[][] m) {
        try {
            SwingUtilities.invokeAndWait( new Runnable() {
                public void run() {
                    mazeView.redrawMaze(m);
                }
            });
        } catch(Exception e ) {}
    }

    /**
     * This method will be called by the solver's thread when the solver has finished
     * executing.
     */
    private void solverFinished() {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                startButton.setEnabled(true);
                loadButton.setEnabled(true);
            }
        });
    }

    /**
     * The listener for the GUI's buttons and text field.
     *
     */
    private class MazeGUIActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if( e.getSource() == loadButton || e.getSource() == mazeFileTF ) {
                infoLabel.setText("Loading maze: " + mazeFileTF.getText());
                try {
                    mazeView.loadMaze(mazeFileTF.getText());
                    mazeView.drawMaze();
                    infoLabel.setText("Maze " + mazeFileTF.getText() + " loaded." );
                    startButton.setEnabled(true);
                } catch(IOException ex) {
                    String message = "Unable to load maze file: "
                            + ex.getMessage();
                    JOptionPane.showMessageDialog(MazeGUI.this, message);
                    infoLabel.setText(message);
                }
            }

            if( e.getSource() == startButton ) {
                startButton.setEnabled(false);
                loadButton.setEnabled(false);
                mazeView.startSolver();
            }
        }
    }

    private class MazeView extends JPanel {

        private BufferedImage img;
        private Maze originalMaze = null;

        // Width and height of maze view
        private static final int WIDTH = 600;
        private static final int HEIGHT = 600;

        // Extra space between cell boundary and start/goal marker
        private static final int BUFFER = 4;

        /**
         * Creates a "blank" MazeView.  The view is initially
         * black with a message in the center.
         */
        public MazeView() {
            this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
            img = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);
            Graphics g = img.getGraphics();
            g.setColor(Color.black);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setColor(Color.white);
            g.drawString("To load a maze, type in the file name, and click Load.",
                    WIDTH/2 - 200, HEIGHT/2);
        }

        /**
         * Draws the content for this MazeView.
         */
        public void paintComponent( Graphics g ) {
            super.paintComponent(g);

            g.drawImage(img, 0, 0, this);
        }

        public void loadMaze( String fileName ) throws IOException {
            originalMaze = new Maze(fileName);
        }

        /**
         * Initiates a redraw of the maze.  If the Maze is not set
         * (see setMaze) this does nothing.
         */
        public void redrawMaze(char[][] maze) {
            Graphics g = img.getGraphics();
            this.draw(g, maze);
            repaint();
        }

        public void drawMaze() {
            Graphics g = img.getGraphics();
            this.draw(g, originalMaze.getArray());
            repaint();
        }

        public void startSolver() {
            if( this.originalMaze == null ) return;
            Thread t = new Thread(new Runnable() {
                public void run() {
                    solver.solve(originalMaze.getArray(),originalMaze.getStartRow(),
                            originalMaze.getStartCol(),originalMaze.getGoalRow(),
                            originalMaze.getGoalCol());
                    solverFinished();
                }
            });
            t.start();
        }

        /**
         * Draws this Maze using the given Graphics object.
         *
         * @param g a Graphics object
         * @param w the width of the canvas in pixels.
         * @param h the height of the canvas in pixels.
         */
        public void draw(Graphics g, char[][] maze) {
            if( this.originalMaze == null ) return;

            int cols = maze[0].length;
            int rows = maze.length;
            double cellW = (double)WIDTH / cols;
            double cellH = (double)HEIGHT / rows;
            int cellWi = (int)Math.round(cellW);
            int cellHi = (int)Math.round(cellH);

            int x, y;
            g.setColor(Color.black);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setColor(Color.white);
            for( int r = 0; r < rows; r++ ) {
                y = (int)Math.floor(r * cellH);
                for( int c = 0; c < cols; c++ ) {
                    x = (int)Math.floor(c * cellW);
                    char cell = maze[r][c];
                    if( cell == '@' || cell == ' ' || cell == '%' ) {
                        if( cell == '@' )
                            g.setColor(Color.lightGray);
                        else if (cell == '%')
                            g.setColor(Color.CYAN);
                        else
                            g.setColor(Color.white);
                        g.fillRect(x, y, cellWi, cellHi);
                        g.setColor(Color.black);
                        g.drawRect(x,y,cellWi,cellHi);
                    }
                }
            }

            g.setColor(Color.green);
            x = (int)Math.round(cellW * originalMaze.getStartCol()) + BUFFER;
            y = (int)Math.round(cellH * originalMaze.getStartRow()) + BUFFER;
            g.fillOval(x, y, cellWi - 2 * BUFFER, cellHi - 2 * BUFFER );

            g.setColor(Color.red);
            x = (int)Math.round(cellW * originalMaze.getGoalCol()) + BUFFER;
            y = (int)Math.round(cellH * originalMaze.getGoalRow()) + BUFFER;
            g.fillOval(x, y, cellWi - 2 * BUFFER, cellHi - 2 * BUFFER );
        }
    }

}
