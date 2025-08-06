package websocket.commands;

import model.ResignRequest;

public class ResignCommand extends UserGameCommand {

    ResignRequest resignRequest;

    public ResignCommand(ResignRequest resignRequest) {
        super(CommandType.RESIGN, resignRequest.authToken(), resignRequest.gameId());
        this.resignRequest = resignRequest;
    }

    public ResignRequest getResignRequest() {
        return resignRequest;
    }
}
