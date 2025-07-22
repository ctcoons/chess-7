package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;


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
        Assertions.assertNull(myDatabase.getUser("FAKE_USER"));
        Assertions.assertEquals("myEmail", myDatabase.getUser("myUsername").email());

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

        myDatabase.clear();

        Assertions.assertNull(myDatabase.getUser("myUsername"));

    }

    @Test
    void startDatabase() {
    }

}