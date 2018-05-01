package eu.tarienna.springextplorer.exception;

/**
 * Generic exception class which represents file system error 
 */
public class FileStoreException extends RuntimeException {
    
    public FileStoreException(String message) {
        super(message);
    }
    
    public FileStoreException(Throwable t) {
        super(t);
    }
    
}
