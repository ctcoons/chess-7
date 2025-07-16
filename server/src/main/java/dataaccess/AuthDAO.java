package dataaccess;


import model.AuthData;

public interface AuthDAO {
    String createAuth(String username) throws DataAccessException;

    AuthData getAuthByUsername(String username) throws DataAccessException;

    void deleteAuth(String authToken) throws DataAccessException;

    boolean validateAuth(String authToken);

    String getAuthByAuthToken(String authToken) throws DataAccessException;

    void clear();
}
