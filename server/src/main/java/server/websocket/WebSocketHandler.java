package server.websocket;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.SQLAuthDAO;
import exception.UnauthorizedException;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import websocket.commands.*;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final AuthDAO authDAO;

    public WebSocketHandler(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }


    @OnWebSocketMessage
    public void onMessage(Session session, String msg) {
        try {
            UserGameCommand command = new Gson().fromJson(msg, UserGameCommand.class);

            // Throws a custom exception.UnauthorizedException. Yours may work differently.
            String username = getUsername(command.getAuthToken());

            saveSession(command, session);

            switch (command.getCommandType()) {
                case CONNECT -> connectToGame(session, username, (ConnectCommand) command);
                case MAKE_MOVE -> makeMove(session, username, (MakeMoveCommand) command);
                case LEAVE -> leaveGame(session, username, (LeaveGameCommand) command);
                case RESIGN -> resign(session, username, (ResignCommand) command);
            }
        } catch (UnauthorizedException ex) {
            // Serializes and sends the error message
            sendMessage(session.getRemote(), new ErrorMessage("Unauthorized"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(session.getRemote(), new ServerMessage(ServerMessage.ServerMessageType.ERROR));
        }
    }


    private void connectToGame(Session session, String username, ConnectCommand command) {
        connections.add(command.getGameID(), command.getAuthToken(), session);
        var message = String.format("%s has joined the game", username);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        connections.broadcast(command.getGameID(), command.getAuthToken(), ServerMessage);
    }

    private void resign(Session session, String username, ResignCommand command) {
    }

    private void leaveGame(Session session, String username, LeaveGameCommand command) {
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) {
    }

    private void sendMessage(RemoteEndpoint remote, ServerMessage serverMessage) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(serverMessage);
            remote.sendString(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSession(UserGameCommand command, Session session) {
        connections.add(command.getGameID(), command.getAuthToken(), session);
    }


    private String getUsername(String authToken) throws UnauthorizedException {
        if (authToken == null) {
            throw new UnauthorizedException("Not Authorized??");
        }

        try {
            return authDAO.getAuthByAuthToken(authToken);
        } catch (Exception e) {
            throw new UnauthorizedException("Also not authorized");
        }

    }


}


