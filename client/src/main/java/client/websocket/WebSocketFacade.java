package client.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import server.ServerFacade;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveGameCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.lang.module.ResolutionException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;


    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
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
                    notificationHandler.notify(serverMessage);
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

    public void joinGame(String authToken, int gameID, String whoIsConnecting) throws ResponseException {
        try {
            var connectCommand = new ConnectCommand(authToken, gameID, whoIsConnecting);
            this.session.getBasicRemote().sendText(new Gson().toJson(connectCommand));
            System.out.print("Sent Message To Other Players ??");
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void quitGame(String authToken, int gameID, String whoIsConnecting) throws ResponseException {
        try {
            var leaveCommand = new LeaveGameCommand(authToken, gameID, whoIsConnecting);
            this.session.getBasicRemote().sendText(new Gson().toJson(leaveCommand));
            System.out.print("Sent Message To Other Players ??");
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


}

