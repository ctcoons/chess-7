package server.websocket;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.SQLAuthDAO;
import exception.UnauthorizedException;
import websocket.commands.UserGameCommand;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

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
                case CONNECT -> connect(session, username, command);
                case MAKE_MOVE -> makeMove(session, username, command);
                case LEAVE -> LeaveGame(session, username, command);
                case RESIGN -> resign(session, username, command);
            }
        } catch (UnauthorizedException ex) {
            // Serializes and sends the error message
            sendMessage(session.getRemote(), new ErrorMessage("Error: unauthorized"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(session.getRemote(), new ErrorMessage("Error: " + ex.getMessage()));
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


