package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    TeamColor currentTurnColor;
    ChessBoard board;
    ChessPosition blackKingPosition;
    ChessPosition whiteKingPosition;

    public ChessGame() {

        this.currentTurnColor = TeamColor.WHITE;
        ChessBoard newBoard = new ChessBoard();
        newBoard.resetBoard();
        this.board = newBoard;
        this.blackKingPosition = new ChessPosition(8, 5);
        this.whiteKingPosition = new ChessPosition(1, 5);

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurnColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurnColor = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {

        Collection<ChessMove> validMoves = new ArrayList<>();

        ChessPiece myPiece = board.getPiece(startPosition);
        Collection<ChessMove> uneditedMoves = myPiece.pieceMoves(board, startPosition);

        for (ChessMove move: uneditedMoves) {
            // Loop Through Each Move in Valid Moves
            // Make a new copy of the board and try the move
            // Check if It would put you in Check
            // If it doesn't result in check, Add it to Valid Moves

            ChessGame testGame = new ChessGame();
            copy_board(testGame);
            copy_game(testGame);

            try {
                testGame.makeMove(move);
            } catch (Exception e) {
                System.out.println(e.toString());
            }

            if (!testGame.isInCheck(testGame.currentTurnColor)) {
                validMoves.add(move);
            }

        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {

        ChessPosition start = move.startPosition;
        ChessPosition end = move.endPosition;
        ChessPiece piece = board.getPiece(start);

        if (piece == null) {
            throw new InvalidMoveException();
        }

        if (move.getPromotionPiece() != null) {

            ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            board.addPiece(end, promotedPiece);

        } else {

            board.addPiece(end, piece);
        }


        board.addPiece(start, null);

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition myKing;

        if (Objects.requireNonNull(teamColor) == TeamColor.BLACK) {
            myKing = blackKingPosition;
        } else {
            myKing = whiteKingPosition;
        }

        // Iterate through all the opponent's pieces to check if one of their moves equals
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition curPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(curPosition);
                if (piece == null) continue;
                if (piece.getTeamColor() == teamColor) continue;

                if (check_if_any_opponent_move_is_check(piece, curPosition, myKing)){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    boolean check_if_any_opponent_move_is_check(ChessPiece piece, ChessPosition curPosition, ChessPosition myKingPosition) {
        Collection<ChessMove> pieceMoves = piece.pieceMoves(board, curPosition);
        for (ChessMove move : pieceMoves) {
            if (move.getEndPosition() == myKingPosition) {
                return true;
            }
        }
        return false;
    }

    public void copy_board(ChessGame newGame){
        ChessBoard newBoard = newGame.getBoard();

        for(int x = 0; x < 8; x++) {
            for(int y = 0; y < 8; y++) {

                if (this.board.board[x][y] != null) {

                    ChessPiece originalPiece = this.board.getPiece(new ChessPosition(x+1, y+1));
                    ChessPiece copyPiece = new ChessPiece(originalPiece.getTeamColor(), originalPiece.getPieceType());

                    newBoard.board[x][y] = copyPiece;

                } else {

                    newBoard.board[x][y] = null;

                }

            }
        }
    }

    public void copy_game(ChessGame newGame){
        newGame.currentTurnColor = this.currentTurnColor;
        newGame.blackKingPosition = this.blackKingPosition;
        newGame.whiteKingPosition = this.whiteKingPosition;
    }
}

//ChessGame.TeamColor pieceColor, ChessPiece.PieceType type