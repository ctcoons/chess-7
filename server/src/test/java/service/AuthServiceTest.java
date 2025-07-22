package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    public AuthService authService;
    public AuthDAO authDAO;
    public String authToken1;
    public String authToken2;
    public String username1;
    public String username2;

    @BeforeEach
    void initialize() throws DataAccessException {
        authService = new AuthService();
        authDAO = new MemoryAuthDAO();
        this.username1 = "username1";
        this.authToken1 = authService.addAuth(username1, authDAO);
        this.username2 = "username2";
        this.authToken2 = authService.addAuth(username2, authDAO);

    }


    @Test
    void addAuth() throws DataAccessException {

        Assertions.assertNotNull(username1);
        Assertions.assertNotEquals(authToken1, authToken2);

    }

    @Test
    void getAuthByUsername() throws DataAccessException {

        String correctToken1 = authToken1;
        String nonExistentUsername = "doenstExistUser";

        String fetchedToken = authService.getAuthByUsername(username1, authDAO).authToken();

        Assertions.assertEquals(correctToken1, fetchedToken);
        assertThrows(DataAccessException.class, () -> {
            String s = authService.getAuthByUsername(nonExistentUsername, authDAO).authToken();
            System.out.println(s);
        });

    }

    @Test
    void getUsernameByAuthToken() throws DataAccessException {

        String correctAuth = authToken1;
        String correctUser = username1;
        String attempt = authService.getUsernameByAuthToken(correctAuth, authDAO);

        Assertions.assertEquals(attempt, correctUser);

        assertThrows(DataAccessException.class, () -> {
            authService.getUsernameByAuthToken("FakeToken", authDAO);
        });


    }

    @Test
    void deleteAuth() throws DataAccessException {
        authService.deleteAuth(authToken1, authDAO);


        assertThrows(DataAccessException.class, () -> {
            authService.getUsernameByAuthToken(authToken1, authDAO);
        });

    }

    @Test
    void validateAuth() throws DataAccessException {
        Assertions.assertTrue(authService.validateAuth(authToken1, authDAO));
        Assertions.assertFalse(authService.validateAuth("InvalidToken", authDAO));
    }

    @Test
    void clearAuthData() throws DataAccessException {
        authService.clearAuthData(authDAO);


        assertThrows(DataAccessException.class, () -> {
            authService.getUsernameByAuthToken(authToken1, authDAO);
        });
    }
}