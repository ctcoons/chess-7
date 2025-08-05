package websocket.commands;


import chess.ChessGame;
import chess.ChessMove;
import model.GameData;
import model.MakeMoveResponse;

public class MakeMoveCommand extends UserGameCommand {
    ChessMove move;
    GameData gameData;

    public MakeMoveCommand(String authToken, MakeMoveResponse makeMoveResponse, ChessMove move) {
        super(CommandType.MAKE_MOVE, authToken, makeMoveResponse.gameData().gameID());
        this.move = move;
        this.gameData = makeMoveResponse.gameData();
    }

}
