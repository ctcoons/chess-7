package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import ui.EscapeSequences;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {

    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);

    }

    public void run() {
        System.out.println(WHITE_QUEEN + "Welcome to Chess. Sign in to start.");
        System.out.print(SET_TEXT_COLOR_BLUE + client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals(("quit"))) {
            if (client.INGAME) {
                drawGame();
            }
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + ERASE_SCREEN + "[" + client.state + "]" + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    private void drawGame() {
        if (client.COLOR == ChessGame.TeamColor.BLACK) {
            System.out.println("Printing game from black perspective");
        } else {
            System.out.println("Printing from white perspective");
        }

        GameData gameData = client.GAME;
        ChessGame chessGame = gameData.game();
        ChessBoard chessBoard = chessGame.getBoard();


    }


}
