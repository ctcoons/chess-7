package dataaccess;

import model.AuthData;

public class SQLAuthDAO extends SQLParent implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException {

    }


    @Override
    public String createAuth(String username) throws DataAccessException {
        return "";
    }

    @Override
    public AuthData getAuthByUsername(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

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
