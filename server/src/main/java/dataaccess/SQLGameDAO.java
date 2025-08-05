package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import com.google.gson.Gson;
import model.GameData;
import model.MakeMoveResponse;


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
        return new GameData(id, whiteUsername, blackUsername, gameName, null, chessGame);
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
    public void leaveGame(int gameId, String username) throws DataAccessException {
        GameData gameData = getGameByID(gameId);
        String statement;

        if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username)) {
            statement = "UPDATE gameData SET whiteUsername = ? WHERE gameName = ?";
        } else if (gameData.blackUsername() != null && gameData.blackUsername().equals(username)) {
            statement = "UPDATE gameData SET blackUsername = ? WHERE gameName = ?";
        } else {
            throw new DataAccessException("This User Isn't Playing");
        }

        executeUpdate(statement, null, gameData.gameName());

    }

    @Override
    public MakeMoveResponse makeMove(int gameId, ChessMove chessMove) throws DataAccessException {
        GameData curGame = getGameByID(gameId);

        ChessPiece piece = curGame.game().getBoard().getPiece(chessMove.getStartPosition());

        // MAKE SURE THE PIECE ISN'T NULL
        if (piece == null) {
            return new MakeMoveResponse(false, "No Piece Here", curGame);
        }

        // THE COLOR OF THE PIECE BELONGS TO THE TURN THAT IT IS
        ChessGame.TeamColor pieceColorTryingToMove = piece.getTeamColor();
        ChessGame.TeamColor startingTurn = curGame.game().getTeamTurn();
        if (pieceColorTryingToMove != startingTurn) {
            return new MakeMoveResponse(false, "Can't Move A Piece That Isn't Yours", curGame);
        }

        // No MOVES ALLOWED IF GAME ALREADY WON
        if (curGame.winner() != null) {
            return new MakeMoveResponse(false, "Game Already Over", curGame);
        }

        // TRY TO MAKE THE MOVE
        GameData updatedGame;
        try {
            // 1. Make a move
            curGame.game().makeMove(chessMove);

            // 2. Find out if there is a winner
            GameData.Winner winner;
            ChessGame.TeamColor endingTurn = curGame.game().getTeamTurn();
            if (curGame.game().isInStalemate(endingTurn)) {
                winner = GameData.Winner.DRAW;
            } else if (curGame.game().isInCheckmate(endingTurn)) {
                winner = returnOppositeColor(endingTurn);
            } else {
                winner = null;
            }

            // Set updatedGameData
            updatedGame = new GameData(
                    curGame.gameID(),
                    curGame.whiteUsername(),
                    curGame.blackUsername(),
                    curGame.gameName(),
                    winner,
                    curGame.game()
            );
        } catch (Exception e) {
            return new MakeMoveResponse(false, "Failed to make move due to error: " + e.getMessage(), curGame);
        }


        try (var conn = DatabaseManager.getConnection()) {
            var statement = "UPDATE gameData SET winner = ?, game = ? WHERE id = ?";
            try (var ps = conn.prepareStatement(statement)) {
                if (updatedGame.winner() == null) {
                    ps.setNull(1, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(1, updatedGame.winner().toString());
                }
                ps.setString(2, new Gson().toJson(updatedGame.game()));
                ps.setInt(3, gameId);
                int rowsUpdated = ps.executeUpdate();
                if (rowsUpdated == 1) {
                    return new MakeMoveResponse(true, null, updatedGame);
                } else {
                    return new MakeMoveResponse(false, "Faled To Complete", curGame);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Wasn't Able To Update The Game in MakeMove " + e);
        }
    }

    private GameData.Winner returnOppositeColor(ChessGame.TeamColor endingTurn) {
        return ChessGame.TeamColor.BLACK.equals(endingTurn) ? GameData.Winner.BLACK : GameData.Winner.WHITE;
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
