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

        setTeamTurn(TeamColor.WHITE);
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
        this.currentTurnColor = team;
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

        // Check to make sure myPiece isn't Null
        if (myPiece == null) {
            return validMoves;
        }

        Collection<ChessMove> uneditedMoves = myPiece.pieceMoves(board, startPosition);

        for (ChessMove move: uneditedMoves) {
            // Loop Through Each Move in Valid Moves
            // Make a new copy of the board and try the move
            // Check if It would put you in Check
            // If it doesn't result in check, Add it to Valid Moves

            ChessGame testGame = new ChessGame();
            testGame.copy_board(board);
            testGame.copy_game(currentTurnColor, blackKingPosition, whiteKingPosition);

            try {
                testGame.makeMove(move);
                if (!testGame.isInCheck(currentTurnColor)) {
                    validMoves.add(move);
                }
            } catch (Exception e) {
                System.out.println(e.toString());
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

        // Switch Team Turn
        if (getTeamTurn() == TeamColor.BLACK) {
            setTeamTurn(TeamColor.WHITE);
        } else {
            setTeamTurn(TeamColor.BLACK);
        }

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition myKingPosition;

        if (teamColor == TeamColor.BLACK) {
            myKingPosition = blackKingPosition;
        } else {
            myKingPosition = whiteKingPosition;
        }

        // Iterate through all the opponent's pieces to check if one of their moves equals
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition curPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(curPosition);
                if (piece == null) continue;
                if (piece.getTeamColor() == teamColor) continue;

                if (check_if_any_opponent_move_is_check(piece, curPosition, myKingPosition)){
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
                System.out.println("OPPONENT IN CHECK" +
                        move);
                return true;
            }
        }
        return false;
    }

    private void copy_board(ChessBoard boardToCopyFrom){

        for(int x = 0; x < 8; x++) {
            for(int y = 0; y < 8; y++) {
                ChessPosition curPosition = new ChessPosition(x+1, y+1);

                if (boardToCopyFrom.getPiece(curPosition) == null) {

                    board.addPiece(curPosition, null);

                } else {

                    ChessPiece originalPiece = boardToCopyFrom.getPiece(curPosition);
                    ChessPiece copyPiece = new ChessPiece(originalPiece.getTeamColor(), originalPiece.getPieceType());

                    board.addPiece(curPosition, copyPiece);

                    if (originalPiece.getPieceType() == ChessPiece.PieceType.KING){
                        if (originalPiece.getTeamColor() == TeamColor.BLACK) {
                            this.blackKingPosition = curPosition;
                        } else {
                            this.whiteKingPosition = curPosition;
                        }
                    }

                }

            }
        }
    }

    private void copy_game(TeamColor color, ChessPosition blackKingPos, ChessPosition whiteKingPos){
        this.currentTurnColor = color;
    }

    private ChessPosition find_king(TeamColor color) {
        for(int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                ChessPosition curPosition = new ChessPosition(x + 1, y + 1);
                ChessPiece piece = board.getPiece(curPosition);
                if (piece == null) {
                    continue;
                } else if (piece.getTeamColor() == color && piece.getPieceType() == ChessPiece.PieceType.KING){
                    return curPosition;
                }

            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTurnColor == chessGame.currentTurnColor && Objects.equals(board, chessGame.board) && Objects.equals(blackKingPosition, chessGame.blackKingPosition) && Objects.equals(whiteKingPosition, chessGame.whiteKingPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurnColor, board, blackKingPosition, whiteKingPosition);
    }
}


//             testGame.copy_game(currentTurnColor, blackKingPosition, whiteKingPosition);