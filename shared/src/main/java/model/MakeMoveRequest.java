package model;

import chess.ChessMove;

public record MakeMoveRequest(
        ChessMove chessMove,
        int gameId
) {
}
