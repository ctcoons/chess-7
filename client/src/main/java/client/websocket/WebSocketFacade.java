package client.websocket;

import chess.ChessMove;
import client.ChessClient;
import com.google.gson.Gson;
import exception.ResponseException;
import model.MakeMoveResponse;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
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
        chessClient.gaMe = loadGameMessage.game;
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

