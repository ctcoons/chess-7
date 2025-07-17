package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO{

    int id = 1234;
    private HashMap<String, GameData> gameDataHashMap = new HashMap<>();


    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return gameDataHashMap.values();
    }

    @Override
    public void createGame(String gameName) {
        GameData gameData = new GameData(id, null, null, gameName, new ChessGame());
        gameDataHashMap.put(gameName, gameData);
        id++;
    }


    @Override
    public void clear() {
        gameDataHashMap.clear();
    }

    @Override
    public boolean contains(String gameName) {
        return gameDataHashMap.containsKey(gameName);
    }

    @Override
    public GameData getGameByName(String gameName) throws DataAccessException {
        if(gameDataHashMap.containsKey(gameName)) {
            return gameDataHashMap.get(gameName);
        } else {
            throw new DataAccessException("No Game By This Name");
        }
    }

    @Override
    public GameData getGameByID(int ID) throws DataAccessException {
        GameData gameData = null;

        for (var item : gameDataHashMap.entrySet()) {
            if (item.getValue().gameID() == ID) {
                gameData = item.getValue();
                break;
            }
        }

        if (gameData == null) {
            throw new DataAccessException("No Game By This ID");
        } else {
            return gameData;
        }
    }

    @Override
    public void joinGame(String username, String gameName, String desiredColor) throws DataAccessException {
        GameData gameData = getGameByName(gameName);
        GameData updatedGame;

        if (desiredColor.equals("WHITE")) {
            updatedGame = new GameData(gameData.gameID(), username, gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else {
            updatedGame = new GameData(gameData.gameID(), gameData.whiteUsername(), username, gameData.gameName(), gameData.game());
        }

        gameDataHashMap.remove(gameData.gameName());
        gameDataHashMap.put(updatedGame.gameName(), updatedGame);
    }



}
