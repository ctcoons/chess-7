package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class SQLGameDAOTest {

    SQLGameDAO myDatabase;

    @BeforeEach
    void initialize() throws DataAccessException {
        this.myDatabase = new SQLGameDAO();
        myDatabase.clearDatabase();
    }

    @Test
    void listGames() throws DataAccessException {
        myDatabase.createGame("NewGame2");
        Collection<GameData> listOfGames = myDatabase.listGames();
        ArrayList<String> gameNames = new ArrayList<>();
        for (GameData gameData : listOfGames) {
            String name = gameData.gameName();
            gameNames.add(name);
        }
        Assertions.assertTrue(gameNames.contains("NewGame2"));
    }

    @Test
    void createGame() throws DataAccessException {
        int initialSize = myDatabase.listGames().size();
        myDatabase.createGame("NewGame1");
        int sizeAfterCreate = myDatabase.listGames().size();
        assertEquals(sizeAfterCreate, initialSize + 1);
    }

    @Test
    void clear() throws DataAccessException {
        myDatabase.createGame("NewGame4");
        assertFalse(myDatabase.listGames().isEmpty());
        myDatabase.clear();
        assertTrue(myDatabase.listGames().isEmpty());
    }

    @Test
    void contains() {
        myDatabase.createGame("NewGame5");
        assertTrue(myDatabase.contains("NewGame5"));
        assertFalse(myDatabase.contains("FAKE_GAME_NAME_DOES_NOT_EXIST"));
    }

    @Test
    void getGameByName() throws DataAccessException {
        myDatabase.createGame("NewGame6");
        GameData game6data = myDatabase.getGameByName("NewGame6");
        Assertions.assertEquals("NewGame6", game6data.gameName());
        Assertions.assertNull(game6data.whiteUsername());
        Assertions.assertNull(game6data.blackUsername());

    }

    @Test
    void getGameByID() throws DataAccessException {
        myDatabase.createGame("NewGame7");
        GameData game7data = myDatabase.getGameByName("NewGame7");
        int gameID = game7data.gameID();
        Assertions.assertEquals("NewGame7", myDatabase.getGameByID(gameID).gameName());
    }

    @Test
    void joinGame() throws DataAccessException {
        myDatabase.createGame("NewGame7");
        GameData game7data = myDatabase.getGameByName("NewGame7");
        Assertions.assertNull(game7data.blackUsername(), game7data.whiteUsername());
        myDatabase.joinGame("username1", "NewGame7", "WHITE");
        GameData game7dataAfterJoin = myDatabase.getGameByName("NewGame7");
        Assertions.assertEquals("username1", game7dataAfterJoin.whiteUsername());
    }
}