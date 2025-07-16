package server;

/**
 * Indicates there was an error connecting to the database
 */
public class NotAuthorizedException extends Exception{
    public NotAuthorizedException(String message) {
        super(message);
    }
    public NotAuthorizedException(String message, Throwable ex) {
        super(message, ex);
    }
}
