package dataaccess;

import chess.ChessMove;
import model.GameData;
import model.MakeMoveResponse;
import model.ResignRequest;


import java.util.Collection;

public interface GameDAO {

    Collection<GameData> listGames() throws DataAccessException;

    void createGame(String gameName);

    void clear() throws DataAccessException;

    boolean containsGameByName(String gameName) throws DataAccessException;

    GameData getGameByName(String gameName) throws DataAccessException;

    GameData getGameByID(int id) throws DataAccessException;

    void joinGame(String username, String gameName, String desiredColor) throws DataAccessException;

    boolean containsGameById(int id) throws DataAccessException;

    void leaveGame(int gameId, String username) throws DataAccessException;

    MakeMoveResponse makeMove(int gameId, ChessMove chessMove) throws DataAccessException;

    String resign(int gameId, String resigner);
}
