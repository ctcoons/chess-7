package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    public Integer gameID;
    public Session session;


    public Connection(Integer gameID, Session session) {
        this.gameID = gameID;
        this.session = session;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }


}
