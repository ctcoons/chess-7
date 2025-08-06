package client.websocket;

import chess.ChessGame;
import chess.ChessPosition;
import websocket.messages.ServerMessage;

public interface NotificationHandler {
    void notify(ServerMessage serverMessage);

    void redraw(ChessGame game, ChessPosition highlightPosition);
}
