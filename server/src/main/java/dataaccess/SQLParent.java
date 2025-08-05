package dataaccess;

import model.UserData;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

import java.sql.SQLException;

public class SQLParent {

    public SQLParent() {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            System.out.println("WARNING! SQL PARENT CONSTRUCTOR THREW ERROR: " + e);
        }
    }

    private final String[] createStatements = {

            """
            CREATE TABLE IF NOT EXISTS  userData (
              `username` varchar(256) NOT NULL UNIQUE,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              PRIMARY KEY (`username`),
              INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,

            """
            CREATE TABLE IF NOT EXISTS  authData (
              `authToken` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              PRIMARY KEY (`authToken`),
              INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,

            """
            CREATE TABLE IF NOT EXISTS  gameData (
              `id` int NOT NULL AUTO_INCREMENT,
              `gameName` varchar(256) NOT NULL UNIQUE,
              `whiteUsername` varchar(256) DEFAULT NULL,
              `blackUsername` varchar(256) DEFAULT NULL,
              `winner` ENUM('WHITE', 'BLACK', 'DRAW') DEFAULT NULL,
              `json` TEXT NOT NULL,
              PRIMARY KEY (`id`),
              INDEX(gameName)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """


    };


    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("DataAccessException: " + ex);
        }
    }

    int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    switch (param) {
                        case String p -> ps.setString(i + 1, p);
                        case Integer p -> ps.setInt(i + 1, p);
                        case UserData p -> ps.setString(i + 1, p.toString());
                        case null -> ps.setNull(i + 1, NULL);
                        default -> {
                            throw new DataAccessException("Invalid Type Used");
                        }
                    }
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();

//                find out what rs.next() does
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e);
        }
    }

    public void clearDatabase() throws DataAccessException {

        executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
        executeUpdate("DROP TABLE IF EXISTS authData");
        executeUpdate("DROP TABLE IF EXISTS userData");
        executeUpdate("DROP TABLE IF EXISTS gameData");
        executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        configureDatabase();


    }


}

