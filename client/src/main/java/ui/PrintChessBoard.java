package ui;

import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;

public class PrintChessBoard {
    public PrintChessBoard() {

    }

    public void print(ChessBoard board) {
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece == null) {
                    System.out.println("NULL_PIECE");
                } else {
                    System.out.println(piece.getPieceType());
                }


            }
        }
    }

}
