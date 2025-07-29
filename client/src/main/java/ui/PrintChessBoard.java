package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class PrintChessBoard {

    // Board dimensions.
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 2;
    ChessGame.TeamColor TEAM_COLOR;
    // Padded characters.
    private final int START_ROW;
    private final int END_ROW;
    private final int ROW_DIR;
    private final int START_COL;
    private final int END_COL;
    private final int COL_DIR;


    public PrintChessBoard(ChessGame.TeamColor teamColor) {
        this.TEAM_COLOR = teamColor;
        if (teamColor == ChessGame.TeamColor.BLACK) {
            START_ROW = 1;
            END_ROW = 8;
            ROW_DIR = 1;

            START_COL = 8;
            END_COL = 1;
            COL_DIR = -1;

        } else {
            START_ROW = 8;
            END_ROW = 1;
            ROW_DIR = -1;

            START_COL = 1;
            END_COL = 8;
            COL_DIR = 1;
        }

    }

    public void print(ChessBoard board) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);

        drawFooters(out);

        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);

        drawChessBoard(board, out);

        drawFooters(out);

    }


    private boolean flop(boolean dark) {
        return !dark;
    }

    private void drawChessBoard(ChessBoard board, PrintStream out) {
        boolean dark = false;
        for (int row = START_ROW; row != END_ROW + ROW_DIR; row += ROW_DIR) {
            drawLeftMargin(row, out);

            if (dark) {
                setDarkGrey(out);
            } else {
                setLightGrey(out);
            }

            drawRowOfSquares(board, row, out, dark);
            dark = flop(dark);
        }
    }

    private void drawLeftMargin(int row, PrintStream out) {
        printFooterText(out, " " + String.valueOf(row) + " ");
    }

    private void drawRowOfSquares(ChessBoard board, int row, PrintStream out, boolean dark) {
        for (int col = START_COL; col != END_COL + COL_DIR; col += COL_DIR) {


            if (dark) {
                setDarkGrey(out);
            } else {
                setLightGrey(out);
            }
//            out.print(" ");

            String piece = getUnicodePiece(board, row, col, out);
            out.print(piece);

            if (dark) {
                setDarkGrey(out);
            } else {
                setLightGrey(out);
            }
//            out.print(" ");


            dark = flop(dark);

        }
        setBlack(out);
        out.println();
    }

    private String getUnicodePiece(ChessBoard board, int row, int col, PrintStream out) {
        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(position);
        if (piece == null) {
            return EMPTY;
        }

        ChessGame.TeamColor color = piece.getTeamColor();
        ChessPiece.PieceType type = piece.getPieceType();

        if (color == ChessGame.TeamColor.WHITE) {
            out.print(SET_TEXT_COLOR_WHITE);
            return switch (type) {
                case KING -> WHITE_KING;
                case QUEEN -> WHITE_QUEEN;
                case BISHOP -> WHITE_BISHOP;
                case KNIGHT -> WHITE_KNIGHT;
                case ROOK -> WHITE_ROOK;
                case PAWN -> WHITE_PAWN;
            };
        } else {
            out.print(SET_TEXT_COLOR_BLACK);
            return switch (type) {
                case KING -> BLACK_KING;
                case QUEEN -> BLACK_QUEEN;
                case BISHOP -> BLACK_BISHOP;
                case KNIGHT -> BLACK_KNIGHT;
                case ROOK -> BLACK_ROOK;
                case PAWN -> BLACK_PAWN;
            };
        }

    }

    private void drawFooters(PrintStream out) {
        setBlack(out);
        out.print("   ");

        String[] footers = {"a", "b", "c", "d", "e", "f", "g", "h"};
        for (int col = START_COL - 1; col != END_COL + COL_DIR - 1; col += COL_DIR) {
            drawFooter(out, footers[col]);

        }
        out.println();
    }

    private void drawFooter(PrintStream out, String footerText) {
        int prefixLength = SQUARE_SIZE_IN_PADDED_CHARS / 2;
        int suffixLength = SQUARE_SIZE_IN_PADDED_CHARS - prefixLength - 1;

        out.print(" ");
        printFooterText(out, footerText);
        out.print(" ");
    }

    private static void printFooterText(PrintStream out, String piece) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_GREEN);

        out.print(piece);

        setBlack(out);
    }

    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setLightGrey(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_LIGHT_GREY);
    }

    private static void setDarkGrey(PrintStream out) {
        out.print(SET_BG_COLOR_DARK_GREY);
        out.print(SET_TEXT_COLOR_DARK_GREY);
    }


}

/*
for (int row = START_ROW; row != END_ROW + ROW_DIR; row += ROW_DIR) {
            for (int col = START_COL; col != END_COL + COL_DIR; col += COL_DIR) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece == null) {
                    System.out.println("NULL_PIECE");
                } else {
                    ChessGame.TeamColor pieceColor = piece.getTeamColor();
                    ChessPiece.PieceType pieceType = piece.getPieceType();
                    System.out.println(pieceColor + pieceType.toString());
                }

            }
        }
 */





/*
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece == null) {
                    System.out.println("NULL_PIECE");
                } else {
                    System.out.println(piece.getPieceType());
                }
 */


