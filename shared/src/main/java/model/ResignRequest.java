package model;

import chess.ChessGame;

public record ResignRequest(
        boolean desireResignation,
        int gameId,
        String username,
        String authToken,
        ChessGame.TeamColor teamColor
) {
}
