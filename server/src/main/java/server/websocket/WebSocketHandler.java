package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.AuthDAO;
import exception.UnauthorizedException;
import model.GameData;
import model.ResignRequest;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import server.Server;
import websocket.commands.*;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Map;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections;
    private final AuthDAO authDAO;
    private Server server;

    public WebSocketHandler(Server server) {
        this.server = server;
        this.authDAO = server.authDAO;
        this.connections = new ConnectionManager(this.server);
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
                case LEAVE -> leaveGame(username, new Gson().fromJson(msg, LeaveGameCommand.class));
                case RESIGN -> resign(session, username, new Gson().fromJson(msg, ResignCommand.class));
            }
        } catch (UnauthorizedException ex) {
            // Serializes and sends the error message
            sendMessage(session.getRemote(), new ErrorMessage("Error: Unauthorized"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(session.getRemote(), new ErrorMessage("Found Some Error: " + ex));
        }
    }


    private void connectToGame(Session session, String username, ConnectCommand command) throws IOException {
        saveSession(command, session);
        if (connections.sendLoadGame(command, session)) {
            var message = String.format("%s has joined the game as %s", username, command.getWhoIsConnecting());
            var notification = new NotificationMessage(message);
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);
        }
    }

    private void resign(Session session, String username, ResignCommand command) throws IOException {
        String winner = connections.resign(session, command);
        if (winner != null) {
            var message = String.format("%s has resigned. %s Wins!", username, winner);
            var notification = new NotificationMessage(message);
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);
            try {
                GameData game = server.gameDAO.getGameByID(command.getGameID());
                System.out.println("GAME AFTER RESIGNATION: " + game);
            } catch (Exception e) {
                System.out.println("mytest Failed because of " + e.getMessage());
            }
        }
    }

    private void leaveGame(String username, LeaveGameCommand command) throws IOException {
        String role = connections.roleOfWhoLeft(username, command.getAuthToken(), command.getGameID());
        if (role != null) {
            endSession(command);
            var message = String.format("(%s) %s has left the game", role, username);
            var notification = new NotificationMessage(message);
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);
        }
    }

    private void makeMove(Session session, String username, MakeMoveCommand mmcmd) throws IOException {
        if (connections.sendLoadGameForMove(mmcmd, session)) {
            var message = String.format("%s moved from %s to %s", username, mmcmd.move.getStartPosition(), mmcmd.move.getEndPosition());
            var notification = new NotificationMessage(message);
            connections.broadcast(mmcmd.getGameID(), mmcmd.getAuthToken(), notification);
            try {
                var loadGameMessage = new LoadGameMessage(server.gameDAO.getGameByID(mmcmd.getGameID()));
                connections.broadcast(mmcmd.getGameID(), mmcmd.getAuthToken(), loadGameMessage);
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        }
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

    private void endSession(LeaveGameCommand command) {
        connections.removeUserFromGame(command.getGameID(), command.getAuthToken());
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


