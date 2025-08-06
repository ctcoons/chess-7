package client.websocket;

import chess.*;
import client.ChessClient;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;
import model.MakeMoveResponse;
import model.ResignRequest;
import model.ResignResponse;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;
    ChessClient chessClient;


    public WebSocketFacade(String url, NotificationHandler notificationHandler, ChessClient chessClient) throws ResponseException {
        this.chessClient = chessClient;
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    switch (serverMessage.getServerMessageType()) {
                        case LOAD_GAME -> loadGame(message);
                        case ERROR -> errorHandler(message);
                        default -> notificationHandler.notify(serverMessage);
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void loadGame(String message) {
        LoadGameMessage loadGameMessage = new Gson().fromJson(message, LoadGameMessage.class);
        GameData gameData = loadGameMessage.game;
        ChessGame chessGame = gameData.game();
        ChessPosition highlightPosition = null;
        if (gameData.winner() != null) {
            notificationHandler.notify(new NotificationMessage(gameData.winner() + " WINS"));
        } else if (chessGame.isInCheck(ChessGame.TeamColor.WHITE)) {
            notificationHandler.notify(new NotificationMessage("WHITE IN CHECK"));
            highlightPosition = findKing(chessGame, ChessGame.TeamColor.WHITE);
        } else if (chessGame.isInCheck(ChessGame.TeamColor.BLACK)) {
            notificationHandler.notify(new NotificationMessage("BLACK IN CHECK"));
            highlightPosition = findKing(chessGame, ChessGame.TeamColor.BLACK);
        }
        chessClient.gaMe = loadGameMessage.game;
        notificationHandler.redraw(loadGameMessage.game.game(), highlightPosition);
    }

    private ChessPosition findKing(ChessGame chessGame, ChessGame.TeamColor teamColor) {
        ChessBoard board = chessGame.getBoard();
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece == null) {
                    continue;
                }
                if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return pos;
                }
            }
        }
        return null;
    }

    public void errorHandler(String message) {
        ErrorMessage errorMessage = new Gson().fromJson(message, ErrorMessage.class);
        notificationHandler.notify(errorMessage);
    }

    public void joinGame(String authToken, int gameID, String whoIsConnecting) throws ResponseException {
        try {
            var connectCommand = new ConnectCommand(authToken, gameID, whoIsConnecting);
            this.session.getBasicRemote().sendText(new Gson().toJson(connectCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void quitGame(String authToken, int gameID, String whoIsConnecting) throws ResponseException {
        try {
            var leaveCommand = new LeaveGameCommand(authToken, gameID, whoIsConnecting);
            this.session.getBasicRemote().sendText(new Gson().toJson(leaveCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void resign(ResignRequest resignRequest) throws ResponseException {
        try {
            var resignCommand = new ResignCommand(resignRequest);
            this.session.getBasicRemote().sendText(new Gson().toJson(resignCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void makeMove(String authToken, MakeMoveResponse makeMoveResponse, ChessMove chessMove) throws ResponseException {
        // TODO: Finish this
        try {
            var makeMoveCommand = new MakeMoveCommand(authToken, makeMoveResponse, chessMove);
            this.session.getBasicRemote().sendText(new Gson().toJson(makeMoveCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }


    }


}

