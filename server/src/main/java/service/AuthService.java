package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

public class AuthService {


    public AuthService() {

    }

    public String addAuth(String username, AuthDAO authDAO) throws DataAccessException {
        return authDAO.createAuth(username);
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

    public boolean validateAuth(String authToken, AuthDAO authDAO) throws DataAccessException {
        return authDAO.validateAuth(authToken);
    }

    public void clearAuthData(AuthDAO authDAO) throws DataAccessException {
        authDAO.clear();
    }

}

