package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import service.GameAlreadyExistsException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SQLGameDAO extends SQLParent implements GameDAO {

    public SQLGameDAO() {

    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, whiteUsername, blackUsername, gameName, json FROM gameData";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Failed to retrieve games");
        }
        return result;
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var id = rs.getInt("id");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        var json = rs.getString("json");
        ChessGame chessGame = new Gson().fromJson(json, ChessGame.class);
        return new GameData(id, whiteUsername, blackUsername, gameName, chessGame);
    }

    @Override
    public void createGame(String gameName) {
        var statement = "INSERT INTO gameData (gameName, json) VALUES (?, ?)";
        var json = new Gson().toJson(new ChessGame());
        try {
            executeUpdate(statement, gameName, json);
        } catch (DataAccessException e) {
            System.out.println("WARNING! DID NOT CREATE GAME: " + gameName + " DUE TO ERROR: " + e);
        }

    }

    @Override
    public void clear() {

    }

    @Override
    public boolean contains(String gameName) {
        return false;
    }

    @Override
    public GameData getGameByName(String gameName) throws DataAccessException {
        return null;
    }

    @Override
    public GameData getGameByID(int id) throws DataAccessException {
        return null;
    }

    @Override
    public void joinGame(String username, String gameName, String desiredColor) throws DataAccessException {

    }

}
