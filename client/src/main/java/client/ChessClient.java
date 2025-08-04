package client;

import chess.ChessGame;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import exception.ResponseException;
import model.AuthData;
import model.CreateGameResponse;
import model.GameData;
import server.ServerFacade;

import java.util.*;

public class ChessClient {
    private final ServerFacade server;
    private final NotificationHandler notificationHandler;
    private final String serverUrl;
    private WebSocketFacade ws;
    public State state = State.LOGGEDOUT;
    public boolean inGame = false;
    private String authToken;
    public String userName;
    public ChessGame.TeamColor color;
    public int gameId;
    public GameData gaMe;
    public boolean observer = false;
    private Integer mapIndex = 1;
    private Map<Integer, Integer> idMap = new HashMap<>();

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        this.server = new ServerFacade(serverUrl);
        this.authToken = null;
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
        try {
            this.ws = new WebSocketFacade(serverUrl, notificationHandler);
        } catch (Exception ignore) {
            System.out.print("Failed To Connect To WS");
        }
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            if (state == State.LOGGEDIN) {
                if (inGame) {
                    return inGameCommands(cmd, params);
                }
                return signedInCommands(cmd, params);
            } else {
                return signedOutCommands(cmd, params);
            }

        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private String signedOutCommands(String cmd, String[] params) throws ResponseException {
        return switch (cmd) {
            case "register" -> register(params);
            case "login" -> login(params);
            case "quit" -> quit();
            case "clear" -> clear(params);
            default -> help();
        };
    }

    private String signedInCommands(String cmd, String[] params) throws ResponseException {
        return switch (cmd) {
            case "logout" -> logout();
            case "quit" -> quit();
            case "create" -> create(params);
            case "list" -> listGames(params);
            case "join" -> join(params);
            case "observe" -> observe(params);
            default -> help();
        };
    }

    private String inGameCommands(String cmd, String[] params) {
        return switch (cmd) {
            case "quit" -> quit();
            default -> help();
        };
    }

    public String quit() {
        if (state == State.LOGGEDOUT) {
            return "quit";
        } else if (inGame) {
            inGame = false;
            observer = false;
            gaMe = null;
            color = null;
            return "quit";
        } else {
            state = State.LOGGEDOUT;
            return "Signed Out";
        }
    }

    public String register(String... params) throws ResponseException {
        if (!(params.length == 3)) {
            throw new ResponseException(400, "Must Register with <USERNAME> <PASSWORD> <EMAIL>");
        }

        AuthData authData;

        try {
            authData = server.register(params[0], params[1], params[2]);
        } catch (Exception e) {
            throw new ResponseException(400, "Username Already Taken");
        }

        authToken = authData.authToken();
        state = State.LOGGEDIN;
        userName = params[0];
        updateGames(authToken);
        return "Registered Successfully. Welcome " + params[0];
    }

    public String login(String... params) throws ResponseException {
        if (!(params.length == 2)) {
            throw new ResponseException(400, "Must log in with <USERNAME> <PASSWORD>");
        }
        String username = params[0];
        String password = params[1];

        try {
            var authData = server.login(username, password);

            if (authData == null) {
                throw new ResponseException(400, "Incorrect Credentials");
            }

            state = State.LOGGEDIN;
            authToken = authData.authToken();
            userName = params[0];
            updateGames(authToken);
            return "Successfully logged in. Welcome " + username;
        } catch (Exception e) {
            throw new ResponseException(401, "Incorrect Username Or Password");
        }

    }

    public String logout() throws ResponseException {
        assertSignedIn();
        server.logout(authToken);
        authToken = null;
        userName = null;
        color = null;
        state = State.LOGGEDOUT;
        return "Logged Out Successfully";
    }

    private String clear(String... params) throws ResponseException {
        if (params.length != 1) {
            return "";
        } else {
            if (Objects.equals(server.clearApplication(params[0]), "")) {
                idMap.clear();
                mapIndex = 1;
                return " ";
            } else {
                return "";
            }
        }
    }

    public String create(String[] params) throws ResponseException {
        assertSignedIn();
        if (!(params.length == 1)) {
            throw new ResponseException(400, "Must create game with exactly 1 argument: <GAME_NAME>");
        }

        String gameName = params[0];
        CreateGameResponse response;
        try {
            response = server.createNewGame(gameName, authToken);
        } catch (Exception e) {
            return "Game with name '" + gameName + "' already exists. Use another game name.";
        }
        if (response != null) {
            int id = response.gameID();
            idMap.put(mapIndex, id);
            mapIndex++;
            return "Created Game '" + gameName + "' successfully.";
        } else {
            throw new ResponseException(400, "Failed to create game");
        }
    }

    private void updateGames(String authToken) throws ResponseException {
        HashMap<Integer, Integer> result = new HashMap<>();

        try {
            mapIndex = 1;
            Collection<GameData> gameData = server.listGames(authToken);
            for (GameData game : gameData) {
                result.put(mapIndex, game.gameID());
                mapIndex++;
            }
        } catch (Exception e) {
            throw new ResponseException(400, e.getMessage());
        }

        idMap = result;
    }

    public String listGames(String[] params) throws ResponseException {
        if (!(params.length == 0)) {
            throw new ResponseException(400, "No Arguments Needed For List Games");
        }

        assertSignedIn();

        try {
            updateGames(authToken);
        } catch (Exception e) {
            throw new ResponseException(400, "Failed to update Games ID List While Listing Games");
        }

        StringBuilder result = new StringBuilder("Current Games:\n");

        for (Map.Entry<Integer, Integer> entry : idMap.entrySet()) {
            int id = entry.getValue();
            GameData game = server.getGame(id, authToken);

            String name = game.gameName();
            String whiteUser = game.whiteUsername();
            if (whiteUser == null) {
                whiteUser = "empty";
            }
            String blackUser = game.blackUsername();
            if (blackUser == null) {
                blackUser = "empty";
            }
            result.append(entry.getKey()).append("): '").append(name);
            result.append("'\t").append("WHITE: ").append(whiteUser);
            result.append("\t").append("BLACK: ").append(blackUser).append("\n");
        }

        return result.toString();
    }

    public String join(String[] params) throws ResponseException {
        if (params.length != 2) {
            throw new ResponseException(400, "Wrong Input. Format to join a game: join <ID> [BLACK|WHITE]");
        }

        int index;
        try {
            index = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "Use the game ID to join a game");
        }

        Integer id;
        try {
            id = idMap.get(index);
            if (id == null) {
                throw new Exception("bad id");
            }
        } catch (Exception e) {
            throw new ResponseException(400, "Game By This ID doesn't Exist");
        }


        String color = params[1].toUpperCase();

        if (color.equals("BLACK") || color.equals("WHITE")) {
            try {
                server.joinGame(id, color, authToken);
            } catch (Exception e) {
                throw new ResponseException(400, "Must use a valid game ID and pick a color that is available");
            }

            try {
                gaMe = server.getGame(id, authToken);
            } catch (Exception e) {
                throw new ResponseException(400, "Failed to get game");
            }

            inGame = true;
            observer = false;

            this.color = color.equals("BLACK") ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

            gameId = id;

            ws.joinGame(authToken, gameId, color);

            return "Joining Game " + index + "...\n";
        } else {
            throw new ResponseException(400, "Format to join a game: join <ID> [BLACK|WHITE]");
        }

    }

    public String observe(String[] params) throws ResponseException {
        assertSignedIn();
        if (params.length != 1) {
            throw new ResponseException(400, "Wrong Input. Format to observe a game: observe <ID>");
        }

        int index;
        try {
            index = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "Use the game ID to observe a game");
        }

        Integer id;
        try {
            id = idMap.get(index);
            if (id == null) {
                throw new Exception("bad id");
            }
        } catch (Exception e) {
            throw new ResponseException(400, "Game By This ID doesn't Exist");
        }

        try {
            gaMe = server.getGame(id, authToken);
        } catch (Exception e) {
            throw new ResponseException(400, "Failed to get game");
        }

        if (gaMe == null) {
            throw new ResponseException(400, "No Game Found With This ID");
        }

        inGame = true;

        color = ChessGame.TeamColor.WHITE;

        observer = true;

        gameId = id;

        ws.joinGame(authToken, gameId, "observer");

        return "Observing Game " + index + "...\n";


    }

    public String help() {
        if (state == State.LOGGEDOUT) {
            return """
                    - "register" <USERNAME> <PASSWORD> <EMAIL>
                    - "login" <USERNAME> <PASSWORD>
                    - "help"
                    - "quit"
                    """;
        }

        if (inGame) {
            return """
                    - "quit"
                    - *** IN-GAME HELP COMMANDS (NOT IMPLEMENTED YET) ***
                    """;
        }

        return """
                - "create" <NAME>
                - "list"
                - "join" <ID> [WHITE|BLACK]
                - "observe" <ID>
                - "logout"
                - "help"
                """;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.LOGGEDOUT) {
            throw new ResponseException(400, "You must sign in");
        }

    }
}


