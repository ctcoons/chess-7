package server;

/**
 * Indicates there was an error connecting to the database
 */
public class IncorrectCredentialsException extends Exception{
    public IncorrectCredentialsException(String message) {
        super(message);
    }
    public IncorrectCredentialsException(String message, Throwable ex) {
        super(message, ex);
    }
}
