package client;

import chess.ChessBoard;
import chess.ChessGame;
import client.websocket.NotificationHandler;
import model.GameData;
import ui.PrintChessBoard;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {

    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);

    }

    public void run() {
        System.out.println(WHITE_QUEEN + "Welcome to Chess. Sign in to start.");
        System.out.print(SET_TEXT_COLOR_BLUE + client.help());

        System.out.print(moveCursorToLocation(15, 7));
        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals(("quit"))) {
            if (client.inGame) {
                drawGame(scanner);
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

    private void drawGame(Scanner scanner) {
        PrintChessBoard printChessBoard = new PrintChessBoard(client.color);

        var result = "";
        while (!result.equals(("quit"))) {

            String observerStatus = client.observer ? "OBSERVER" : client.color.toString();

            System.out.print(ERASE_SCREEN + moveCursorToLocation(1, 1));
            System.out.flush();

            GameData gameData = client.gaMe;
            ChessGame chessGame = gameData.game();
            ChessBoard chessBoard = chessGame.getBoard();

            printChessBoard.print(chessBoard);

            System.out.print(RESET_BG_COLOR);
            System.out.print(SET_TEXT_COLOR_BLUE);

            System.out.print("\n" + "[INGAME:" + observerStatus + "]" + ">>> " + SET_TEXT_COLOR_GREEN);
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


    public void notify(ServerMessage serverMessage) {
        System.out.println(SET_TEXT_COLOR_RED + serverMessage.getMessage());
        printPrompt();
    }

}
