package com.thesett.util.file;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * FileUtils provides some helper methods for working with files.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Delete many files in one go. </td></tr>
 * <tr><td> Move files to a directory. </td></tr>
 * </table></pre>
 */
public class FileUtils {
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(FileUtils.class.getName());

    /** Proviate constructor to prevent instantiation. */
    private FileUtils() {
    }

    /**
     * Delete a list of files.
     *
     * @param fileList A list of files to delete.
     */
    public static void deleteFiles(Iterable<File> fileList) {
        for (File file : fileList) {
            if (!file.delete()) {
                LOG.warning("Unable to delete: " + file.getName());
            }
        }
    }

    /**
     * Moves a file to a directory.
     *
     * @param file    The file to move.
     * @param dirPath The directory to move the file to.
     */
    public static void moveFileToDirectory(File file, String dirPath) {
        StringBuilder builder = new StringBuilder();
        builder.append(dirPath).append(File.separator).append(file.getName());

        if (!file.renameTo(new File(builder.toString()))) {
            LOG.warning("Unable to move: " + file.getName() + " to " + builder.toString());
        }
    }
}
