package client.websocket;

import chess.ChessGame;
import chess.ChessPosition;
import ui.PrintChessBoard;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

public interface NotificationHandler {
    void notify(ServerMessage serverMessage);

    void redraw(ChessGame game, ChessPosition highlightPosition);

    void errorHandler(String message);

    void setPrintObject(PrintChessBoard printChessBoard);

    PrintChessBoard getPrintObject();
}
