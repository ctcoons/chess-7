package server;

import com.google.gson.Gson;

// DATA access Classes
import dataaccess.*;

// Model Classes
import model.*;


import server.websocket.WebSocketHandler;
import service.*;
import spark.*;

import java.util.Collection;
import java.util.Map;

public class Server {

    public UserDAO userDAO;
    public AuthDAO authDAO;
    public GameDAO gameDAO;
    public UserService userService;
    public AuthService authService;
    public GameService gameService;
    private final WebSocketHandler webSocketHandler;

    public Server() {
        this.userDAO = new SQLUserDAO();
        this.authDAO = new SQLAuthDAO();
        this.gameDAO = new SQLGameDAO();
        this.userService = new UserService();
        this.authService = new AuthService();
        this.gameService = new GameService();
        this.webSocketHandler = new WebSocketHandler(this);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.webSocket("/ws", webSocketHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.put("/game/:id/:username/resign", this::resign);
        Spark.put("/game/:id/:username", this::leaveGame);
        Spark.post("/game/:id/:username", this::makeMove);
        Spark.get("/game/:id", this::getGameById);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.get("/game", this::listGames);
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

    private Object resign(Request request, Response response) throws DataAccessException {
        ResignRequest resignRequest = fromJson(request, ResignRequest.class);
        int gameId;
        try {
            gameId = Integer.parseInt(request.params("id"));
        } catch (NumberFormatException e) {
            throw new DataAccessException("Invalid Input For ID");
        }
        gameService.resign(gameId, resignRequest.username(), gameDAO);
        return "{}";
    }

    private Object makeMove(Request request, Response response) throws DataAccessException {

        MakeMoveRequest makeMoveRequest = fromJson(request, MakeMoveRequest.class);
        int gameId;
        try {
            gameId = Integer.parseInt(request.params("id"));
        } catch (NumberFormatException e) {
            throw new DataAccessException("Invalid Input For ID");
        }
        MakeMoveResponse makeMoveResponse = gameService.makeMove(gameId, makeMoveRequest.chessMove(), gameDAO);
        return new Gson().toJson(makeMoveResponse);
    }

    private Object leaveGame(Request request, Response response) throws DataAccessException, NotAuthorizedException {

        AuthData authData = fromJson(request, AuthData.class);

        if (authService.validateAuth(authData.authToken(), authDAO)) {
            int gameId;
            try {
                gameId = Integer.parseInt(request.params("id"));
            } catch (NumberFormatException e) {
                throw new DataAccessException("Invalid Input For ID");
            }
            String username = request.params("username");
            gameService.leaveGame(gameId, username, gameDAO);
            return "{}";

        } else {
            throw new NotAuthorizedException("User Not Authorized");
        }
    }

    private Object getGameById(Request request, Response response) throws DataAccessException, NotAuthorizedException {

        String authToken = request.headers("Authorization");

        // √ Authenticate the User
        if (authService.validateAuth(authToken, authDAO)) {

            int gameID;
            try {
                gameID = Integer.parseInt(request.params("id"));
            } catch (NumberFormatException e) {
                throw new DataAccessException("Invalid Input For ID");
            }

            GameData gameData = gameService.getGameById(gameID, gameDAO);
            Gson gameDataGson = new Gson();
            return gameDataGson.toJson(gameData);

        } else {
            System.out.println("Join Game Exception Thrown");
            throw new NotAuthorizedException("User Not Authorized");
        }
    }

    private Object clearApplication(Request request, Response response) throws DataAccessException {

        System.out.println("RUNNING  authService.clearAuthData(authDAO)");
        authService.clearAuthData(authDAO);

        System.out.println("RUNNING  gameService.clearGameData(gameDAO)");
        gameService.clearGameData(gameDAO);

        System.out.println("RUNNING  userService.clearUserData(userDAO)");
        userService.clearUserData(userDAO);

        return "{}";
    }

    private Object joinGame(Request request, Response response) throws NotAuthorizedException, DataAccessException, InvalidColorException, ColorTakenException, BadRequestException {
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
            int iD = gameData.gameID();

            CreateGameResponse createGameResponse = new CreateGameResponse(iD);
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
            System.out.println("ListGames Not Authorized Thrown");
            throw new NotAuthorizedException("Not Authorized");
        }
    }

    // *** LOGOUT HANDLER ***
    private Object logout(Request request, Response response) throws LogoutFailureException, DataAccessException {

        String authToken = request.headers("Authorization");

        if (authService.getUsernameByAuthToken(authToken, authDAO) == null) {
            throw new LogoutFailureException("Failed to logout because wasn't logged in");
        }

        authService.deleteAuth(authToken, authDAO);

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

        if (userService.login(loginRequest, userDAO)) {
            System.out.println("Validating user " + loginRequest.username() + " with password: " + loginRequest.password());
            String newAuthToken = authService.addAuth(loginRequest.username(), authDAO);

            AuthData authData = new AuthData(authService.getUsernameByAuthToken(newAuthToken, authDAO), newAuthToken);
            Gson authGson = new Gson();

            return authGson.toJson(authData);
        } else {
            System.out.println("Incorrect credentials thrown from login");
            throw new IncorrectCredentialsException("Incorrect User Name and/or Password");
        }

    }

    // *** REGISTER HANDLER ***
    private Object register(Request request, Response response) throws DataAccessException, RegisterException, BadRequestException {
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
            case BadRequestException ignored -> 400;
            case RegisterException ignored -> 403;
            case LogoutFailureException ignored -> 401;
            case NotAuthorizedException ignored -> 401;
            case NullFieldsException ignored -> 400;
            case ColorTakenException ignored -> 403;
            case InvalidColorException ignored -> 400;
            case DataAccessException ignored -> 500;


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
