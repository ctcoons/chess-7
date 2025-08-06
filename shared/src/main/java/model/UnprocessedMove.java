package model;

import chess.ChessPosition;

public class UnprocessedMove {
    public ChessPosition start;
    public ChessPosition end;

    public UnprocessedMove(ChessPosition start, ChessPosition end) {
        this.start = start;
        this.end = end;
    }

}
