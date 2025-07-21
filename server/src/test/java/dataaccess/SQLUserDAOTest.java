package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import service.AuthService;

import static org.junit.jupiter.api.Assertions.*;

class SQLUserDAOTest {

    SQLUserDAO myDatabase;

    @BeforeEach
    void initialize() throws DataAccessException {
        this.myDatabase = new SQLUserDAO();
        myDatabase.clear();
        UserData user = new UserData("myUsername", "myPassword", "myEmail");
        myDatabase.createUser(user);
    }

    @Test
    void getUser() throws DataAccessException {
        UserData user = myDatabase.getUser("myUsername");
        assertEquals("myUsername", user.username());
        assertTrue(BCrypt.checkpw("myPassword", user.password()));
        assertEquals("myEmail", user.email());

    }

    @Test
    void createUser() throws DataAccessException {

    }

    @Test
    void clear() {
    }

    @Test
    void startDatabase() {
    }
}