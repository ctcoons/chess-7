package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO{
    private HashMap<String, String> authHashMap = new HashMap<>();


    @Override
    public void createAuth(String username) throws DataAccessException {

        String authToken = UUID.randomUUID().toString();
        authHashMap.put(username, authToken);

        System.out.println("AFTER CREATE: --> " + authHashMap);

    }

    @Override
    public AuthData getAuthByUsername(String username) throws DataAccessException {
        if (!authHashMap.containsKey(username)) {
            throw new DataAccessException("Auth Not Found For This Username");
        }

        return new AuthData(username, authHashMap.get(username));
    }

    @Override
    public String getAuthByAuthToken(String authToken) throws DataAccessException {

        for (var item : authHashMap.entrySet()) {
            String curToken = item.getValue();
            if (curToken.equals(authToken)) {
                return item.getKey();
            }
        }

        throw new DataAccessException("No Such Token");

    }

    @Override
    public void clear() {
        authHashMap.clear();
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

        String username = null;
        for (var item : authHashMap.entrySet()) {
            if(item.getValue().equals(authToken)){
                username = item.getKey();
                break;
            }
        }


        System.out.println("*** Auth Token: " + authToken);
        System.out.println("*** Username: " + username);
        System.out.println(authHashMap);

        if (username == null) {
            throw new DataAccessException("This Token Is Not In The Database");
        }

        authHashMap.remove(username, authToken);

    }

    @Override
    public boolean validateAuth(String authToken) {
        return authHashMap.containsValue(authToken);
    }



}
