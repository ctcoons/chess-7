package service;


import dataaccess.DataAccessException;
import dataaccess.SQLUserDAO;
import model.*;

import dataaccess.UserDAO;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    public UserService() {
    }

    public void register(UserData userData, UserDAO userDAO) throws DataAccessException, RegisterException {
        // Look to see if there is a user already in the system

        UserData possibleUser = null;

        try {
            possibleUser = userDAO.getUser(userData.username());
        } catch (DataAccessException e) {
            userDAO.createUser(userData);
        }

        if (possibleUser != null) {
            throw new RegisterException("Username Already Taken");
        }


    }

    public boolean login(LoginRequest loginRequest, UserDAO userDAO) throws DataAccessException {

        UserData userData;

        try {
            userData = userDAO.getUser(loginRequest.username());
        } catch (DataAccessException e) {
            return false;
        }

        if (userDAO instanceof SQLUserDAO) {
            return BCrypt.checkpw(loginRequest.password(), userData.password());
        }

        return userData.password().equals(loginRequest.password());
    }

    public void clearUserData(UserDAO userDAO) {
        userDAO.clear();
    }
}

//        RegisterResult authData = userService.register(userData, userDAO);