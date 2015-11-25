package com.thesett.util.uploader.file;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.thesett.util.queue.FifoLinkedQueue;
import com.thesett.util.queue.MappedSource;
import com.thesett.util.queue.Queue;
import com.thesett.util.queue.Source;
import com.thesett.util.uploader.NamedReader;

/**
 * FilePatternETLSource provides a mechanism to scan a directory for files matching a pattern, and to initiate an ETL
 * process on each of those files.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Scan a directory for matching files. </td></tr>
 * </table></pre>
 */
public class FilePatternSource implements Source<NamedReader> {
    /** The input directory to scan for files. */
    protected final String inputDir;

    /** The format, including wildcards, to match against file names with. */
    protected final String fileNamePattern;

    /** The list of matched files. */
    protected Queue<File> fileSource = new FifoLinkedQueue<>();

    protected Source<NamedReader> readerSource;

    /**
     * Creates the file pattern loader.
     *
     * @param inputDir        The path to the input directory to read from.
     * @param fileNamePattern A pattern to match files in the input directory as files to load.
     */
    public FilePatternSource(String inputDir, String fileNamePattern) {
        this.inputDir = inputDir;
        this.fileNamePattern = fileNamePattern;

        initializeFileList();

        readerSource = new MappedSource<File, NamedReader>(new FileToNamedReader(), fileSource);
    }

    /** {@inheritDoc} */
    public NamedReader poll() {
        return readerSource.poll();
    }

    /** {@inheritDoc} */
    public NamedReader peek() {
        return readerSource.peek();
    }

    /** Tries to obtain a list of files from the input directory, if no files are found the list will be empty. */
    protected void initializeFileList() {
        File dir = new File(inputDir);
        FileFilter typeFilter = new WildcardFileFilter(fileNamePattern);

        if (dir.exists()) {
            File[] files = dir.listFiles(typeFilter);

            for (File file : files) {
                fileSource.offer(file);
            }
        }
    }
}
