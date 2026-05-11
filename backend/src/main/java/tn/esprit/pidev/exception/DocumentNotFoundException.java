package tn.esprit.pidev.exception;

/**
 * Exception thrown when a legal document is not found
 */
public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(Long documentId) {
        super("Legal document with ID " + documentId + " not found");
    }

    public DocumentNotFoundException(String message) {
        super(message);
    }
}

