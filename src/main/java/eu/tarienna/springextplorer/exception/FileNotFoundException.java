package eu.tarienna.springextplorer.exception;

/**
 * Exception class which represents error if file doesn't exist
 */
public class FileNotFoundException extends FileStoreException {

    public FileNotFoundException(String fileName) {
        super("File not found " + fileName);
    }

}
