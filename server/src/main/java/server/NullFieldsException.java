package server;

/**
 * Indicates there was an error connecting to the database
 */
public class NullFieldsException extends Exception{
    public NullFieldsException(String message) {
        super(message);
    }
    public NullFieldsException(String message, Throwable ex) {
        super(message, ex);
    }
}
