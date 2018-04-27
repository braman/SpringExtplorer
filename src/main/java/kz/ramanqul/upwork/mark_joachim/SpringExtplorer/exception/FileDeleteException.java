package kz.ramanqul.upwork.mark_joachim.SpringExtplorer.exception;

/**
 * Exception class which represents error for deletion failure
 */
public class FileDeleteException extends FileStoreException {

    public FileDeleteException(String fileName) {
        super("Failed to delete file " + fileName);
    }

}
