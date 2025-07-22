package dataaccess;

import model.GameData;


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


}
