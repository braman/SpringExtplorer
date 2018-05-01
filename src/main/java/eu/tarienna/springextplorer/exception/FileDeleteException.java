package eu.tarienna.springextplorer.exception;

/**
 * Exception class which represents error for deletion failure
 */
public class FileDeleteException extends FileStoreException {

    public FileDeleteException(String fileName) {
        super("Failed to delete file " + fileName);
    }

}
