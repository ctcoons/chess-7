package server.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;


public class ConnectionManager {

    public ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> connections;


    public ConnectionManager() {
        this.connections = new ConcurrentHashMap<>();
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
                    c.send(serverMessage.getMessage());
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


}
