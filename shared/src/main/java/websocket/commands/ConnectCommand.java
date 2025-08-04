package websocket.commands;

public class ConnectCommand extends UserGameCommand {

    private static String whoIsConnecting;

    public ConnectCommand(String authToken, Integer gameId, String whoIsConnecting) {
        super(CommandType.CONNECT, authToken, gameId);
        ConnectCommand.whoIsConnecting = whoIsConnecting;
    }

    public String getWhoIsConnecting() {
        return whoIsConnecting;
    }

}
