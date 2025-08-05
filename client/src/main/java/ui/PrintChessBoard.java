package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import static ui.EscapeSequences.*;

public class PrintChessBoard {

    // Board dimensions.
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 2;
    ChessGame.TeamColor teamColor;
    // Padded characters.
    private final int startRow;
    private final int endRow;
    private final int rowDir;
    private final int startCol;
    private final int endCol;
    private final int colDir;
    private ChessPosition highlightPosition;
    private Collection<ChessPosition> squaresToHighlight = new ArrayList<>();


    public PrintChessBoard(ChessGame.TeamColor teamColor) {
        this.teamColor = teamColor;
        if (teamColor == ChessGame.TeamColor.BLACK) {
            startRow = 1;
            endRow = 8;
            rowDir = 1;

            startCol = 8;
            endCol = 1;
            colDir = -1;

        } else {
            startRow = 8;
            endRow = 1;
            rowDir = -1;

            startCol = 1;
            endCol = 8;
            colDir = 1;
        }

    }

    public void print(ChessGame chessGame, ChessPosition highlightPosition) {

        ChessBoard board = chessGame.getBoard();

        this.highlightPosition = highlightPosition;
        if (highlightPosition != null) {
            Collection<ChessMove> validMoves = chessGame.validMoves(this.highlightPosition);
            squaresToHighlight.add(highlightPosition);
            if (!validMoves.isEmpty()) {
                for (ChessMove move : validMoves) {
                    squaresToHighlight.add(move.getEndPosition());
                }
            }
        } else {
            squaresToHighlight.clear();
        }

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
        for (int row = startRow; row != endRow + rowDir; row += rowDir) {
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
        for (int col = startCol; col != endCol + colDir; col += colDir) {

            ChessPosition curPosition = new ChessPosition(row, col);

            if (dark) {
                if (squaresToHighlight.contains(curPosition)) {
                    setDarkGreen(out);
                } else {
                    setDarkGrey(out);
                }
            } else {
                if (squaresToHighlight.contains(curPosition)) {
                    setLightGreen(out);
                } else {
                    setLightGrey(out);
                }
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

    private void setLightGreen(PrintStream out) {
        out.print(SET_BG_COLOR_GREEN);
        out.print(SET_TEXT_COLOR_GREEN);
    }

    private void setDarkGreen(PrintStream out) {
        out.print(SET_BG_COLOR_DARK_GREEN);
        out.print(SET_TEXT_COLOR_GREEN);
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
        for (int col = startCol - 1; col != endCol + colDir - 1; col += colDir) {
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

