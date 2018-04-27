package kz.ramanqul.upwork.mark_joachim.SpringExtplorer.exception;

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
