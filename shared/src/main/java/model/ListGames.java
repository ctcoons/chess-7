package model;

import chess.ChessGame;

import java.util.Collection;

public record ListGames(
        String allGames,
        Collection<GameData> chessGameCollection) {
}
