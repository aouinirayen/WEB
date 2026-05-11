package tn.esprit.pidev.exception;

/**
 * Exception thrown when file upload fails
 */
public class FileUploadException extends RuntimeException {
    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}

