package client;

import chess.ChessGame;
import exception.ResponseException;
import model.AuthData;
import model.CreateGameResponse;
import model.GameData;
import server.ServerFacade;

import java.util.Arrays;
import java.util.Collection;

public class ChessClient {
    private final ServerFacade server;
    public State state = State.LOGGEDOUT;
    public boolean INGAME = false;
    private String authToken;
    public String USERNAME;
    public ChessGame.TeamColor COLOR;
    public int GAMEID;
    public GameData GAME;
    public boolean OBSERVER = false;

    public ChessClient(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
        this.authToken = null;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            if (state == State.LOGGEDIN) {
                if (INGAME) {
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
        } else if (INGAME) {
            INGAME = false;
            return "Game Quit";
        } else {
            state = State.LOGGEDOUT;
            return "Signed Out";
        }
    }

    public String register(String... params) throws ResponseException {
        if (!(params.length == 3)) {
            throw new ResponseException(400, "Must Register with <USERNAME> <PASSWORD> <EMAIL>");
        }

        AuthData authData = server.register(params[0], params[1], params[2]);
        authToken = authData.authToken();
        state = State.LOGGEDIN;
        USERNAME = params[0];
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
            USERNAME = params[0];
            return "Successfully logged in. Welcome " + username;
        } catch (Exception e) {
            throw new ResponseException(401, "Incorrect Username Or Password");
        }

    }

    public String logout() throws ResponseException {
        assertSignedIn();
        server.logout(authToken);
        authToken = null;
        USERNAME = null;
        COLOR = null;
        state = State.LOGGEDOUT;
        return "Logged Out Successfully";
    }

    private String clear(String... params) throws ResponseException {
        if (params.length != 1) {
            return "";
        } else {
            return server.clearApplication(params[0]);
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
            return "Created Game '" + gameName + "' successfully.";
        } else {
            throw new ResponseException(400, "Failed to create game");
        }
    }

    public String listGames(String[] params) throws ResponseException {
        if (!(params.length == 0)) {
            throw new ResponseException(400, "No Arguments Needed For List Games");
        }
        assertSignedIn();
        Collection<GameData> gameData = server.listGames(authToken);
        StringBuilder result = new StringBuilder("Current Games:\n");
        for (GameData game : gameData) {
            int id = game.gameID();
            String name = game.gameName();
            String whiteUser = game.whiteUsername();
            if (whiteUser == null) {
                whiteUser = "empty";
            }
            String blackUser = game.blackUsername();
            if (blackUser == null) {
                blackUser = "empty";
            }
            result.append(id).append("): '").append(name);
            result.append("'\t").append("WHITE: ").append(whiteUser);
            result.append("\t").append("BLACK: ").append(blackUser).append("\n");
        }

        return result.toString();
    }

    public String join(String[] params) throws ResponseException {
        if (params.length != 2) {
            throw new ResponseException(400, "Wrong Input. Format to join a game: join <ID> [BLACK|WHITE]");
        }

        int id;
        try {
            id = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "Use the game ID to join a game");
        }


        String color = params[1].toUpperCase();

        if (color.equals("BLACK") || color.equals("WHITE")) {
            try {
                server.joinGame(id, color, authToken);
            } catch (Exception e) {
                throw new ResponseException(400, "Must use a valid game ID and pick a color that is available");
            }

            try {
                GAME = server.getGame(id, authToken);
            } catch (Exception e) {
                throw new ResponseException(400, "Failed to get game");
            }

            INGAME = true;
            OBSERVER = false;

            COLOR = color.equals("BLACK") ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

            GAMEID = id;

            return "Joining Game " + id + "...\n";
        } else {
            throw new ResponseException(400, "Format to join a game: join <ID> [BLACK|WHITE]");
        }

    }

    public String observe(String[] params) throws ResponseException {
        assertSignedIn();
        if (params.length != 1) {
            throw new ResponseException(400, "Wrong Input. Format to observe a game: observe <ID>");
        }

        int id;
        try {
            id = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "Use the game ID to observe a game");
        }

        try {
            GAME = server.getGame(id, authToken);
        } catch (Exception e) {
            throw new ResponseException(400, "Failed to get game");
        }

        INGAME = true;

        COLOR = ChessGame.TeamColor.WHITE;

        OBSERVER = true;

        GAMEID = id;

        return "Observing Game " + id + "...\n";


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

        if (INGAME) {
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


