package client;

import chess.ChessGame;
import chess.ChessPosition;
import client.websocket.NotificationHandler;
import com.google.gson.Gson;
import model.GameData;
import model.ResignRequest;
import ui.PrintChessBoard;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {

    public final ChessClient client;
    public PrintChessBoard printChessBoard;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
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

    private void printInGamePrompt() {
        System.out.print(RESET_BG_COLOR);
        System.out.print(SET_TEXT_COLOR_BLUE);
        String observerStatus = client.observer ? "OBSERVER" : client.color.toString();
        System.out.print("\n" + "[INGAME:" + observerStatus + "]" + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    private void drawGame(Scanner scanner) {
        // MAKE THE PRINTER OBJECT


        var result = "";
        while (!result.equals(("quit"))) {


            // Still Doesn't Work Like I Want it to. We Want this to clear the screen
            System.out.print(ERASE_SCREEN + moveCursorToLocation(1, 1));
            System.out.flush();

            GameData gameData = client.gaMe;
            ChessGame chessGame = gameData.game();
            if (gameData.winner() != null) {
                System.out.print(gameData.winner() + " WINS!\n");
            } else {
                System.out.print(RESET_BG_COLOR + SET_TEXT_COLOR_GREEN + chessGame.getTeamTurn() + " turn\n");
            }

//            printChessBoard.print(chessGame, null);

//            printInGamePrompt();
            String line = scanner.nextLine();

            // Try / Catch Block for the client.eval()
            try {
                result = client.eval(line);
                if (result.equals("redraw")) {
                    continue;
                }

                // Highlight Moves
                try {
                    ChessPosition highlightPosition = new Gson().fromJson(result, ChessPosition.class);
                    printChessBoard.print(client.gaMe.game(), highlightPosition);
                    continue;
                } catch (Exception ignore) {
                }

                try {
                    ResignRequest resignRequest = new Gson().fromJson(result, ResignRequest.class);
                    if (resignRequest == null) {
                        continue;
                    }
                    result = client.areYouSure(scanner);
                    if (result.equals("YES")) {
                        client.resignFromGame();
                        System.out.print("RESIGNING...");
                        System.out.print("YOU LOSE\n");
                    } else {
                        System.out.print("Not Resigning");
                    }
                    continue;

                } catch (Exception ignore) {
                }


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

    }

    @Override
    public void redraw(ChessGame game, ChessPosition highlightPosition) {
        System.out.print(game.getTeamTurn() + " turn\n");
        printChessBoard.print(game, highlightPosition);
        printInGamePrompt();
    }

    @Override
    public void errorHandler(String message) {
        ErrorMessage errorMessage = new Gson().fromJson(message, ErrorMessage.class);
        System.out.println(SET_TEXT_COLOR_RED + errorMessage.getError());
    }

    @Override
    public void setPrintObject(PrintChessBoard printChessBoard) {
        this.printChessBoard = printChessBoard;
    }

    @Override
    public PrintChessBoard getPrintObject() {
        return this.printChessBoard;
    }


}
