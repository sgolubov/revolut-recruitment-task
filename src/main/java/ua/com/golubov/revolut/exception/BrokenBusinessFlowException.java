package ua.com.golubov.revolut.exception;

public class BrokenBusinessFlowException extends RuntimeException {

    public BrokenBusinessFlowException() {
        super("This error indicates that business flow is broken.");
    }
}
