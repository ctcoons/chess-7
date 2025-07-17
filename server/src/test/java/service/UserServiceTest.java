package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.LoginRequest;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    UserService userService;
    UserData userData;
    String username;
    String password;
    String email;
    UserDAO userDAO;


    @BeforeEach
    void initialize() {
        userService = new UserService();
        password = "password";
        username = "username";
        email = "email";
        userData = new UserData(username, password, email);
        userDAO = new MemoryUserDAO();
    }

    @Test
    void register() throws DataAccessException, RegisterException {
        userService.register(userData, userDAO);
        Assertions.assertEquals(userData.toString(), userDAO.getUser(username).toString());

        assertThrows(RegisterException.class, () -> {
            userService.register(userData, userDAO);
        });

    }

    @Test
    void login() throws RegisterException, DataAccessException {
        userService.register(userData, userDAO);
        Assertions.assertTrue(userService.login(new LoginRequest(username, password), userDAO));

        Assertions.assertFalse(userService.login(new LoginRequest("fakeUsername", "fakePassword"), userDAO));

    }

    @Test
    void clearUserData() throws DataAccessException {
        userService.clearUserData(userDAO);
        Assertions.assertFalse(userService.login(new LoginRequest("fakeUsername", "fakePassword"), userDAO));
    }
}