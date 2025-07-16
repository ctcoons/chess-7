package service;

/**
 * Indicates there was an error connecting to the database
 */
public class GameAlreadyExistsException extends Exception{
    public GameAlreadyExistsException(String message) {
        super(message);
    }
    public GameAlreadyExistsException(String message, Throwable ex) {
        super(message, ex);
    }
}
