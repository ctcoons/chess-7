package service;

import chess.ChessGame;
import dataaccess.*;
import model.GameData;
import model.JoinGameRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class GameServiceTest {

    public GameService gameService;
    public GameDAO gameDAO;
    public ChessGame chessGame;
    public GameData gameData;

    @BeforeEach
    void initialize() throws GameAlreadyExistsException {
        gameService = new GameService();
        gameDAO = new MemoryGameDAO();
        chessGame = new ChessGame();
        gameService.createGame("newGame", gameDAO);
        gameData = new GameData(1234, null, null, "newGame", chessGame);

    }

    @Test
    void listGames() throws DataAccessException {
        Assertions.assertEquals(1, gameService.listGames(gameDAO).toArray().length);
        Assertions.assertNotEquals(8, gameService.listGames(gameDAO).toArray().length);

    }

    @Test
    void createGame() throws GameAlreadyExistsException, DataAccessException {
        gameService.createGame("NewGame2", gameDAO);
        Assertions.assertEquals(2, gameService.listGames(gameDAO).toArray().length);

        assertThrows(GameAlreadyExistsException.class, () -> {
            gameService.createGame("NewGame2", gameDAO);
        });
    }

    @Test
    void getGameByName() throws DataAccessException {
        Assertions.assertEquals(gameData, gameService.getGameByName("newGame", gameDAO));

        assertThrows(DataAccessException.class, () -> {
            gameService.getGameByName("FalseGame", gameDAO);
        });
    }

//
//    @Test
//    void getGameByID() {
//    }

    @Test
    void joinGame() throws ColorTakenException, InvalidColorException, DataAccessException {
        gameService.joinGame("username", new JoinGameRequest("WHITE", 1234), gameDAO);
        Assertions.assertEquals("username", gameDAO.getGameByID(1234).whiteUsername());

        assertThrows(ColorTakenException.class, () -> {
            gameService.joinGame("username2", new JoinGameRequest("WHITE", 1234), gameDAO);
        });


    }

    @Test
    void clearGameData() throws DataAccessException {
        gameService.clearGameData(gameDAO);
        Assertions.assertEquals(0, gameService.listGames(gameDAO).toArray().length);
    }
}