package dataaccess;

import model.GameData;


import java.util.Collection;

public interface GameDAO {

    Collection<GameData> listGames() throws DataAccessException;

    void createGame(String gameName);

    void clear();

    boolean contains(String gameName);

    GameData getGameByName(String gameName) throws DataAccessException;

    GameData getGameByID(int id) throws DataAccessException;

    void joinGame(String username, String gameName, String desiredColor) throws DataAccessException;


}
