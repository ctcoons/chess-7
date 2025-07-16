package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO{
    // Maps Key:authToken --> Value:username
    private HashMap<String, String> authHashMap = new HashMap<>();


    @Override
    public String createAuth(String username) throws DataAccessException {

        String authToken = UUID.randomUUID().toString();
        authHashMap.put(authToken, username);
        System.out.println("AFTER CREATE: --> " + authHashMap);
        return authToken;

    }

    @Override
    public AuthData getAuthByUsername(String username) throws DataAccessException {

        for (var item : authHashMap.entrySet()) {
            String curUser = item.getValue();
            if (curUser.equals(username)) {
                return new AuthData(item.getValue(), item.getKey());
            }
        }
        throw new DataAccessException("Couldn't Find Auth with Given Username");


    }

    @Override
    public String getAuthByAuthToken(String authToken) throws DataAccessException {

        if (authHashMap.containsKey(authToken)) {
            return authHashMap.get(authToken);
        } else {
            throw new DataAccessException("Couldn't find User Given Auth Token");
        }

    }

    @Override
    public void clear() {
        authHashMap.clear();
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

        if (authHashMap.containsKey(authToken)) {
            authHashMap.remove(authToken);
        } else {
            throw new DataAccessException("Deletion Failure");
        }

    }

    @Override
    public boolean validateAuth(String authToken) {
        return authHashMap.containsKey(authToken);
    }



}
