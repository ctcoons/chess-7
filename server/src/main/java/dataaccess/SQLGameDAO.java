package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;


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
            System.out.println("error: " + e);
            throw new DataAccessException("Failed to retrieve games: " + e);
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
    public void clear() throws DataAccessException {
        String statement1 = "DELETE FROM gameData";
        executeUpdate(statement1);
    }

    @Override
    public boolean containsGameByName(String gameName) throws DataAccessException {
        var result = new ArrayList<String>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, gameName FROM gameData";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGameNames(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Exception in Contains" + e);
        }
        return result.contains(gameName);
    }

    private String readGameNames(ResultSet rs) throws SQLException {
        return rs.getString("gameName");
    }


    @Override
    public GameData getGameByName(String gameName) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, whiteUsername, blackUsername, gameName, json FROM gameData WHERE gameName=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, gameName);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Failed to get game by name: " + gameName + " Due to error: " + e);
        }

    }

    @Override
    public GameData getGameByID(int id) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, whiteUsername, blackUsername, gameName, json FROM gameData WHERE id=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, id);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Failed to get game by id: " + id + " Due to error: " + e);
        }
    }

    public boolean containsGameById(int id) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM gameData WHERE id=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, id);
                try (var rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Error searching for Contains Game: " + e);
        }
    }

    @Override
    public void joinGame(String username, String gameName, String desiredColor) throws DataAccessException {
        GameData gameData = getGameByName(gameName);
        String statement;
        if (desiredColor.equals("WHITE")) {
            if (gameData.whiteUsername() != null) {
                throw new DataAccessException("White Taken");
            } else {
                statement = "UPDATE gameData SET whiteUsername = ? WHERE gameName = ?";
            }
        } else if (desiredColor.equals("BLACK")) {
            if (gameData.blackUsername() != null) {
                throw new DataAccessException("BLACK Taken by User: " + gameData.blackUsername());
            } else {
                statement = "UPDATE gameData SET blackUsername = ? WHERE gameName = ?";
            }
        } else {
            throw new DataAccessException("Must pick WHITE or BLACK color");
        }

        executeUpdate(statement, username, gameName);
    }

}
