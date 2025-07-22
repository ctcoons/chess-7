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
        myDatabase.createUser(new UserData("user2", "pass2", "email2"));
        UserData user = myDatabase.getUser("user2");
        assertEquals("user2", user.username());
        assertTrue(BCrypt.checkpw("pass2", user.password()));
        assertEquals("email2", user.email());
    }

    @Test
    void clear() throws DataAccessException {
        try {
            UserData user = myDatabase.getUser("myUsername");
            assertEquals("myUsername", user.username());
            assertTrue(BCrypt.checkpw("myPassword", user.password()));
            assertEquals("myEmail", user.email());
        } catch (Exception e) {
            fail("Failed test due to exception: " + e);
        }

        myDatabase.clear();

        try {
            UserData user = myDatabase.getUser("myUsername");
            assertNotEquals("myUsername", user.username());
        } catch (DataAccessException e) {
            System.out.println("Exception '" + e + "' caught when accessing invalid username");
        }

    }

    @Test
    void startDatabase() {
    }
}