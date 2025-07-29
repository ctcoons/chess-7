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
    private AuthData authData;

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
        facade.register("caleb", "caleb_password", "caleb@email.com");
        this.authData = facade.login("caleb", "caleb_password");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    void register() throws ResponseException {
        facade.register("m", "m", "m");

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.register("m", "m", "m");
        });
    }

    @Test
    void login() throws ResponseException {

        AuthData badAuth = null;

        try {
            badAuth = facade.login("caleb", "bad_password");
        } catch (Exception e) {

        }
        Assertions.assertNull(badAuth);


    }

    @Test
    void logout() throws ResponseException {

        facade.logout(authData.authToken());

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.listGames(authData.authToken());
        });

    }

    @Test
    void createNewGame() throws ResponseException {

        facade.createNewGame("new_game", authData.authToken());

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.createNewGame("new_game", authData.authToken());
        });
    }

    @Test
    void listGames() throws ResponseException {

        facade.createNewGame("new_game", authData.authToken());
        Collection<GameData> games = facade.listGames(authData.authToken());
        Assertions.assertFalse(games.isEmpty());


    }

    @Test
    void joinGame() throws ResponseException {
        CreateGameResponse createGameResponse = facade.createNewGame("ThisNewGame", authData.authToken());
        int gameID = createGameResponse.gameID();
        facade.joinGame(gameID, "WHITE", authData.authToken());

        facade.register("joe", "joe", "joe");
        AuthData newAuthData = facade.login("joe", "joe");


        Assertions.assertThrows(ResponseException.class, () -> {
            facade.joinGame(gameID, "WHITE", newAuthData.authToken());
        });

    }

    @Test
    void getGame() {

    }

    @Test
    void clearApplication() {
    }

}


