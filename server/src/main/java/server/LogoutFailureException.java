package server;

/**
 * Indicates there was an error connecting to the database
 */
public class LogoutFailureException extends Exception{
    public LogoutFailureException(String message) {
        super(message);
    }
    public LogoutFailureException(String message, Throwable ex) {
        super(message, ex);
    }
}
