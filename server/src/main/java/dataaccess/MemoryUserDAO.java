package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {

    // Maps Username to UserData

    private HashMap<String, UserData> users = new HashMap<>();

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (!users.containsKey(username)) {
            throw new DataAccessException("Username Not Registered");
        }
        return users.get(username);
    }

    @Override
    public void createUser(UserData user) {
        user = new UserData(user.username(), user.password(), user.email());
        users.put(user.username(), user);
    }

    @Override
    public void clear() {
        users.clear();
    }


}
