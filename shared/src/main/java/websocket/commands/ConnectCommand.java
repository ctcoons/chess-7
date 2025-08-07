package websocket.commands;

import model.GameData;

public class ConnectCommand extends UserGameCommand {

    private final String whoIsConnecting;
    private final GameData gameData;
    private final ClientType clientType;

    public ConnectCommand(String authToken, Integer gameId, String whoIsConnecting, GameData gameData, ClientType clientType) {
        super(CommandType.CONNECT, authToken, gameId);
        this.whoIsConnecting = whoIsConnecting;
        this.gameData = gameData;
        this.clientType = clientType;
    }

    public String getWhoIsConnecting() {
        return whoIsConnecting;
    }

    public GameData getGameData() {
        return gameData;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public enum ClientType {
        OBSERVER,
        WHITE,
        BLACK
    }
}
