package websocket.commands;

import model.GameData;

public class ConnectCommand extends UserGameCommand {

    private final String whoIsConnecting;
    private final GameData gameData;

    public ConnectCommand(String authToken, Integer gameId, String whoIsConnecting, GameData gameData) {
        super(CommandType.CONNECT, authToken, gameId);
        this.whoIsConnecting = whoIsConnecting;
        this.gameData = gameData;
    }

    public String getWhoIsConnecting() {
        return whoIsConnecting;
    }

    public GameData getGameData() {
        return gameData;
    }
}
