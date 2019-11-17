package tablut;

import ucb.gui2.Pad;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.util.concurrent.ArrayBlockingQueue;
import java.awt.event.MouseEvent;
import static tablut.Piece.*;
import static tablut.Square.sq;
import java.util.ArrayList;

/** A widget that displays a Tablut game.
 *  @author Michael Chien
 */
class BoardWidget extends Pad {

    /* Parameters controlling sizes, speeds, colors, and fonts. */

    /** Squares on each side of the board. */
    static final int SIZE = Board.SIZE;

    /** Colors of empty squares, pieces, grid lines, and boundaries. */
    static final Color
        SQUARE_COLOR = new Color(238, 207, 161),
        THRONE_COLOR = new Color(180, 255, 180),
        ADJACENT_THRONE_COLOR = new Color(200, 220, 200),
        CLICKED_SQUARE_COLOR = new Color(255, 255, 100),
        GRID_LINE_COLOR = Color.black,
        WHITE_COLOR = Color.white,
        BLACK_COLOR = Color.black;

    /** Margins. */
    static final int
        OFFSET = 2,
        MARGIN = 16;
    /** font for king. */
    static final int
        KINGFONT = 20;
    /** Side of single square and of board (in pixels). */
    static final int
        SQUARE_SIDE = 30,
        BOARD_SIDE = SQUARE_SIDE * SIZE + 2 * OFFSET + MARGIN;

    /** The font in which to render the "K" in the king. */
    static final Font KING_FONT = new Font("Serif", Font.BOLD, 18);
    /** The font for labeling rows and columns. */
    static final Font ROW_COL_FONT = new Font("SanSerif", Font.PLAIN, 10);

    /** Squares adjacent to the throne. */
    static final Square[] ADJACENT_THRONE = {
        Board.NTHRONE, Board.ETHRONE, Board.STHRONE, Board.WTHRONE
    };
    /** Scaling factor for letters a-i. */
    static final int LETTERS = 40;
    /** Scaling factor for numbers 1-9. */
    static final int NUMBERS = 20;


    /** A graphical representation of a Tablut board that sends commands
     *  derived from mouse clicks to COMMANDS.  */
    BoardWidget(ArrayBlockingQueue<String> commands) {
        _commands = commands;
        clicked = new ArrayList<>();
        setMouseHandler("click", this::mouseClicked);
        setPreferredSize(BOARD_SIDE, BOARD_SIDE);
        _acceptingMoves = false;
    }

    /** Draw the bare board G.  */
    private void drawGrid(Graphics2D g) {
        g.setColor(SQUARE_COLOR);
        g.fillRect(0, 0, BOARD_SIDE, BOARD_SIDE);
        g.setColor(THRONE_COLOR);
        g.fillRect(cx(Board.THRONE), cy(Board.THRONE),
                   SQUARE_SIDE, SQUARE_SIDE);
        g.setColor(ADJACENT_THRONE_COLOR);
        g.fillRect(cx(Board.NTHRONE), cy(Board.NTHRONE),
                SQUARE_SIDE, SQUARE_SIDE);
        g.fillRect(cx(Board.STHRONE), cy(Board.STHRONE),
                SQUARE_SIDE, SQUARE_SIDE);
        g.fillRect(cx(Board.WTHRONE), cy(Board.WTHRONE),
                SQUARE_SIDE, SQUARE_SIDE);
        g.fillRect(cx(Board.ETHRONE), cy(Board.ETHRONE),
                SQUARE_SIDE, SQUARE_SIDE);

        g.setColor(GRID_LINE_COLOR);
        for (int k = 0; k <= SIZE; k += 1) {
            g.drawLine(cx(0), cy(k - 1), cx(SIZE), cy(k - 1));
            g.drawLine(cx(k), cy(-1), cx(k), cy(SIZE - 1));
        }
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        drawGrid(g);
        drawLetters(g);
        if (!clicked.isEmpty()) {
            for (Square s : clicked) {
                g.setColor(CLICKED_SQUARE_COLOR);
                g.fillRect(cx(s), cy(s), SQUARE_SIDE, SQUARE_SIDE);
            }
        }
        Square.SQUARE_LIST.iterator().forEachRemaining(s -> drawPiece(g, s));
    }

    /** @param g Draw the col and row of the board. */
    private void drawLetters(Graphics2D g) {
        String letters = "abcdefghi";
        String numbers = "123456789";
        int row = 0;
        int col = 0;
        g.setColor(BLACK_COLOR);
        g.setFont(ROW_COL_FONT);
        for (int i = 0; i < SIZE; i++) {
            g.drawString("" + letters.charAt(i), cx(Square.sq(i, row)) + 5,
                    cy(Square.sq(i, row)) + LETTERS);
            g.drawString("" + numbers.charAt(i), cx(Square.sq(col, i)) - 12,
                    cy(Square.sq(col, i)) + NUMBERS);
        }
    }
    /** Draw the contents of S on G. */
    private void drawPiece(Graphics2D g, Square s) {
        if (_board.get(s) == WHITE) {
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(2.0f,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(cx(s.col()) + 2, cy(s.row()) + 2,
                    SQUARE_SIDE - 5, SQUARE_SIDE - 5);
            g.setColor(WHITE_COLOR);
            g.fillOval(cx(s.col()) + 2, cy(s.row()) + 2,
                    SQUARE_SIDE - 5, SQUARE_SIDE - 5);

        } else if (_board.get(s) == BLACK) {
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(2.0f,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(cx(s.col()) + 2, cy(s.row()) + 2,
                    SQUARE_SIDE - 5, SQUARE_SIDE - 5);
            g.setColor(BLACK_COLOR);
            g.fillOval(cx(s.col()) + 2, cy(s.row()) + 2,
                    SQUARE_SIDE - 5, SQUARE_SIDE - 5);
        } else if (_board.get(s) == KING) {
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(2.0f,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(cx(s.col()) + 2, cy(s.row()) + 2,
                    SQUARE_SIDE - 5, SQUARE_SIDE - 5);
            g.setColor(WHITE_COLOR);
            g.fillOval(cx(s.col()) + 2, cy(s.row()) + 2,
                    SQUARE_SIDE - 5, SQUARE_SIDE - 5);
            g.setColor(Color.RED);
            g.setFont(KING_FONT);
            g.drawString("K", cx(s.col()) + 8, cy(s.row()) + KINGFONT);
        }
    }

    /** Handle a click on S. */
    private void click(Square s) {
        if (!clicked.contains(s)) {
            clicked.add(s);
        }
        String command = "";
        if (clicked.size() == 2) {
            if (clicked.get(0).col() == clicked.get(1).col()) {
                command += clicked.get(0).toString() + "-";
                command += clicked.get(1).toString().charAt(1);
                clicked.clear();
                _commands.offer(command);
            } else if (clicked.get(0).row() == clicked.get(1).row()) {
                command += clicked.get(0).toString() + "-";
                command += clicked.get(1).toString().charAt(0);
                clicked.clear();
                _commands.offer(command);
            }
            clicked.clear();
        }
        repaint();
    }
    /** Handle mouse click event E. */
    private synchronized void mouseClicked(String unused, MouseEvent e) {
        int xpos = e.getX(), ypos = e.getY();
        int x = (xpos - OFFSET - MARGIN) / SQUARE_SIDE,
            y = (OFFSET - ypos) / SQUARE_SIDE + SIZE - 1;
        if (_acceptingMoves
            && x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
            click(sq(x, y));
        }
    }

    /** Revise the displayed board according to BOARD. */
    synchronized void update(Board board) {
        _board.copy(board);
        repaint();
    }

    /** Turn on move collection iff COLLECTING, and clear any current
     *  partial selection.  When move collection is off, ignore clicks on
     *  the board. */
    void setMoveCollection(boolean collecting) {
        _acceptingMoves = collecting;
        repaint();
    }

    /** Return x-pixel coordinate of the left corners of column X
     *  relative to the upper-left corner of the board. */
    private int cx(int x) {
        return x * SQUARE_SIDE + OFFSET + MARGIN;
    }

    /** Return y-pixel coordinate of the upper corners of row Y
     *  relative to the upper-left corner of the board. */
    private int cy(int y) {
        return (SIZE - y - 1) * SQUARE_SIDE + OFFSET;
    }

    /** Return x-pixel coordinate of the left corner of S
     *  relative to the upper-left corner of the board. */
    private int cx(Square s) {
        return cx(s.col());
    }

    /** Return y-pixel coordinate of the upper corner of S
     *  relative to the upper-left corner of the board. */
    private int cy(Square s) {
        return cy(s.row());
    }

    /** Queue on which to post move commands (from mouse clicks). */
    private ArrayBlockingQueue<String> _commands;
    /** Board being displayed. */
    private final Board _board = new Board();

    /** True iff accepting moves from user. */
    private boolean _acceptingMoves;
    /** ArrayList of squares. */
    private ArrayList<Square> clicked;
}
