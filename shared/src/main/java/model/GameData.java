package model;

import chess.ChessGame;

public record GameData(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        Winner winner,
        ChessGame game) {

    public enum Winner {
        WHITE,
        BLACK,
        DRAW
    }

}


