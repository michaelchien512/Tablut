package tablut;
import org.junit.Test;
import static org.junit.Assert.*;
import ucb.junit.textui;

import java.util.HashSet;

/** The suite of all JUnit tests for the enigma package.
 *  @author Michael Chien
 */
public class UnitTest {

    /**
     * Run the JUnit tests in this package. Add xxxTest.class entries to
     * the arguments of runClasses to run other JUnit tests.
     */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    @Test
    public void testFunctions() {
        Board board = new Board();
        board.put(Piece.BLACK, Square.sq(2, 1));
        assertEquals(Piece.BLACK, board.get(2, 1));
        assertEquals(Square.sq(4, 4), board.kingPosition());
        assertEquals(Piece.EMPTY, board.get(0, 0));
    }

    @Test
    public void testIsUnblocked() {
        Board board = new Board();
        assertEquals(false, board.isUnblockedMove(Square.sq(0, 2),
                Square.sq(0, 3)));
        assertEquals(true, board.isUnblockedMove(Square.sq(0, 1),
                Square.sq(0, 2)));
        assertEquals(false, board.isUnblockedMove(Square.sq(0, 3),
                Square.sq(4, 3)));
        assertEquals(true, board.isUnblockedMove(Square.sq(0, 2),
                Square.sq(3, 2)));
        assertEquals(false, board.isUnblockedMove(Square.sq(0, 8),
                Square.sq(0, 0)));
        assertEquals(true, board.isUnblockedMove(Square.sq(2, 3),
                Square.sq(2, 0)));
    }

    @Test
    public void testIsLegal() {
        Board board = new Board();
        assertEquals(false, board.isLegal(Square.sq(0, 0), Square.sq(0, 1)));
        assertEquals(true, board.isLegal(Square.sq(0, 3), Square.sq(1, 3)));
        assertEquals(false, board.isLegal(Square.sq(2, 4), Square.sq(2, 8)));
    }

    @Test
    public void testMakeMove() {
        Board board = new Board();
        board.makeMove(Square.sq(0, 3), Square.sq(3, 3));
        assertEquals(Piece.BLACK, board.get(Square.sq(3, 3)));
        assertEquals(1, board.moves().size());
        assertEquals(Piece.WHITE, board.turn());
        board.makeMove(Square.sq(2, 4), Square.sq(2, 8));
        assertEquals(Piece.WHITE, board.get(Square.sq(2, 8)));
        assertEquals(2, board.moves().size());
        assertEquals(Piece.BLACK, board.turn());
    }

    @Test
    public void testRepeated() {
        Board board = new Board();
        board.makeMove(Square.sq(8, 5), Square.sq(6, 5));
        board.makeMove(Square.sq(3, 4), Square.sq(3, 6));
        board.makeMove(Square.sq(6, 5), Square.sq(8, 5));
        board.makeMove(Square.sq(3, 6), Square.sq(3, 4));
        assertEquals(Piece.BLACK, board.winner());
    }

    @Test
    public void testUndo() {
        Board board = new Board();
        board.makeMove(Square.sq(8, 5), Square.sq(6, 5));
        board.makeMove(Square.sq(3, 4), Square.sq(3, 6));
        board.undo();
        assertEquals(Piece.EMPTY, board.get(Square.sq(3, 6)));
        assertEquals(Piece.WHITE, board.get(Square.sq(3, 4)));
        assertEquals(Piece.WHITE, board.turn());
        assertEquals(1, board.moveCount());
        assertEquals(2, board.bpositions().size());
        assertEquals(1, board.moves().size());
    }
    @Test
    public void testCapture() {
        Board board = new Board();
        board.makeMove(Square.sq(8, 3), Square.sq(8, 0));
        board.makeMove(Square.sq(5, 4), Square.sq(5, 7));
        board.makeMove(Square.sq(8, 0), Square.sq(8, 1));
        board.makeMove(Square.sq(6, 4), Square.sq(6, 6));
        board.makeMove(Square.sq(8, 5), Square.sq(8, 6));
        board.makeMove(Square.sq(4, 2), Square.sq(0, 2));
        board.makeMove(Square.sq(0, 5), Square.sq(0, 6));
        board.makeMove(Square.sq(0, 2), Square.sq(0, 1));
        board.makeMove(Square.sq(8, 6), Square.sq(7, 6));
        board.makeMove(Square.sq(0, 1), Square.sq(0, 0));
        board.makeMove(Square.sq(0, 6), Square.sq(3, 6));
        board.makeMove(Square.sq(0, 0), Square.sq(0, 1));
        board.makeMove(Square.sq(5, 0), Square.sq(5, 6));
        board.undo();
        assertEquals(1, 1);
    }
    @Test
    public void aiCapture() {
        AI ai = new AI();
        Board board = new Board();
        board.makeMove(Square.sq(8, 3), Square.sq(6, 3));
        HashSet<Square> a = board.pieceLocations(board.turn());
        board.makeMove(Square.sq(2, 4), Square.sq(2, 0));
        HashSet<Square> b = board.pieceLocations(board.turn());
        board.pieceLocations(board.turn());
    }
    @Test
    public void capture() {
        Board board = new Board();
        board.makeMove(Square.sq(8, 3), Square.sq(6, 3));
        board.makeMove(Square.sq(2, 4), Square.sq(2, 0));
        board.makeMove(Square.sq(8, 5), Square.sq(6, 5));
        HashSet<Square> a = board.pieceLocations(Piece.WHITE);
        assertEquals(Piece.EMPTY, board.get(Square.sq(6, 4)));
    }
}


