package server;

import chess.ChessGame;
import com.google.gson.Gson;

import exception.ResponseException;
import model.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }


    public AuthData register(String username, String password, String email) throws ResponseException {
        var path = "/user";
        var regReq = new RegisterRequest(username, password, email);
        return this.makeRequest("POST", path, regReq, AuthData.class);
    }


    public AuthData login(String username, String password) throws ResponseException {
        var path = "/session";
        var logReq = new LoginRequest(username, password);
        return this.makeRequest("POST", path, logReq, AuthData.class);
    }


    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        this.makeRequest("DELETE", path, authToken, null);
    }


    public CreateGameResponse createNewGame(String gameName, String authToken) throws ResponseException {
        CreateGameRequestFacade createRequest = new CreateGameRequestFacade(gameName, authToken);
        var path = "/game";
        return this.makeRequest("POST", path, createRequest, CreateGameResponse.class);
    }

    private static class GameListResponse {
        Collection<GameData> games;

        public Collection<GameData> getGames() {
            return games;
        }
    }

    public Collection<GameData> listGames(String authToken) throws ResponseException {
        var path = "/game";
        GameListResponse response = this.makeRequest("GET", path, authToken, GameListResponse.class);
        if (response != null) {
            return response.getGames();
        } else {
            throw new ResponseException(400, "Failed to get Games");
        }
    }


    public void joinGame() throws ResponseException {
    }


    public void observeGame() throws ResponseException {
    }


    public void clearApplication() throws ResponseException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (request instanceof String && (method.equals("GET") || method.equals("DELETE"))) {
                http.setRequestProperty("Authorization", (String) request);
            } else if (request instanceof CreateGameRequestFacade) {
                http.setRequestProperty("Authorization", ((CreateGameRequestFacade) request).authToken());
                CreateGameRequest gameRequest = new CreateGameRequest(((CreateGameRequestFacade) request).gameName());
                writeBody(gameRequest, http);
            } else {
                writeBody(request, http);
            }

            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(respErr);
                }
            }

            throw new ResponseException(status, "other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }


}
