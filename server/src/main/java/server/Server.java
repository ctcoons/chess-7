package server;

import dataaccess.*;
import spark.*;

public class Server {
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        this.authDAO = new MemoryAuthDAO();
        this.gameDAO = new MemoryGameDAO();
        this.userDAO = new MemoryUserDAO();


        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
