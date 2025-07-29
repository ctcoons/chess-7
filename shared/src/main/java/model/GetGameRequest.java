package model;

public record GetGameRequest(
        int id,
        String authToken
) {
}
