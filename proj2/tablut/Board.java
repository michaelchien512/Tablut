package tablut;
import java.util.Formatter;
import java.util.Stack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import static tablut.Piece.*;
import static tablut.Square.*;
import static tablut.Move.mv;
import java.util.List;


/** The state of a Tablut Game.
 *  @author Michael Chien
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }
    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        this._board = new Piece[SIZE][SIZE];
        for (int i = 0; i < _board.length; i++) {
            for (int j = 0; j < _board[i].length; j++) {
                this._board[i][j] = model._board[i][j];
            }
        }
        for (Square s : INITIAL_DEFENDERS) {
            this._board[s.row()][s.col()] = model._board[s.row()][s.col()];
        }
        for (Square s : INITIAL_ATTACKERS) {
            this._board[s.row()][s.col()] = model._board[s.row()][s.col()];
        }
        this._board[THRONE.row()][THRONE.col()] =
                model._board[THRONE.row()][THRONE.col()];
        this._moves = model.moves();
        this._turn = model.turn();
        this._winner = model._winner;
        this._moveCount = model.moveCount();
        this._bpositions = model.bpositions();
        this.capturedmap = model.capturedmap;
        this.locs = model.locs;

    }

    /** Clears the board to the initial position. */
    void init() {
        _limit = Integer.MAX_VALUE;
        _turn = BLACK;
        _winner = null;
        _moveCount = 0;
        _bpositions = new ArrayList<>();
        _moves = new Stack<>();
        _board = new Piece[SIZE][SIZE];
        capturedmap = new HashMap<>();
        locs = new ArrayList<>();
        for (int i = 0; i < _board.length; i++) {
            for (int j = 0; j < _board[i].length; j++) {
                _board[i][j] = EMPTY;
            }
        }
        for (Square s : INITIAL_ATTACKERS) {
            _board[s.row()][s.col()] = BLACK;
        }
        for (Square s : INITIAL_DEFENDERS) {
            _board[s.row()][s.col()] = WHITE;
        }
        _board[THRONE.row()][THRONE.col()] = KING;
        _bpositions.add(encodedBoard());

    }

    /** @param n is the move limit Set the move limit to n.  */
    void setMoveLimit(int n) {
        _limit = n;
        if (2 * _limit <= moveCount()) {
            throw new IllegalArgumentException("Over movecount!");
        }
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }
    /** Returns true iff I captured a piece. */
    boolean captured() {
        return _captured;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        for (int i = 0; i < _bpositions.size(); i++) {
            for (int j = i + 1; j < _bpositions.size(); j++) {
                if (_bpositions.get(i).equals(_bpositions.get(j))) {
                    _repeated = true;
                    _winner = _turn;
                }
            }
        }
    }
    /** @return My movestack. */
    Stack<Move> moves() {
        return _moves;
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {
        for (int i = 0; i < _board.length; i++) {
            for (int j = 0; j < _board[i].length; j++) {
                if (get(i, j) == KING) {
                    return Square.sq(i, j);
                }

            }
        }
        return null;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return  this._board[s.row()][s.col()];
    }
    /** return boardpositions. */
    ArrayList<String> bpositions() {
        return _bpositions;
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return get(Square.sq(col, row));
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        _board[s.row()][s.col()] = p;
    }

    /** @param m Set piece to s and record for undoing
     * @param p piece
     * @param s square*/
    final void revPut(Move m, Piece p, Square s) {
        _board[s.row()][s.col()] = p;
        locs.add(s);
        String string = _moveCount + " " + m.toString();
        if (capturedmap.containsKey(string)) {
            capturedmap.replace(string, locs);
        } else {
            capturedmap.put(_moveCount + " " + m.toString(), locs);
        }
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        Square copy;
        if (from.isRookMove(to)) {
            int dir = from.direction(to);
            if (dir == 0 || dir == 2) {
                for (int i = 1; i <= Math.abs(to.row() - from.row()); i++) {
                    copy = from.rookMove(dir, i);
                    if (get(copy) != EMPTY) {
                        return false;
                    }
                }
            } else if (dir == 1 || dir == 3) {
                for (int i = 1; i <= Math.abs(to.col() - from.col()); i++) {
                    copy = from.rookMove(dir, i);
                    if (get(copy) != EMPTY) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        if (exists(from.col(), from.row())) {
            if (get(from) == KING && turn() == WHITE) {
                return isUnblockedMove(from, to);
            }
            if (to == THRONE && get(from) != KING) {
                return false;
            }
            if (get(from) == turn() && get(to) == EMPTY) {
                return isUnblockedMove(from, to);
            }
        }
        return false;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        Move m = mv(from, to);
        _moves.push(m);
        if (moveCount() > _limit) {
            _winner = turn().opponent();
        }
        _moveCount += 1;
        if (get(from) == KING) {
            if (to.isEdge()) {
                put(KING, to);
                put(EMPTY, from);
                _winner = WHITE;
            } else {
                put(KING, to);
                put(EMPTY, from);
                _turn = KING;
                if (!partner(to).isEmpty() && !partner(to).contains(null)) {
                    for (Square s : partner(to)) {
                        capture(to, s);
                    }
                }
                _turn = BLACK;
            }
        } else {
            put(turn(), to);
            put(EMPTY, from);
            if (!partner(to).isEmpty()) {
                for (Square s : partner(to)) {
                    capture(to, s);
                }
            }
            if (_turn == BLACK) {
                if (to.adjacent(NTHRONE) || to.adjacent(STHRONE)
                        || to.adjacent(WTHRONE) || to.adjacent(ETHRONE)) {
                    specialCapture(to, THRONE);
                }
            }
            _turn = turn().opponent();
        }
        _bpositions.add(encodedBoard());
        checkRepeated();
    }
    /** @param current Return my partner to help me capture a piece */
    ArrayList<Square> partner(Square current) {
        ArrayList<Square> partners = new ArrayList<>();
        HashSet<Square> possiblesquares = pieceLocations(turn());
        for (int i = 0; i < 4; i++) {
            Square twoaway = current.rookMove(i, 2);
            if (possiblesquares.contains(twoaway)) {
                partners.add(twoaway);
            }
            if (twoaway == THRONE && get(twoaway) == EMPTY) {
                partners.add(twoaway);
            }
        }
        return partners;
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        Piece turn = this.turn();
        Square captured = sq0.between(sq2);
        Move m = _moves.get(_moves.size() - 1);
        if (turn == WHITE || turn == KING) {
            if (get(captured) == BLACK) {
                revPut(m, EMPTY, captured);
            }
        } else {
            if (get(captured) == KING) {
                if (captured == THRONE) {
                    if (get(NTHRONE) == BLACK && get(STHRONE) == BLACK
                            && get(ETHRONE) == BLACK && get(WTHRONE) == BLACK) {
                        revPut(m, EMPTY, THRONE);
                        _winner = BLACK;
                    } else {
                        return;
                    }
                } else if ((captured == NTHRONE || captured == STHRONE
                        || captured == WTHRONE || captured == ETHRONE
                        && get(THRONE) == EMPTY)) {
                    if (thrones()) {
                        revPut(m, EMPTY, captured);
                        _winner = BLACK;
                    }
                } else if (captured != THRONE || captured != NTHRONE
                        || captured != STHRONE
                        || captured != ETHRONE || captured != WTHRONE) {
                    revPut(m, EMPTY, captured);
                    _winner = BLACK;
                }
            } else if (get(captured) == WHITE) {
                revPut(m, EMPTY, captured);
            }
        }
    }

    /** @param throne use the throne.
     * @param black  Special case of capture. */
    private void specialCapture(Square black, Square throne) {
        int blackcount = 0;
        int whitecount = 0;
        Move m = _moves.get(_moves.size() - 1);
        for (int i = 0; i < _board.length; i++) {
            for (int j = 0; j < _board.length; j++) {
                if (throne.adjacent(Square.sq(i, j))
                        && get(Square.sq(i, j)) == BLACK) {
                    blackcount += 1;
                } else if (throne.adjacent(Square.sq(i, j))
                        && get(Square.sq(i, j)) == WHITE) {
                    whitecount += 1;
                }
                if (blackcount == 3 && whitecount == 1) {
                    Square captured = black.between(throne);
                    revPut(m, EMPTY, captured);

                }
            }
        }
    }
    /** @return  Capture if kings on a different throne.  */
    private boolean thrones() {
        Square k = kingPosition();
        int blackcount = 0;
        for (int i = 0; i < _board.length; i++) {
            for (int j = 0; j < _board.length; j++) {
                if (k.adjacent(Square.sq(i, j))
                        && get(Square.sq(i, j)) == BLACK) {
                    blackcount += 1;
                }
                if (get(THRONE) == EMPTY && blackcount == 3) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            _moveCount = _moveCount - 1;
            Move m = _moves.pop();
            Piece a = get(m.to());
            for (String move : capturedmap.keySet()) {
                ArrayList<Square> squares = capturedmap.get(move);
                for (Square s : squares) {
                    put(a, s);
                }
            }
            put(_turn.opponent(), m.from());
            put(EMPTY, m.to());
            _turn = turn().opponent();
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        checkRepeated();
        if (_repeated) {
            return;
        }
        _bpositions.remove(_bpositions.size() - 1);
        _repeated = false;
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
        while (_moves != null) {
            _moves.pop();
        }
        _bpositions.clear();
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        _legalmoves = new ArrayList<>();
        HashSet<Square> locations = pieceLocations(side);
        Piece temp = _turn;
        _turn = side;
        for (Square s : locations) {
            for (int i = 0; i < _board.length; i++) {
                for (int j = 0; j < _board[i].length; j++) {
                    if (exists(i, j)) {
                        if (isLegal(s, Square.sq(i, j))) {
                            _legalmoves.add(Move.mv(s, Square.sq(i, j)));
                        }
                    }
                }
            }
        }
        _turn = temp;
        if (_legalmoves.size() == 0) {
            _winner = _turn.opponent();
        }
        return _legalmoves;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        List<Move> check = legalMoves(side);
        return !check.isEmpty();
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> p = new HashSet<>();
        if (side == WHITE || side == KING) {
            if (kingPosition() != null) {
                p.add(kingPosition());
            }
        }
        for (int i = 0; i < _board.length; i++) {
            for (int j = 0; j < _board[i].length; j++) {
                if (get(i, j) == side) {
                    p.add(Square.sq(i, j));
                } else if (_turn == KING) {
                    if (get(i, j) == WHITE) {
                        p.add(Square.sq(i, j));
                    }
                }
            }
        }
        return p;
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;
    /** 2d array board with the pieces inside. */
    private Piece[][] _board;
    /** Stack of moves. */
    private Stack<Move> _moves;
    /** Arraylist for previous board positions. */
    private ArrayList<String> _bpositions;
    /** Legalmoves for one side. */
    private ArrayList<Move> _legalmoves;
    /** Hashmaps to store captured pieces. */
    private HashMap<String, ArrayList<Square>> capturedmap;
    /** Arraylist for captured pieces locations. */
    private ArrayList<Square> locs;
    /** True when I capture a piece. */
    private boolean _captured;
    /** MoveLimit. */
    private int _limit;
}
