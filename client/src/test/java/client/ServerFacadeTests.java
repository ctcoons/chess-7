package client;

import dataaccess.DataAccessException;
import exception.ResponseException;
import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


public class ServerFacadeTests {

    private static int port;
    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void clearServer() throws DataAccessException {
        try {
            String method = "DELETE";
            var serverUrl = "http://localhost:" + port;
            URL url = (new URI(serverUrl + "/db")).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = "{}";
            OutputStream reqBody = http.getOutputStream();
            reqBody.write(reqData.getBytes());
            http.connect();
        } catch (Exception e) {
            throw new DataAccessException("Failed To Clear DB in Setup");
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    void register() throws ResponseException {
        AuthData authData = facade.register("jacob", "caleb_password", "caleb@email.com");
        Assertions.assertEquals("jacob", authData.username());


    }

    @Test
    void login() {
    }

    @Test
    void logout() {
    }

    @Test
    void createNewGame() {
    }

    @Test
    void listGames() {
    }

    @Test
    void joinGame() {
    }

    @Test
    void observeGame() {
    }

    @Test
    void clearApplication() {
    }

}


