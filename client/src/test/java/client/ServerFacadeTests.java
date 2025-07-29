package client;

import dataaccess.DataAccessException;
import exception.ResponseException;
import model.AuthData;
import model.CreateGameResponse;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;


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
    public void clearServer() throws ResponseException {
        facade.clearApplication("secretpassword");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    void register() throws ResponseException {
        AuthData authData = facade.register("caleb", "caleb_password", "caleb@email.com");
        Assertions.assertEquals("caleb", authData.username());

    }

    @Test
    void login() throws ResponseException {
        facade.register("caleb", "caleb_password", "caleb@email.com");
        AuthData authData = facade.login("caleb", "caleb_password");
        Assertions.assertNotNull(authData);

        AuthData badAuth = null;

        try {
            badAuth = facade.login("caleb", "bad_password");
        } catch (Exception e) {

        }
        Assertions.assertNull(badAuth);


    }

    @Test
    void logout() throws ResponseException {
        facade.register("caleb", "caleb_password", "caleb@email.com");
        AuthData authData = facade.login("caleb", "caleb_password");
        Assertions.assertNotNull(authData);
        facade.logout(authData.authToken());

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.listGames(authData.authToken());
        });

    }

    @Test
    void createNewGame() {


    }

    @Test
    void listGames() {
    }

    @Test
    void joinGame() throws ResponseException {
        AuthData authData = facade.register("caleb", "caleb_password", "caleb@email.com");
        CreateGameResponse createGameResponse = facade.createNewGame("ThisNewGame", authData.authToken());
        int gameID = createGameResponse.gameID();
        facade.joinGame(gameID, "WHITE", authData.authToken());
    }

    @Test
    void observeGame() {
    }

    @Test
    void clearApplication() {
    }

}


