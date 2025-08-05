package model;

import chess.ChessGame;
import chess.ChessMove;

public record MakeMoveResponse(
        Boolean success,
        String responseMessage,
        GameData gameData
) {
}
