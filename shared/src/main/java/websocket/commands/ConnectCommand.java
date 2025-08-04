package websocket.commands;

public class ConnectCommand extends UserGameCommand {

    private final String whoIsConnecting;

    public ConnectCommand(String authToken, Integer gameId, String whoIsConnecting) {
        super(CommandType.CONNECT, authToken, gameId);
        this.whoIsConnecting = whoIsConnecting;
    }

    public String getWhoIsConnecting() {
        return whoIsConnecting;
    }

}
