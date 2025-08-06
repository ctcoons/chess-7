package server.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import server.Server;
import websocket.commands.ConnectCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;


public class ConnectionManager {

    public ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> connections;
    private Server server;

    public ConnectionManager(Server server) {
        this.connections = new ConcurrentHashMap<>();
        this.server = server;
    }

    public void add(Integer gameId, String authToken, Session session) {
        connections.putIfAbsent(gameId, new ConcurrentHashMap<>());
        var connection = new Connection(authToken, session);
        connections.get(gameId).put(authToken, connection);
    }

    public void removeUserFromGame(Integer gameId, String authToken) {
        if (connections.get(gameId) == null) {
            return;
        }
        connections.get(gameId).remove(authToken);
    }

    public void broadcast(Integer gameId, String excludeAuthToken, ServerMessage serverMessage) throws IOException {
        var removeList = new ArrayList<Connection>();
        var participants = connections.get(gameId);
        if (participants == null) {
            return;
        }

        for (var c : participants.values()) {
            if (c.session.isOpen()) {
                if (!c.authToken.equals(excludeAuthToken)) {
                    c.send(new Gson().toJson(serverMessage));
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.get(gameId).remove(c.authToken);
        }
    }


    public void sendLoadGame(ConnectCommand command, Session session) throws IOException {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();

        Connection c = connections.get(gameID).get(authToken);

        Gson gson = new Gson();

        if (c.session.isOpen()) {
            GameData game;

            try {
                game = server.gameDAO.getGameByID(command.getGameID());
                if (game == null) {
                    throw new Exception("game shouldn't be null");
                }
            } catch (Exception e) {
                c.send(gson.toJson(new ErrorMessage("ERROR: no game by this ID")));
                return;
            }

            c.send(new Gson().toJson(new LoadGameMessage(game)));

        } else {
            c.send(new Gson().toJson(new ErrorMessage("ERROR: Wasn't Able To Load Game")));
        }
    }
}
