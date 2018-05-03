package eu.tarienna.springextplorer.conf;

import java.io.File;
import java.util.Comparator;

/**
 * Compares files and priorities folders as first items
 *
 */
public class FileComparator implements Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
        if (o1 != null && o1.isDirectory() ) {
            return -1;
        }
        
        if (o2 != null && o2.isDirectory()) {
            return 1;
        }
        
        return 0;
    }
}