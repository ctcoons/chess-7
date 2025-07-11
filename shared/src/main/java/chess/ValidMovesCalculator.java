package chess;

import java.util.ArrayList;
import java.util.Collection;

public class ValidMovesCalculator {

    ChessBoard myBoard;
    ChessPosition myPosition;
    ChessPiece myPiece;
    ChessGame.TeamColor myColor;

    public ValidMovesCalculator(ChessBoard board, ChessPosition position) {

        this.myBoard = board;
        this.myPosition = position;
        this.myPiece = myBoard.getPiece(myPosition);
        this.myColor = myPiece.getTeamColor();

    }

    public Collection<ChessMove> getValidMoves() {

        switch(myPiece.type) {
            case KING -> {
                return kingMoves();
            }
            case BISHOP -> {
                return bishopMoves();
            }
            case ROOK -> {
                return rookMoves();
            }
            case QUEEN -> {
                return queenMoves();
            }
            case KNIGHT -> {
                return knightMoves();
            }
            case PAWN -> {
                return pawnMoves();
            }
            default -> {
                return new ArrayList<>();
            }
        }

    }

    public Collection<ChessMove> kingMoves() {
        Collection<ChessMove> valid_Moves = new ArrayList<ChessMove>();

        for (int rowDif : new int[] {-1, 0, 1}) {
            for (int colDif : new int[] {-1, 0, 1}) {

                int newRow = myPosition.getRow() + rowDif;
                int newCol = myPosition.getColumn() + colDif;


                if (differentColor(newRow, newCol)) {
                    ChessPosition validDestination = new ChessPosition(newRow, newCol);
                    ChessMove validMove = new ChessMove(myPosition, validDestination, null);
                    valid_Moves.add(validMove);
                }
            }
        }

        return valid_Moves;
    }

    public Collection<ChessMove> bishopMoves() {
        return diagonalMoves();
    }

    public Collection<ChessMove> rookMoves() {
        return verticalAndHorizontalMoves();
    }

    public Collection<ChessMove> queenMoves() {
        Collection<ChessMove> valid_Moves = new ArrayList<ChessMove>();
        valid_Moves.addAll(diagonalMoves());
        valid_Moves.addAll(verticalAndHorizontalMoves());

        return valid_Moves;
    }

    public Collection<ChessMove> knightMoves() {
        Collection<ChessMove> valid_Moves = new ArrayList<ChessMove>();

        for (int[] arr: new int[][] {{-2, -1}, {-2,1}, {-1,-2}, {-1,2}, {1,2}, {1,-2}, {2,1}, {2,-1}}) {
            int newRow = arr[1] + myPosition.getRow();
            int newCol = arr[0] + myPosition.getColumn();

            if (differentColor(newRow, newCol)) {
                ChessPosition position = new ChessPosition(newRow, newCol);
                valid_Moves.add(new ChessMove(myPosition, position, null));
            }
        }

        return valid_Moves;
    }

    public Collection<ChessMove> pawnMoves() {
        Collection<ChessMove> valid_Moves = new ArrayList<ChessMove>();

        // See if the pawn is on the 2nd to last line
        boolean prom = calcProm();

        // Calculate direction based on color
        int colorDif = calcColorDif();

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        if (myBoard.getPiece(new ChessPosition(row + colorDif, col)) == null) {
            ChessPosition validDestination = new ChessPosition(row+colorDif, col);
            if (prom) {
                valid_Moves.addAll(calcPromMoves(validDestination));
            } else {

                valid_Moves.add(new ChessMove(myPosition, validDestination, null));

                if ((myColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7)||(myColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2)) {
                    if (myBoard.getPiece(new ChessPosition(row + 2*colorDif, col)) == null) {
                        ChessPosition newDest = new ChessPosition(row + 2 * colorDif, col);
                        valid_Moves.add(new ChessMove(myPosition, newDest, null));
                    }
                }
            }
        }

        for (int colChange : new int[] {-1, 1}) {
            int newCol = myPosition.getColumn() + colChange;
            int newRow = myPosition.getRow() + colorDif;
            if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                continue;
            } else {
                ChessPosition destination = new ChessPosition(newRow, newCol);
                if (myBoard.getPiece(destination) == null ) {
                    continue;
                } else {
                    if (myBoard.getPiece(destination).getTeamColor() != myColor) {
                        if (prom) {
                            valid_Moves.addAll(calcPromMoves(destination));
                        } else {
                            valid_Moves.add(new ChessMove(myPosition, destination, null));
                        }
                    }
                }
            }

        }




        return valid_Moves;
    }

    public Collection<ChessMove> diagonalMoves() {
        Collection<ChessMove> valid_Moves = new ArrayList<ChessMove>();
        for (int rowVec : new int[] {-1, 1}) {
            for (int colVec : new int[] {-1, 1}) {
                int magnitude = 1;
                while(true) {
                    int newRow = myPosition.getRow() + rowVec*magnitude;
                    int newCol = myPosition.getColumn() + colVec*magnitude;
                    magnitude++;

                    if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                        break;
                    }

                    ChessPosition position = new ChessPosition(newRow, newCol);
                    ChessPiece piece = myBoard.getPiece(position);

                    if (piece == null) {
                        valid_Moves.add(new ChessMove(myPosition, position, null));
                    } else if (piece.getTeamColor() != myColor) {
                        valid_Moves.add(new ChessMove(myPosition, position, null));
                        break;
                    } else if (piece.getTeamColor() == myColor) {
                        break;
                    }

                }

            }
        }

        return valid_Moves;
    }

    public Collection<ChessMove> verticalAndHorizontalMoves() {
        Collection<ChessMove> valid_Moves = new ArrayList<ChessMove>();

        // Up and Down
        for (int rowVec : new int[] {-1, 1}) {
            int magnitude = 1;
            int newCol = myPosition.getColumn();

            while(true) {
                int newRow = myPosition.getRow() + rowVec * magnitude;

                magnitude++;

                if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                    break;
                }

                ChessPosition position = new ChessPosition(newRow, newCol);
                ChessPiece piece = myBoard.getPiece(position);

                if (piece == null) {
                    valid_Moves.add(new ChessMove(myPosition, position, null));
                } else if (piece.getTeamColor() != myColor) {
                    valid_Moves.add(new ChessMove(myPosition, position, null));
                    break;
                } else if (piece.getTeamColor() == myColor) {
                    break;
                }
            }
        }

        // Horizontal
        for (int colVec : new int[] {-1, 1}) {
            int magnitude = 1;
            int newRow = myPosition.getRow();

            while(true) {
                int newCol = myPosition.getColumn() + colVec * magnitude;

                magnitude++;

                if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                    break;
                }

                ChessPosition position = new ChessPosition(newRow, newCol);
                ChessPiece piece = myBoard.getPiece(position);

                if (piece == null) {
                    valid_Moves.add(new ChessMove(myPosition, position, null));
                } else if (piece.getTeamColor() != myColor) {
                    valid_Moves.add(new ChessMove(myPosition, position, null));
                    break;
                } else if (piece.getTeamColor() == myColor) {
                    break;
                }
            }
        }

        return valid_Moves;
    }

    boolean differentColor(int row, int col) {

        // Check for Out of Range
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        }

        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = myBoard.getPiece(position);

        if (piece == null) {
            return true;
        }

        return piece.getTeamColor() != myColor;
    }

    boolean calcProm() {
        if(myColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2) {
            return true;
        } else return myColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7;
    }

    int calcColorDif() {
        if(myColor == ChessGame.TeamColor.BLACK) {
            return -1;
        } else return 1;
    }


    public Collection<ChessMove> calcPromMoves(ChessPosition validDestination) {
        Collection<ChessMove> valid_Moves = new ArrayList<ChessMove>();

        valid_Moves.add(new ChessMove(myPosition, validDestination, ChessPiece.PieceType.QUEEN));
        valid_Moves.add(new ChessMove(myPosition, validDestination, ChessPiece.PieceType.ROOK));
        valid_Moves.add(new ChessMove(myPosition, validDestination, ChessPiece.PieceType.BISHOP));
        valid_Moves.add(new ChessMove(myPosition, validDestination, ChessPiece.PieceType.KNIGHT));

        return valid_Moves;
    }

}

