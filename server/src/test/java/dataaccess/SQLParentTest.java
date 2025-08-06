package dataaccess;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SQLParentTest {

    @Test
    void clearDatabase() throws DataAccessException {
        SQLParent sqlParent = new SQLParent();
        sqlParent.clearDatabase();
    }
}