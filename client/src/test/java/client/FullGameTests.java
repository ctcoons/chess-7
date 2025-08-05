package client;

import exception.ResponseException;
import model.AuthData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;
import server.ServerFacade;

import java.awt.event.ActionListener;

public class FullGameTests {

    private static Server server;
    static ServerFacade facade;
    private AuthData authData;
    private static Repl repl;
    private static ChessClient client;

    @BeforeAll
    static void init() {
        server = new Server();
        int port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
        repl = new Repl("http://localhost:" + port);
        client = repl.client;
    }

    @BeforeEach
    void clearDatabase() throws ResponseException {
        facade.clearApplication("secretpassword");
    }

    @AfterAll
    static void stopServer() throws ResponseException {
        facade.clearApplication("secretpassword");
        server.stop();
    }


    @Test
    void run() throws ResponseException {
        client.register("caleb", "caleb", "caleb");
        client.create(new String[]{"newGame"});
        client.observe(new String[]{"1"});
    }


}
