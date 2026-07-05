package za.co.monate.retail.exception;

public class BulkImportException extends RuntimeException {
    public BulkImportException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BulkImportException(String message) {
        super(message);
    }
}