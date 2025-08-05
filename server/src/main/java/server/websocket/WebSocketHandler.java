package server.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.AuthDAO;
import exception.UnauthorizedException;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import websocket.commands.*;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections;
    private final AuthDAO authDAO;

    public WebSocketHandler(AuthDAO authDAO) {
        this.authDAO = authDAO;
        this.connections = new ConnectionManager();
    }


    @OnWebSocketMessage
    public void onMessage(Session session, String msg) {
        try {

            JsonObject jsonObject = JsonParser.parseString(msg).getAsJsonObject();
            String commandType = jsonObject.get("commandType").getAsString();
            UserGameCommand.CommandType type = UserGameCommand.CommandType.valueOf(commandType);
            Gson gson = new Gson();

            UserGameCommand command = switch (type) {
                case CONNECT -> gson.fromJson(jsonObject, ConnectCommand.class);
                case MAKE_MOVE -> gson.fromJson(jsonObject, MakeMoveCommand.class);
                case LEAVE -> gson.fromJson(jsonObject, LeaveGameCommand.class);
                case RESIGN -> gson.fromJson(jsonObject, ResignCommand.class);
            };

            // Throws a custom exception.UnauthorizedException. Yours may work differently.
            String username = getUsername(command.getAuthToken());

            switch (command.getCommandType()) {
                case CONNECT -> connectToGame(session, username, new Gson().fromJson(msg, ConnectCommand.class));
                case MAKE_MOVE -> makeMove(session, username, new Gson().fromJson(msg, MakeMoveCommand.class));
                case LEAVE -> leaveGame(session, username, new Gson().fromJson(msg, LeaveGameCommand.class));
                case RESIGN -> resign(session, username, new Gson().fromJson(msg, ResignCommand.class));
            }
        } catch (UnauthorizedException ex) {
            // Serializes and sends the error message
            sendMessage(session.getRemote(), new ErrorMessage("Unauthorized"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(session.getRemote(), new ErrorMessage("Found Some Error: " + ex));
        }
    }


    private void connectToGame(Session session, String username, ConnectCommand command) throws IOException {
        saveSession(command, session);
        var message = String.format("%s has joined the game as %s", username, command.getWhoIsConnecting());
        var notification = new NotificationMessage(message);
        connections.broadcast(command.getGameId(), command.getAuthToken(), notification);
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
        connections.add(command.getGameId(), command.getAuthToken(), session);
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


