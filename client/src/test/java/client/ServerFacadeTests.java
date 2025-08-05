package client;

import exception.ResponseException;
import model.AuthData;
import model.CreateGameResponse;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;

import java.util.Collection;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private AuthData authData;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
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
    static void stopServer() throws ResponseException {
        facade.clearApplication("secretpassword");
        server.stop();
    }


    @Test
    void register() throws ResponseException {
        facade.register("m", "m", "m");

        Assertions.assertThrows(ResponseException.class, () -> facade.register("m", "m", "m"));
    }

    @Test
    void login() throws ResponseException {

        AuthData badAuth = null;

        try {
            badAuth = facade.login("caleb", "bad_password");
        } catch (Exception ignored) {

        }
        Assertions.assertNull(badAuth);
        Assertions.assertNotNull(facade.login("caleb", "caleb_password"));


    }

    @Test
    void logout() throws ResponseException {

        facade.logout(authData.authToken());

        Assertions.assertThrows(ResponseException.class, () -> facade.listGames(authData.authToken()));

    }

    @Test
    void createNewGame() throws ResponseException {

        facade.createNewGame("new_game", authData.authToken());

        Assertions.assertThrows(ResponseException.class, () -> facade.createNewGame("new_game", authData.authToken()));
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


        Assertions.assertThrows(ResponseException.class, () -> facade.joinGame(gameID, "WHITE", newAuthData.authToken()));

    }

    @Test
    void getGame() throws ResponseException {
        CreateGameResponse createGameResponse = facade.createNewGame("ThisNewGame", authData.authToken());
        int gameID = createGameResponse.gameID();

        GameData gameData = facade.getGame(gameID, authData.authToken());
        Assertions.assertNotNull(gameData.gameName());

    }

    @Test
    void clearApplication() throws ResponseException {
        facade.clearApplication("secretpassword");
        facade.register("caleb", "caleb_password", "caleb@email.com");
        this.authData = facade.login("caleb", "caleb_password");
        Collection<GameData> games = facade.listGames(authData.authToken());
        Assertions.assertTrue(games.isEmpty());
    }

}


