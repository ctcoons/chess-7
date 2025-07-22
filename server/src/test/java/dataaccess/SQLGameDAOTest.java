package dataaccess;

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
    void clear() {
    }

    @Test
    void contains() {
    }

    @Test
    void getGameByName() {
    }

    @Test
    void getGameByID() {
    }

    @Test
    void joinGame() {
    }
}