package server;

import com.google.gson.Gson;

// DATA access Classes
import dataaccess.*;

// Model Classes
import model.*;


import service.*;
import spark.*;

import java.util.Collection;
import java.util.Map;

public class Server {

    public AuthDAO authDAO;
    public UserDAO userDAO;
    public GameDAO gameDAO;
    public UserService userService;
    public AuthService authService;
    public GameService gameService;

    public Server() {
        this.authDAO = new MemoryAuthDAO();
        this.gameDAO = new MemoryGameDAO();
        this.userDAO = new MemoryUserDAO();
        this.userService = new UserService();
        this.authService = new AuthService();
        this.gameService = new GameService();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearApplication);


        // EXCEPTION HANDLER
        Spark.exception(Exception.class, this::errorHandler);
        Spark.notFound((req, res) -> {
            var msg = String.format("[%s] %s not found", req.requestMethod(), req.pathInfo());
            return errorHandler(new Exception(msg), req, res);
        });

        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object clearApplication(Request request, Response response) {

        authService.clearAuthData(authDAO);
        gameService.clearGameData(gameDAO);
        userService.clearUserData(userDAO);

        return "{}";
    }

    private Object joinGame(Request request, Response response) throws NotAuthorizedException, DataAccessException, InvalidColorException, ColorTakenException {
        // Check to see if the Game Exists
        // Check to see if the Color is available
        // Return the response
        String authToken = request.headers("Authorization");

        // √ Authenticate the User
        if (authService.validateAuth(authToken, authDAO)) {

            String username = authService.getUsernameByAuthToken(authToken, authDAO);

            JoinGameRequest joinGameRequest = fromJson(request, JoinGameRequest.class);
            gameService.joinGame(username, joinGameRequest, gameDAO);

            return "{}";
        } else {
            System.out.println("Join Game Exception Thrown");
            throw new NotAuthorizedException("User Not Authorized");
        }
    }

    private Object createGame(Request request, Response response) throws NotAuthorizedException, GameAlreadyExistsException, DataAccessException, NullFieldsException {
        String authToken = request.headers("Authorization");

        if (authService.validateAuth(authToken, authDAO)) {

            CreateGameRequest createGameRequest = fromJson(request, CreateGameRequest.class);

            if (createGameRequest.gameName() == null) {
                throw new NullFieldsException("Must Give a Game Name");
            }


            gameService.createGame(createGameRequest.gameName(), gameDAO);
            GameData gameData = gameService.getGameByName(createGameRequest.gameName(), gameDAO);
            int ID = gameData.gameID();

            CreateGameResponse createGameResponse = new CreateGameResponse(ID);
            Gson gameResGson = new Gson();
            return gameResGson.toJson(createGameResponse);

        } else {
            System.out.println("Create Game Exception Thrown");
            throw new NotAuthorizedException("Not Authorized");
        }
    }

    private Object listGames(Request request, Response response) throws NotAuthorizedException, DataAccessException {

        String authToken = request.headers("Authorization");

        if (authService.validateAuth(authToken, authDAO)) {

            Collection<GameData> chessGameCollection;
            chessGameCollection = gameService.listGames(gameDAO);
            Gson gamesGson = new Gson();

            Map<String, Object> mapResult = Map.of("games", chessGameCollection);
            return gamesGson.toJson(mapResult);


        } else {
            throw new NotAuthorizedException("Not Authorized");
        }
    }


    // *** LOGOUT HANDLER ***
    private Object logout(Request request, Response response) throws LogoutFailureException {

        String authToken = request.headers("Authorization");

        try {
            authService.deleteAuth(authToken, authDAO);
        } catch (DataAccessException e) {
            response.status(401);
            throw new LogoutFailureException("Logout Failed");
        }

//        return response;
        return "{}";
    }

    // *** REGISTER HANDLER ***
    private Object login(Request request, Response response) throws DataAccessException, IncorrectCredentialsException, BadRequestException {

        LoginRequest loginRequest = fromJson(request, LoginRequest.class);

        if (loginRequest.username() == null || loginRequest.password() == null) {
            response.status(401);
            throw new BadRequestException("Must give a username and password");
        }


        try {
            if (userService.login(loginRequest, userDAO)) {
                String newAuthToken = authService.addAuth(loginRequest.username(), authDAO);

                AuthData authData = new AuthData(authService.getUsernameByAuthToken(newAuthToken, authDAO), newAuthToken);
                Gson authGson = new Gson();

                return authGson.toJson(authData);

            } else {
                response.status(401);
                throw new IncorrectCredentialsException("Incorrect User Name and/or Password");
            }
        } catch (DataAccessException e) {
            response.status(401);
            throw e;
        }

    }

    // *** REGISTER HANDLER ***
    private Object register(Request request, Response response) throws DataAccessException, NullFieldsException, RegisterException, BadRequestException {
        UserData userData = fromJson(request, UserData.class);

        // Make sure there aren't any null fields
        if (userData.username() == null || userData.email() == null || userData.password() == null) {
            throw new BadRequestException("Must Fill All Fields");
        }


        userService.register(userData, userDAO);
        authService.addAuth(userData.username(), authDAO);


        Gson authGson = new Gson();
        AuthData authData = authService.getAuthByUsername(userData.username(), authDAO);

        return authGson.toJson(authData);
    }

    public Object errorHandler(Exception e, Request req, Response res) {

        int status = switch (e) {
            case IncorrectCredentialsException ignored -> 401;
            case DataAccessException ignored -> 400;
            case BadRequestException ignored -> 400;
            case RegisterException ignored -> 403;
            case LogoutFailureException ignored -> 401;
            case NotAuthorizedException ignored -> 401;
            case NullFieldsException ignored -> 400;
            case ColorTakenException ignored -> 403;
            case InvalidColorException ignored -> 400;

            default -> 500;
        };

        var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage()), "success", false));
        res.type("application/json");
        res.status(status);
        res.body(body);
        return body;
    }


    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private <T> T fromJson(Request request, Class<T> myClass) {
        String body = request.body();
        Gson gson = new Gson();
        return gson.fromJson(body, myClass);
    }

}
