package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SQLAuthDAOTest {

    SQLAuthDAO myDatabase;

    @BeforeEach
    void initialize() throws DataAccessException {
        this.myDatabase = new SQLAuthDAO();
        myDatabase.clearDatabase();
    }


    @Test
    void createAuth() {
        String auth = null;
        try {
            auth = myDatabase.createAuth("username1");
        } catch (Exception e) {
            System.out.println("Error: " + e);
            fail();
        }
        System.out.println("Auth for username1: " + auth);
    }

    @Test
    void getAuthByUsername() {
        try {
            System.out.println("Testing get auth by Username...");
            myDatabase.createAuth("username2");
            AuthData authData = myDatabase.getAuthByUsername("username2");
            System.out.println("The auth token retrieved in the test was: " + authData.authToken());
            assertNotNull(authData.authToken());
        } catch (DataAccessException e) {
            fail("Failed due to exception " + e);
        }

    }

    @Test
    void deleteAuth() throws DataAccessException {
        myDatabase.createAuth("username3");
        AuthData authData = myDatabase.getAuthByUsername("username3");
        Assertions.assertTrue(myDatabase.validateAuth(authData.authToken()));
        myDatabase.deleteAuth(authData.authToken());
        Assertions.assertFalse(myDatabase.validateAuth(authData.authToken()));

    }

    @Test
    void validateAuth() throws DataAccessException {
        myDatabase.createAuth("username4");
        AuthData authData = myDatabase.getAuthByUsername("username4");
        Assertions.assertTrue(myDatabase.validateAuth(authData.authToken()));
    }

    @Test
    void getAuthByAuthToken() throws DataAccessException {
        myDatabase.createAuth("username5");
        AuthData authData = myDatabase.getAuthByUsername("username5");
        String authToken = authData.authToken();
        String username = myDatabase.getAuthByAuthToken(authToken);
        assertEquals("username5", username);

    }

    @Test
    void clear() throws DataAccessException {
        myDatabase.createAuth("username4");
        AuthData authData = myDatabase.getAuthByUsername("username4");
        Assertions.assertTrue(myDatabase.validateAuth(authData.authToken()));

        myDatabase.clear();

        Assertions.assertFalse(myDatabase.validateAuth(authData.authToken()));

    }
}