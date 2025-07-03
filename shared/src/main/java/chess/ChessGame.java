package chess;

import java.util.ArrayList;
import java.util.Arrays;
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

    public ChessGame(ChessBoard copy, TeamColor copyTurnColor) {

        // This is the copy Constructor
        // It will set Turn Color to the Copy Turn Color
        // It will set blackKing and whiteKing to the right place

        this.board = new ChessBoard();
        this.currentTurnColor = copyTurnColor;

        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition curPosition = new ChessPosition(row, col);
                ChessPiece piece = copy.getPiece(curPosition);
                 if (piece != null) {
                     ChessPiece copiedPiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                     this.board.addPiece(curPosition, copiedPiece);

                    if (copiedPiece.getPieceType() == ChessPiece.PieceType.KING) {
                        if (copiedPiece.getTeamColor() == TeamColor.BLACK) {
                            this.blackKingPosition = curPosition;
                        } else {
                            this.whiteKingPosition = curPosition;
                        }
                    }
                }
            }
        }
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
        Collection<ChessMove> myValidMoves= new ArrayList<>();

        ChessPiece myPiece = board.getPiece(startPosition);
        if (myPiece == null) return myValidMoves;
        if (myPiece.getTeamColor() != currentTurnColor) return myValidMoves;



        // Check all the possible Moves for the piece at that location
        // Make a Copy of the Game, test those moves
        // If the move DOESN'T put you in Check, add it to the list of Valid Moves
        // Return This List

        for (ChessMove move : myPiece.pieceMoves(board, startPosition)) {
            ChessGame testGame = new ChessGame(this.board, this.currentTurnColor);
            try {
                // TODO: THERE IS ALSO AN ERROR HERE AT 111
                testGame.makeMove(move);
                if (!testGame.isInCheck(currentTurnColor)) {
                    myValidMoves.add(move);
                }
            } catch (InvalidMoveException e) {
                System.out.println("Error");
            }
        }

        return myValidMoves;
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

        // Check for Null
        if (piece == null) {
            throw new InvalidMoveException("Piece is Null");
        }


        // Check for Wrong Turn
        if (piece.getTeamColor() != currentTurnColor) {
            throw new InvalidMoveException("Not Your Turn");
        }

        // TODO: THERE IS AN ERROR HERE AT LINE 146??
        if (!validMoves(start).contains(move)) {
            throw new InvalidMoveException("Invalid Move");
        }

        if (move.getPromotionPiece() != null) {

            ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            board.addPiece(end, promotedPiece);

        } else {

            board.addPiece(end, piece);
        }


        board.addPiece(start, null);

        // Switch Team Turn
        if (piece.getTeamColor() == TeamColor.BLACK) {
            setTeamTurn(TeamColor.WHITE);
        } else {
            setTeamTurn(TeamColor.BLACK);
        }

        // Update King If Needed
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == TeamColor.BLACK) {
                blackKingPosition = end;
            } else {
                whiteKingPosition = end;
            }
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

        // Iterate through all the opponent's pieces to check if one of their moves equals Your King's position
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition curPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(curPosition);

                // If the piece you're looking at is NULL or == YOUR color, then, there is no reason to check if it will put you in check
                // Otherwise, it must be an opponents piece, so go check on its possible moves
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
            if (move.getEndPosition().equals(myKingPosition)) {
                System.out.println("OPPONENT IN CHECK" +
                        move);
                return true;
            }
        }
        return false;
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