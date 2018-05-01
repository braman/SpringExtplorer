package eu.tarienna.springextplorer.exception;

/**
 * Exception class which represents error for unpacking failure
 */
public class FileUnzipException extends FileStoreException {

    public FileUnzipException(String fileName) {
        super("Failed to unzip file " + fileName);
    }
    
    public FileUnzipException(Throwable t) {
        super(t);
    }

}
