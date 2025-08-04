package client;

import client.websocket.NotificationHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import websocket.messages.ServerMessage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChessClientTest {

    static ChessClient client;

    @BeforeAll
    public static void init() {
        client = new ChessClient("http://localhost:8080", new NotificationHandler() {
            @Override
            public void notify(ServerMessage serverMessage) {

            }
        });
    }

    @Test
    void quit() {
    }

    @Test
    void register() {
    }

    @Test
    void login() {
        client.eval("join 33 BLACK");
    }

    @Test
    void logout() {
    }

    @Test
    void create() {
    }

    @Test
    void listGames() {
    }

    @Test
    void join() {
    }

    @Test
    void observe() {
    }

    @Test
    void help() {
    }
}