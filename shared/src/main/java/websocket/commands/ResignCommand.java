package websocket.commands;

import model.ResignRequest;

public class ResignCommand extends UserGameCommand {

    public ResignCommand(String authToken, int gameID) {
        super(CommandType.RESIGN, authToken, gameID);
    }

}
