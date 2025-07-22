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
        myDatabase.deleteAuth(authData.authToken());
        System.out.println("Passed Delete Auth");


    }

    @Test
    void validateAuth() {
    }

    @Test
    void getAuthByAuthToken() {
    }

    @Test
    void clear() {
    }
}