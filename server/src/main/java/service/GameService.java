package service;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import model.JoinGameRequest;

import java.util.Collection;

public class GameService {

    public Collection<GameData> listGames(GameDAO gameDAO) throws DataAccessException {
        return gameDAO.listGames();
    }

    public void createGame(String gameName, GameDAO gameDAO) throws GameAlreadyExistsException {
        if (gameDAO.contains(gameName)) {
            throw new GameAlreadyExistsException("Game Name Taken");
        } else {
            gameDAO.createGame(gameName);
        }
    }

    public GameData getGameByName(String gameName, GameDAO gameDAO) throws DataAccessException {
        return gameDAO.getGameByName(gameName);
    }

    public void joinGame(String username, JoinGameRequest joinGameRequest, GameDAO gameDAO) throws DataAccessException, InvalidColorException, ColorTakenException {
        int id = joinGameRequest.gameID();
        GameData gameData = gameDAO.getGameByID(id);

        String desiredColor = joinGameRequest.playerColor();

        // Valid Color Input
        if (desiredColor == null) {
            throw new InvalidColorException("Must Select 'WHITE' or 'BLACK' ");
        }

        if (!desiredColor.equals("WHITE") && !desiredColor.equals("BLACK")) {
            throw new InvalidColorException("Must Select 'WHITE' or 'BLACK' ");
        }

        // Color Already Taken
        if ((desiredColor.equals("WHITE") && gameData.whiteUsername() != null) || (desiredColor.equals("BLACK") && gameData.blackUsername() != null)) {
            throw new ColorTakenException("Color Already Taken");
        }

        gameDAO.joinGame(username, gameData.gameName(), desiredColor);

    }

    public void clearGameData(GameDAO gameDAO) {
        gameDAO.clear();
    }
}
