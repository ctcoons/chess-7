package server;

import com.google.gson.Gson;

// DATA access Classes
import dataaccess.*;

// Model Classes
import model.AuthData;
import model.RegisterResult;
import model.UserData;


import service.AuthService;
import service.UserService;
import spark.*;

public class Server {

    public AuthDAO authDAO;
    public UserDAO userDAO;
    public GameDAO gameDAO;
    public UserService userService;
    public AuthService authService;

    public Server() {
        this.authDAO = new MemoryAuthDAO();
        this.gameDAO = new MemoryGameDAO();
        this.userDAO = new MemoryUserDAO();
        this.userService = new UserService();
        this.authService = new AuthService();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.post("/user", this::register);

        Spark.init();

        // TODO: Make the Post and Endpoints

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object register(Request request, Response response) throws DataAccessException, NullFieldsException {
        String body = request.body();
        Gson userGson = new Gson();
        UserData userData = userGson.fromJson(body, UserData.class);
        // Make sure there aren't any null fields

        if (userData.username() == null || userData.email() == null || userData.password() == null) {
            throw new NullFieldsException("Must Fill All Fields");
        }

        userService.register(userData, userDAO);

        authService.addAuth(userData.username(), authDAO);
        Gson authGson = new Gson();
        AuthData authData = authService.getAuth(userData.username(), authDAO);

        return authGson.toJson(authData);
    }




    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
