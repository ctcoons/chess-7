package websocket.commands;

import model.UnprocessedMove;

public class MakeMoveCommand extends UserGameCommand {

    public UnprocessedMove move;

    public MakeMoveCommand(String authToken, int gameId, UnprocessedMove move) {
        super(CommandType.MAKE_MOVE, authToken, gameId);
        this.move = move;
    }

}
