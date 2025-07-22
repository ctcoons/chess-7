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

        if (userDAO.getUser(userData.username()) != null) {
            throw new RegisterException("Username Already Taken");
        } else {
            userDAO.createUser(userData);
        }


    }

    public boolean login(LoginRequest loginRequest, UserDAO userDAO) throws DataAccessException {

        UserData userData = userDAO.getUser(loginRequest.username());

        if (userData == null) {
            return false;
        }

        if (userDAO instanceof SQLUserDAO) {
            return BCrypt.checkpw(loginRequest.password(), userData.password());
        }

        return userData.password().equals(loginRequest.password());
    }

    public void clearUserData(UserDAO userDAO) throws DataAccessException {
        userDAO.clear();
    }
}

//        RegisterResult authData = userService.register(userData, userDAO);