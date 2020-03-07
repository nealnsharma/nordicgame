package tablut;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Formatter;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static tablut.Move.ROOK_MOVES;
import static tablut.Piece.*;
import static tablut.Square.*;

import static tablut.Move.mv;


/** The state of a Tablut Game.
 *  @author neal sharma
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares. */
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
        _turn = model._turn;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                _board[i][j] = model.get(j, i);
            }
        }
    }

    /** Clears the board to the initial position. */
    void init() {
        _winner = null;
        _turn = BLACK;
        _board = new Piece[SIZE][SIZE];
        _moveLim = Integer.MAX_VALUE;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                _board[i][j] = EMPTY;
            }
        }
        _board[THRONE.row()][THRONE.col()] = KING;
        for (Square sq: INITIAL_DEFENDERS) {
            _board[sq.row()][sq.col()] = WHITE;
        }
        for (Square sq: INITIAL_ATTACKERS) {
            _board[sq.row()][sq.col()] = BLACK;
        }
    }

    /** Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     *         @param n something */
    void setMoveLimit(int n) {
        if (2 * n <= moveCount()) {
            throw Utils.error("Unable to set limit.");
        }

        _moveLim = n;
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

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        if (_encodedSet.contains(encodedBoard())) {
            _repeated = true;
            _winner = _turn;
        }
        _encodedSet.add(encodedBoard());
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (_board[i][j] == KING) {
                    return sq(j, i);
                }
            }
        }
        return null;
    }
    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        if (!(0 <= col && 0 <= row && row <= 9 && col <= 9)) {
            throw Utils.error("Contents out of bounds");
        }
        return _board[row][col];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(row - '1', col - 'a');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        _board[s.row()][s.col()] = p;
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        if (!from.isRookMove(to)) {
            return false;
        }
        int direction = from.direction(to);
        int distance = max(abs(from.row() - to.row()),
                abs(from.col() - to.col()));
        for (int steps = 1; steps <= distance; steps++) {
            Square square = from.rookMove(direction, steps);
            if (get(square) != EMPTY) {
                return false;
            }
        }
        return true;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        if (!isLegal(from) || !isUnblockedMove(from, to)) {
            return false;
        }
        if (get(from) != KING) {
            return to != THRONE;
        }
        return true;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        put(get(from), to);
        put(EMPTY, from);
        ifCapture(to);


        if (kingPosition() != null && kingPosition().isEdge()) {
            _winner = WHITE;
        } else if (kingPosition() == null) {
            _winner = BLACK;
        } else if (legalMoves(_turn.opponent()).isEmpty()) {
            _winner = _turn;
        } else {
            _moveCount++;
            if (_moveCount >= _moveLim) {
                _winner = _turn.opponent();
            }
        }
        _turn = _turn.opponent();
        checkRepeated();
    }

    /** This function checks all capture conditions and makes sure
     TO does get captured if it meets a condition. */
    void ifCapture(Square to) {
        /**if (kingPosition().isEdge())
                return; */
        for (int i = 0; i <= 3; i++) {
            if (ROOK_MOVES[to.index()][i].size() > 1) {
                Square other = ROOK_MOVES[to.index()][i].get(1).to();
                if (exists(other.col(), other.row())) {
                    if (get(other.between(to)) == _turn.opponent()) {
                        if (get(other).side() == _turn) {
                            capture(to, other);
                        }
                        if (other == THRONE && get(THRONE) == EMPTY) {
                            capture(to, other);
                        }
                        if (_turn == BLACK && other == THRONE
                                && get(THRONE) == KING
                                && throneWhiteHostile()) {
                            capture(to, other);
                        }
                    } else if (_turn == BLACK
                            && get(other.between(to)) == KING) {
                        if (Arrays.asList(INITIAL_DEFENDERS)
                                .contains(kingPosition())
                                || kingPosition() == THRONE) {
                            boolean flag = true;
                            for (int j = 0; j <= 3; j++) {
                                Square s = ROOK_MOVES[kingPosition()
                                        .index()][j].get(0).to();
                                if (s != THRONE) {
                                    if (get(s) != BLACK) {
                                        flag = false;
                                    }
                                }
                            }
                            if (flag) {
                                capture(to, other);
                            }
                        } else {
                            if (get(other).side() == _turn) {
                                capture(to, other);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Determines whether the throne is hostile to white squares.
     *  @return something*/
    boolean throneWhiteHostile() {
        int i = 0;
        for (Square def : INITIAL_DEFENDERS) {
            if (get(def) == BLACK) {
                i++;
            }
        }
        if (i == 3) {
            return true;
        }
        return false;
    }


    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        Square square = sq0.between(sq2);
        put(EMPTY, square);
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            int last = _encodedSet.size() - 1;
            String prev = _encodedSet.get(last);

            char t = prev.charAt(0);
            _turn  = charToPiece(t);

            for (int i = 1; i < prev.length(); i++) {
                Square s = SQUARE_LIST.get(i - 1);
                _board[s.row()][s.col()] = charToPiece(prev.charAt(i));
            }
            _moveCount--;
        }
    }

    /** Returns piece from toString char. @param c */
    Piece charToPiece(char c) {
        if (c == '-') {
            return EMPTY;
        } else if (c == 'K') {
            return KING;
        } else if (c == 'W') {
            return WHITE;
        } else {
            return BLACK;
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        if (!_repeated) {
            _encodedSet.remove(_encodedSet.size() - 1);
        }
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
        _encodedSet.clear();
        _moveCount = 0;
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        ArrayList<Move> moves = new ArrayList<>();
        for (Square s : pieceLocations(side)) {
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    Move move = mv(s, sq(j, i));
                    if (move != null && isUnblockedMove(move.from(),
                            move.to())) {
                        moves.add(move);
                    }
                }
            }
        }
        return moves;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        if (legalMoves(side).size() > 0) {
            return true;
        }
        return false;
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
        HashSet<Square> locations = new HashSet<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (_board[i][j] == side) {
                    locations.add(sq(j, i));
                } else if (_board[i][j] == KING
                        && side == WHITE) {
                    locations.add(sq(j, i));
                }
            }
        }
        return locations;
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
    /** Move limit. **/
    private int _moveLim;
    /** Board array. **/
    private Piece[][] _board;
    /** Encoded list of boards as strings. **/
    private ArrayList<String> _encodedSet = new ArrayList<>();
}
