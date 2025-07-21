package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.List;

public class SQLGameDAO extends SQLParent implements GameDAO {

    public SQLGameDAO() throws DataAccessException {

    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void createGame(String gameName) {

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
