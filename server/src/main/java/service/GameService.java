package service;

import chess.ChessMove;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import model.JoinGameRequest;
import model.MakeMoveResponse;
import model.ResignRequest;
import server.BadRequestException;

import java.util.Collection;

public class GameService {

    public Collection<GameData> listGames(GameDAO gameDAO) throws DataAccessException {
        return gameDAO.listGames();
    }

    public void leaveGame(int gameId, String username, GameDAO gameDAO) throws DataAccessException {
        gameDAO.leaveGame(gameId, username);
    }

    public void createGame(String gameName, GameDAO gameDAO) throws GameAlreadyExistsException, DataAccessException {
        if (gameDAO.containsGameByName(gameName)) {
            throw new GameAlreadyExistsException("Game Name Taken");
        } else {
            gameDAO.createGame(gameName);
        }
    }

    public GameData getGameByName(String gameName, GameDAO gameDAO) throws DataAccessException {
        return gameDAO.getGameByName(gameName);
    }

    public GameData getGameById(int id, GameDAO gameDAO) throws DataAccessException {
        return gameDAO.getGameByID(id);
    }

    public void joinGame(String username, JoinGameRequest joinGameRequest, GameDAO gameDAO) throws DataAccessException, InvalidColorException, ColorTakenException, BadRequestException {

        int id = joinGameRequest.gameID();

        // Check to see if the ID is good
        if (!gameDAO.containsGameById(id)) {
            throw new BadRequestException("No Game By ID: " + id);
        }

        GameData gameData = gameDAO.getGameByID(id);

        String desiredColor = joinGameRequest.playerColor();

        // Valid Color Input check for NULL first then must be BLACK or WHITE
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

    public void clearGameData(GameDAO gameDAO) throws DataAccessException {
        gameDAO.clear();
    }

    public MakeMoveResponse makeMove(int gameId, ChessMove chessMove, GameDAO gameDAO) throws DataAccessException {
        return gameDAO.makeMove(gameId, chessMove);
    }

    public String resign(int gameId, String resigner, GameDAO gameDAO) throws DataAccessException {
        return gameDAO.resign(gameId, resigner);
    }
}
