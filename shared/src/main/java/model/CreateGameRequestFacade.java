package model;

public record CreateGameRequestFacade(
        String gameName,
        String authToken
) {
}
