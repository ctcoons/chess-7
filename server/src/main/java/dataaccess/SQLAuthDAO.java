package dataaccess;

import model.AuthData;

import java.util.UUID;

public class SQLAuthDAO extends SQLParent implements AuthDAO {

    public SQLAuthDAO() {

    }


    @Override
    public String createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO authData (authToken, username) VALUES (?, ?)";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                ps.setString(2, username);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    return authToken;
                } else {
                    throw new DataAccessException("Failed to create auth");  // No auth found for this username
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Failed createAuth with error: " + e);
        }
    }

    @Override
    public AuthData getAuthByUsername(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken FROM authData WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String authToken = rs.getString("authToken");
                        return new AuthData(username, authToken);
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Failed getAuth with error: " + e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String statement = "DELETE FROM authData WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {

                ps.setString(1, authToken);
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected == 0) {
                    throw new DataAccessException("No Such Auth Token in DB");
                }

            }
        } catch (Exception e) {
            throw new DataAccessException("Failed in delete Auth: " + e);
        }


    }

    @Override
    public boolean validateAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT 1 FROM authData WHERE authToken = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    // No auth found for this username
                    return rs.next();
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("failed to validate auth");
        }

    }

    @Override
    public String getAuthByAuthToken(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username FROM authData WHERE authToken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("username");
                    } else {
                        return null;  // No auth found for this username
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Failed get username from auth with error: " + e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement1 = "DELETE FROM authData";
        try {
            executeUpdate(statement1);
        } catch (DataAccessException e) {
            System.out.println("Clear Auth Data Throws Error--> " + e);
            throw new DataAccessException("Failed to clear authData due to error: " + e);
        }
    }


}
