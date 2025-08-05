package websocket.commands;

public class LeaveGameCommand extends UserGameCommand {

    private final String whoIsConnecting;


    public LeaveGameCommand(String authToken, Integer gameID, String whoIsDisconnecting) {
        super(CommandType.LEAVE, authToken, gameID);
        this.whoIsConnecting = whoIsDisconnecting;

    }

    public String getWhoIsConnecting() {
        return this.whoIsConnecting;
    }

}
