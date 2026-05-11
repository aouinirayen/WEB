package tn.esprit.pidev.exception;

/**
 * Exception thrown when document operation is invalid (wrong status, ownership, etc.)
 */
public class InvalidDocumentException extends RuntimeException {
    public InvalidDocumentException(String message) {
        super(message);
    }

    public InvalidDocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}

