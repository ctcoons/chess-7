package server.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import server.Server;
import websocket.commands.ConnectCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
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


    public boolean sendLoadGameForMove(MakeMoveCommand command, Session session) throws IOException {
        // Must Be Your Turn

        int gameID = command.getGameID();
        String authToken = command.getAuthToken();
        Gson gson = new Gson();
        Connection c = connections.get(gameID).get(authToken);

        if (c.session.isOpen()) {
            System.out.print("Session isn't Open");
            return false;
        }

        // Valid Auth
        try {
            if (!server.authDAO.validateAuth(authToken)) {
                throw new Exception("we should have good auth");
            }
        } catch (Exception e) {
            c.send(gson.toJson(new ErrorMessage("ERROR: Invalid Auth")));
            return false;
        }

        // Get the Username of Who Is Making This Move
        String username;
        try {
            username = server.authDAO.getAuthByAuthToken(authToken);
            if (username == null) {
                throw new Exception("Bad Username");
            }
        } catch (Exception e) {
            c.send(gson.toJson(new ErrorMessage("ERROR: Couldn't find your username based on the authToken")));
            return false;
        }

        // Get the Game Data
        GameData oldGameData;
        try {
            oldGameData = server.gameDAO.getGameByID(gameID);
            if (oldGameData == null) {
                throw new DataAccessException("Bad Username");
            }
        } catch (DataAccessException e) {
            c.send(gson.toJson(new ErrorMessage("ERROR: Couldn't load the game with this ID")));
            return false;
        }

        // Observer Not Allowed to Play
        String blackUser = oldGameData.blackUsername();
        String whiteUser = oldGameData.whiteUsername();
        if (!username.equals(blackUser) && !username.equals(whiteUser)) {
            c.send(gson.toJson(new ErrorMessage("ERROR: Observer Not Allowed to make Moves")));
            return false;
        }

        // Game Isn't Over
        if (oldGameData.winner()!=null) {
            c.send(gson.toJson(new ErrorMessage("ERROR: Game Is Over")));
            return false;
        }




        // It isn't your turn
        // It isn't your piece
        // It isn't a valid move


        if () {
            c.send(gson.toJson(new ErrorMessage("ERROR: Invalid Auth")));
            return false;
        }

        if (gaMe.game().getTeamTurn() != color) {
            return "Not your turn. Wait until " + gaMe.game().getTeamTurn() + " makes a move.\n";
        }

        ChessPosition startPosition = validPosition(params[0]);
        ChessPosition endPosition = validPosition(params[1]);

        if (startPosition == null || endPosition == null) {
            return "Invalid format; enter: move [row][col] [row][col]; " +
                    "for example: move e2 e4; Moves must start abd end between a1 and h8";
        }

        ChessPiece piece = gaMe.game().getBoard().getPiece(startPosition);


        if (piece == null) {
            // TODO: Here i could highlight the piece they tried to move for some extra flare
            notificationHandler.redraw(gaMe.game(), startPosition);
            return "No Piece Found At " + params[0] + "\n";
        }

        ChessPiece.PieceType pieceType = piece.getPieceType();
    }



    public boolean sendLoadGame(ConnectCommand command, Session session) throws IOException {

        int gameID = command.getGameID();
        String authToken = command.getAuthToken();
        Connection c = connections.get(gameID).get(authToken);
        Gson gson = new Gson();

        if (c.session.isOpen()) {
            GameData game;


            // Valid Auth
            try {
                if (!server.authDAO.validateAuth(authToken)) {
                    throw new Exception("we should have good auth");
                }
            } catch (Exception e) {
                c.send(gson.toJson(new ErrorMessage("ERROR: Invalid Auth")));
                return false;
            }


            // Valid Game
            try {
                game = server.gameDAO.getGameByID(command.getGameID());
                if (game == null) {
                    throw new Exception("game shouldn't be null");
                }
            } catch (Exception e) {
                c.send(gson.toJson(new ErrorMessage("ERROR: no game by this ID")));
                return false;
            }

            c.send(new Gson().toJson(new LoadGameMessage(game)));
            return true;

        } else {
            c.send(new Gson().toJson(new ErrorMessage("ERROR: Wasn't Able To Load Game")));
        }

        return false;
    }
}
