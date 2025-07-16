package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

public class AuthService {


    public AuthService() {

    }

    public void addAuth(String username, AuthDAO authDAO) throws DataAccessException {

        AuthData authData = null;

        try {
            authData = getAuthByUsername(username, authDAO);
        } catch (DataAccessException ignored) {

        }

        if (authData == null) {
            authDAO.createAuth(username);
        }

    }

    public AuthData getAuthByUsername(String username, AuthDAO authDAO) throws DataAccessException {
        return authDAO.getAuthByUsername(username);
    }

    public String getUsernameByAuthToken(String authToken, AuthDAO authDAO) throws DataAccessException {
        return authDAO.getAuthByAuthToken(authToken);
    }


    public void deleteAuth(String authToken, AuthDAO authDAO) throws DataAccessException {
        authDAO.deleteAuth(authToken);
    }

    public boolean validateAuth(String authToken, AuthDAO authDAO) {
        return authDAO.validateAuth(authToken);
    }

    public void clearAuthData(AuthDAO authDAO){
        authDAO.clear();
    }

}
