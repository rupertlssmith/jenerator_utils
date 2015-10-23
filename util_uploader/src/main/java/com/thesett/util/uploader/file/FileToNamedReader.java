package com.thesett.util.uploader.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.thesett.util.uploader.NamedReader;
import com.thesett.util.function.Function;

/**
 * Maps a file to a named reader. The name of the reader is the file name without its ending.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Map a file to a named reader. </td></tr>
 * </table></pre>
 */
public class FileToNamedReader implements Function<File, NamedReader> {
    /** {@inheritDoc} */
    public NamedReader apply(File file) {
        try {
            String fileName = file.getName();
            String sourceName = fileName.substring(0, fileName.indexOf("."));

            return new NamedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")),
                sourceName);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
