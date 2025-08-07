package server.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import model.MakeMoveResponse;
import org.eclipse.jetty.websocket.api.Session;
import server.Server;
import websocket.commands.ConnectCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;


public class ConnectionManager {

    public ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> connections;
    private final Server server;

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
        if (!c.session.isOpen()) {
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
        if (oldGameData.game().getWinner() != null) {
            c.send(gson.toJson(new ErrorMessage("ERROR: Game Is Over")));
            return false;
        }
        ChessGame.TeamColor myColor;
        // Make sure it's your turn
        if (blackUser != null && blackUser.equals(username)) {
            myColor = ChessGame.TeamColor.BLACK;
        } else {
            myColor = ChessGame.TeamColor.WHITE;
        }
        ChessGame.TeamColor currentTurn = oldGameData.game().getTeamTurn();
        if (!currentTurn.equals(myColor)) {
            c.send(gson.toJson(new ErrorMessage("ERROR: NOT YOUR TURN. WAIT UNTIL YOUR TURN TO MOVE.")));
            return false;
        }
        // Moves are valid format
        ChessPosition startPos = command.move.getStartPosition();
        ChessPosition endPos = command.move.getEndPosition();
        if (startPos == null || endPos == null) {
            c.send(gson.toJson(new ErrorMessage("ERROR: Invalid Input.")));
            return false;
        }
        try {
            ChessPiece piece = oldGameData.game().getBoard().getPiece(startPos);
            if (piece == null) {
                c.send(gson.toJson(new ErrorMessage("ERROR: No Piece Here")));
                return false;
            }
            if (piece.getTeamColor() != myColor) {
                c.send(gson.toJson(new ErrorMessage("ERROR: THIS ISN'T YOUR PIECE. YOU CAN'T MOVE THIS PIECE")));
                return false;
            }
        } catch (Exception e) {
            c.send(gson.toJson(new ErrorMessage("ERROR: Couldn't Find A Start Piece Indicated By The Move")));
            return false;
        }
        try {
            MakeMoveResponse resp = server.gameDAO.makeMove(gameID, new ChessMove(startPos, endPos, null));
            if (!resp.success()) {
                throw new Exception("Invalid Move");
            }
            c.send(new Gson().toJson(new LoadGameMessage(resp.gameData())));
            return true;
        } catch (Exception e) {
            c.send(gson.toJson(new ErrorMessage("ERROR: Can't Make This Move " + e.getMessage())));
            return false;
        }
    }

    public boolean sendLoadGame(ConnectCommand command, Session session) throws IOException {

        int gameID = command.getGameID();
        String authToken = command.getAuthToken();
        Connection c = connections.get(gameID).get(authToken);
        Gson gson = new Gson();
        ConnectCommand.ClientType clientType = command.getClientType();

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


            System.out.println("The game id you requested was: " + gameID);
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

            try {

                if (clientType != null) {
                    if (clientType == ConnectCommand.ClientType.WHITE) {
                        server.gameDAO.joinGame(server.authDAO.getAuthByAuthToken(authToken), game.gameName(), clientType.toString());
                        game = server.gameDAO.getGameByID(gameID);
                    } else if (clientType == ConnectCommand.ClientType.BLACK) {
                        server.gameDAO.joinGame(server.authDAO.getAuthByAuthToken(authToken), game.gameName(), clientType.toString());
                        game = server.gameDAO.getGameByID(gameID);
                    }
                }
            } catch (Exception e) {
                c.send(gson.toJson(new ErrorMessage("Color already taken")));
                return false;
            }

            c.send(new Gson().toJson(new LoadGameMessage(game)));
            return true;

        } else {
            c.send(new Gson().toJson(new ErrorMessage("ERROR: Wasn't Able To Load Game")));
        }

        return false;
    }

    public String resign(Session session, ResignCommand command) throws IOException {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();
        Gson gson = new Gson();
        Connection c = connections.get(gameID).get(authToken);

        // Make Sure the session is open
        if (!c.session.isOpen()) {
            System.out.print("Session isn't Open");
            return null;
        }

        // Get the current game based off the id provided
        GameData gameData;
        try {
            gameData = server.gameDAO.getGameByID(gameID);
        } catch (DataAccessException e) {
            c.send(gson.toJson(new ErrorMessage("ERROR: Can't Make This Move " + e.getMessage())));
            return null;
        }

        // Make sure That there isn't currently a winner
        if (gameData.game().getWinner() != null) {
            c.send(gson.toJson(new ErrorMessage("ERROR: Game Is Over. No Resigning.")));
            return null;
        }

        // MUST BE A PLAYER TO RESIGN
        String username;
        try {
            username = server.authDAO.getAuthByAuthToken(authToken);
            if (username == null || (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername()))) {
                c.send(gson.toJson(new ErrorMessage("Error: No Observers Can Resign!")));
                return null;
            }
        } catch (Exception e) {
            c.send(gson.toJson(new ErrorMessage("Error: Can Only Sign Out Players Who are PLAYING")));
            return null;
        }


        try {
            System.out.println(username);
            System.out.println(gameData);
            String winner = server.gameDAO.resign(gameID, username);
            c.send(gson.toJson(new NotificationMessage("You Have Lost Due To Resignation :( ")));
            return winner;
        } catch (Exception e) {
            c.send(gson.toJson(new ErrorMessage("Error: We Had an error Resigning you " + e.getMessage())));
            return null;
        }

    }

    public String roleOfWhoLeft(String username, String authToken, Integer gameID) throws IOException {
        GameData game;
        Gson gson = new Gson();
        Connection c = connections.get(gameID).get(authToken);

        // Make Sure the session is open
        if (!c.session.isOpen()) {
            System.out.print("Session isn't Open");
            return null;
        }


        // Load the Game
        try {
            game = server.gameDAO.getGameByID(gameID);
        } catch (Exception e) {
            c.send(gson.toJson(new ErrorMessage("Error: We Had an error Resigning you " + e.getMessage())));
            return null;
        }

        try {
            // See if This Guy is an observer or a player
            if (game.whiteUsername() != null && game.whiteUsername().equals(username)) {
                server.gameDAO.leaveGame(gameID, username);
                return "WHITE";
            } else if (game.blackUsername() != null && game.blackUsername().equals(username)) {
                server.gameDAO.leaveGame(gameID, username);
                return "BLACK";
            } else {
                return "OBSERVER";
            }
        } catch (DataAccessException e) {
            c.send(gson.toJson(new ErrorMessage("Error: We couldn't find your username in this game " + e.getMessage())));
            return null;
        }

    }
}



