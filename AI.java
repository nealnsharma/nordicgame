package tablut;

import static java.lang.Math.*;

/** A Player that automatically generates moves.
 *  @author Neal Sharma
 */
class AI extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A position-score magnitude indicating a forced win in a subsequent
     *  move.  This differs from WINNING_VALUE to avoid putting off wins. */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
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

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;
        int sense = 1;
        if (myPiece() == Piece.BLACK) {
            sense = -1;
        } else {
            assert myPiece() == Piece.WHITE;
        }
        findMove(b, maxDepth(b), true, sense, -INFTY, INFTY);
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        int bestSoFar = 0; Move bestMove = null;
        if (sense == 1) {
            bestSoFar = -INFTY;
            if (board.winner() != null) {
                if (board.winner() == Piece.WHITE) {
                    return WINNING_VALUE;
                } else if (board.winner() == Piece.BLACK) {
                    return -WINNING_VALUE;
                }
            }
            if (depth == 0) {
                return simpleFindMax(board, alpha, beta);
            } else {
                for (Move move: board.legalMoves(myPiece())) {
                    Board copy = new Board();
                    copy.copy(board); copy.makeMove(move);
                    int response =
                            findMove(copy, depth - 1, false, -1, alpha, beta);
                    if (response >= bestSoFar) {
                        bestSoFar = response; bestMove = move;
                        alpha = max(alpha, response);
                        if (beta <= alpha) {
                            break;
                        }
                    }
                }
            }
        } else if (sense == -1) {
            bestSoFar = INFTY;
            if (board.winner() != null) {
                if (board.winner() == Piece.WHITE) {
                    return WINNING_VALUE;
                } else if (board.winner() == Piece.BLACK) {
                    return -WINNING_VALUE;
                }
            }
            if (depth == 0) {
                return simpleFindMin(board, alpha, beta);
            } else {
                for (Move move: board.legalMoves(myPiece())) {
                    Board copy = new Board(); copy.copy(board);
                    copy.makeMove(move);
                    int response = findMove(copy, depth - 1,
                            false, 1, alpha, beta);
                    if (response <= bestSoFar) {
                        bestSoFar = response; bestMove = move;
                        beta = min(beta, response);
                        if (beta <= alpha) {
                            break;
                        }
                    }
                }
            }
        }
        if (saveMove) {
            _lastFoundMove = bestMove;
        }
        return bestSoFar;
    }
    /** Max function that takes in BOARD, ALPHA and BETA @return bestSoFar. */
    private int simpleFindMax(Board board, int alpha, int beta) {
        if (board.winner() == Piece.WHITE) {
            return WINNING_VALUE;
        } else if (board().winner() == Piece.BLACK) {
            return -WINNING_VALUE;
        }
        int bestSoFar = -INFTY;
        for (Move move : board.legalMoves(myPiece())) {
            board.makeMove(move);
            int val = staticScore(board);
            if (val >= bestSoFar) {
                bestSoFar = val;
                alpha = max(alpha, val);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return bestSoFar;
    }

    /** Min function that takes in BOARD, ALPHA and BETA @return bestSoFar. */
    private int simpleFindMin(Board board, int alpha, int beta) {
        if (board.winner() == Piece.WHITE) {
            return WINNING_VALUE;
        } else if (board().winner() == Piece.BLACK) {
            return -WINNING_VALUE;
        }
        int bestSoFar = INFTY;
        for (Move move : board.legalMoves(myPiece())) {
            board.makeMove(move);

            int val = staticScore(board);

            if (val <= bestSoFar) {
                bestSoFar = val;
                beta = min(beta, val);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return bestSoFar;
    }




    /** Return a heuristically determined maximum search depth based on
     * characteristics of BOARD. */
    private static int maxDepth(Board board) {
        return 2;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        int heuristic;
        int kingVal = 7;
        int wc = board.pieceLocations(Piece.WHITE).size() + kingVal;
        int bc = board.pieceLocations(Piece.BLACK).size();
        if (board.kingPosition() != null) {
            heuristic = wc - bc;
            if (board.kingPosition().isEdge()) {
                heuristic = WILL_WIN_VALUE;
            }
        } else {
            heuristic = -WILL_WIN_VALUE;
        }
        return heuristic;
    }

}
