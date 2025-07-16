package service;

/**
 * Indicates there was an error connecting to the database
 */
public class InvalidColorException extends Exception{
    public InvalidColorException(String message) {
        super(message);
    }
    public InvalidColorException(String message, Throwable ex) {
        super(message, ex);
    }
}
