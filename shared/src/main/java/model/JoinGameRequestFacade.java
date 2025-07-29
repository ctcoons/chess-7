package model;

public record JoinGameRequestFacade(
        int id,
        String color,
        String authToken
) {
}
