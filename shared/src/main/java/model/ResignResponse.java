package model;

public record ResignResponse(
        int gameId,
        String username,
        String authToken
) {
}
