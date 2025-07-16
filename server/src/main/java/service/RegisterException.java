package service;

/**
 * Indicates there was an error connecting to the database
 */
public class RegisterException  extends Exception{
    public RegisterException (String message) {
        super(message);
    }
    public RegisterException (String message, Throwable ex) {
        super(message, ex);
    }
}
