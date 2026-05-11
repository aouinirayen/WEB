package tn.esprit.pidev.exception;

/**
 * Exception thrown when a document type is not found
 */
public class DocumentTypeNotFoundException extends RuntimeException {
    public DocumentTypeNotFoundException(Long documentTypeId) {
        super("Document type with ID " + documentTypeId + " not found");
    }

    public DocumentTypeNotFoundException(String message) {
        super(message);
    }
}

