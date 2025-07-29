package client;

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
    private boolean INGAME = false;
    private String authToken;

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
            return "Successfully logged in. Welcome " + username;
        } catch (Exception e) {
            throw new ResponseException(401, "Incorrect Username Or Password");
        }

    }

    public String logout() throws ResponseException {
        assertSignedIn();
        System.out.println(authToken);
        server.logout(authToken);
        authToken = null;
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
        CreateGameResponse response = server.createNewGame(gameName, authToken);
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
            result.append("'\t").append("White: ").append(whiteUser);
            result.append("\t").append("Black: ").append(blackUser).append("\n");
        }

        return result.toString();
    }

    public String join(String[] params) throws ResponseException {
        if (params.length != 2) {
            throw new ResponseException(400, "Format to join a game: Join <ID> [BLACK|WHITE]");
        }

        if (!params[1].equals("BLACK") && !params[1].equals("WHITE")) {
            throw new ResponseException(400, "Format to join a game: Join <ID> [BLACK|WHITE]");
        }

//        TODO: FINISH WRITING JOIN
        return null;
    }

    public String observe(String[] params) throws ResponseException {
        return " ";
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


