package server.websocket;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;


public class ConnectionManager {

    public ConcurrentHashMap<Integer, Connection> connections = new ConcurrentHashMap<>();

    public void add(Integer gameID, Session session) {
        // TODO: Configure the Connection class
        var connection = new Connection(gameID, session);
        connections.put(gameID, connection);
    }


}
