package io.trupeer.automation.core.exceptions;

/**
 * Base unchecked exception for all framework-level failures. Having a single root
 * exception lets the hooks/reporting layer catch and report failures uniformly.
 */
public class FrameworkException extends RuntimeException {

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
