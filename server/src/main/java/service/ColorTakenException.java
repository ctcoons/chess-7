package service;

/**
 * Indicates there was an error connecting to the database
 */
public class ColorTakenException extends Exception{
    public ColorTakenException(String message) {
        super(message);
    }
    public ColorTakenException(String message, Throwable ex) {
        super(message, ex);
    }
}
