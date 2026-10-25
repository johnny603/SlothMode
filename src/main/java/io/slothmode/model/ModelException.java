package io.slothmode.model;

/**
 * Exception thrown when a local model call fails.
 */
public class ModelException extends RuntimeException {
    public ModelException(String message) {
        super(message);
    }

    public ModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
