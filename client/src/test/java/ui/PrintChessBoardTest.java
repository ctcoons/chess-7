package ui;

import chess.ChessBoard;
import chess.ChessGame;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrintChessBoardTest {

    static ChessGame game;
    static ChessBoard board;
    static PrintChessBoard printChessBoard;

    @BeforeAll
    static void init() {
        game = new ChessGame();
        board = game.getBoard();
        printChessBoard = new PrintChessBoard(ChessGame.TeamColor.BLACK);
    }


    @Test
    void print() {
        printChessBoard.print(game, null);
    }
}