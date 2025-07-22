package dataaccess;

import model.AuthData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class SQLAuthDAO extends SQLParent implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException {

    }


    @Override
    public String createAuth(String username) throws DataAccessException {
        var statement = "INSERT INTO authData (authToken, username) VALUES (?, ?)";
        String authToken = UUID.randomUUID().toString();

        if (executeUpdate(statement, authToken, username) == 0) {
            return authToken;
        } else {
            throw new DataAccessException("Didn't pass createAuth");
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
                        return null;  // No auth found for this username
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Failed getAuth with error: " + e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM authData WHERE authToken = ?";
        executeUpdate(statement, authToken);

    }

    @Override
    public boolean validateAuth(String authToken) {
        return false;
    }

    @Override
    public String getAuthByAuthToken(String authToken) throws DataAccessException {
        return "";
    }

    @Override
    public void clear() {

    }


}
