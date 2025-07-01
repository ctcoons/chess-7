package chess;

import java.util.ArrayList;
import java.util.Collection;

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

        for (ChessMove uneditedMove: uneditedMoves) {
            // TODO: Make a loop that will say which moves won't put you in check
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
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        TeamColor opponentColor;
        ChessPosition myKing;

        switch (teamColor) {
            case BLACK -> {
                opponentColor = TeamColor.WHITE;
                myKing = blackKingPosition;
            }
            default -> {
                opponentColor = TeamColor.BLACK;
                myKing = whiteKingPosition;
            }
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
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        throw new RuntimeException("Not implemented");
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

}
