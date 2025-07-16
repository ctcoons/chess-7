package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

public class AuthService {


    public AuthService() {

    }

    public void addAuth(String username, AuthDAO authDAO) throws DataAccessException {

        // Check if they are already logged in
        // If So, Delete it, and then make a new auth token
        // If Not, Make a new auth token

        AuthData authData = null;

        try {
            authData = getAuthByUsername(username, authDAO);
        } catch (DataAccessException ignore) {
            authDAO.createAuth(username);
        }

        if (authData != null) {
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
