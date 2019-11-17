package tablut;

import java.util.HashSet;
import java.util.List;
import static tablut.Board.SIZE;
import static tablut.Board.THRONE;
import static tablut.Piece.*;



/** A Player that automatically generates moves.
 *  @author Michael Chien
 */
class AI extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A position-score magnitude indicating a forced win in a subsequent
     * move.  This differs from WINNING_VALUE to avoid putting off wins.
     */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;
    /** Blackworth. */
    private static final int PIECEWORTH = 50;

    /**
     * A new AI with no piece or controller (intended to produce
     * a template).
     */
    AI() {
        this(null, null);
    }

    /**
     * A new AI playing PIECE under control of CONTROLLER.
     */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;
        if (myPiece() == BLACK) {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }
        if (sense == 1) {
            int bestsofar = Integer.MIN_VALUE;
            List<Move> white = board.legalMoves(WHITE);
            int a = white.size();
            for (Move whitemove : white) {
                board.makeMove(whitemove);
                int response = findMove(board, depth - 1,
                        false, -1, alpha, beta);
                board.undo();
                if (response >= bestsofar) {
                    bestsofar = response;
                    if (saveMove) {
                        _lastFoundMove = whitemove;
                    }
                    alpha = Math.max(alpha, response);
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            return bestsofar;
        } else {
            int bestsofar = Integer.MAX_VALUE;
            List<Move> black = board.legalMoves(BLACK);
            for (Move blackmove : black) {
                board.makeMove(blackmove);
                int response = findMove(board, depth - 1,
                        false, 1, alpha, beta);
                board.undo();
                if (response <= bestsofar) {
                    bestsofar = response;
                    if (saveMove) {
                        _lastFoundMove = blackmove;
                    }
                    beta = Math.min(beta, response);
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            return bestsofar;
        }
    }

    /** @param board
     * Return a heuristically determined maximum search depth
     * based on characteristics of BOARD.
     */
    private static int maxDepth(Board board) {
        return 1;
    }

    /** @param board
     * Return a heuristic value for BOARD.
     */
    private int staticScore(Board board) {
        HashSet<Square> white = board.pieceLocations(WHITE);
        HashSet<Square> black = board.pieceLocations(BLACK);
        int score = 0;
        score += (white.size() * PIECEWORTH) - PIECEWORTH;
        score -= black.size() * -PIECEWORTH;
        if (board.kingPosition() != null) {
            if (board.kingPosition().isEdge()) {
                return WINNING_VALUE;
            }
        }
        if (board.kingPosition() != THRONE && board.kingPosition() == null) {
            return -WINNING_VALUE;
        }
        return score;
    }

}

