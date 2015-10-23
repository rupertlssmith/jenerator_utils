package com.thesett.util.uploader;

import java.io.Reader;

/**
 * NamedReader combined together a Reader and a name for that reader. This allows a file, for example, to be presented
 * as a Reader whilst retaining information about the name of the source file the reader refers to.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Pair a name with a Reader. </td></tr>
 * </table></pre>
 */
public class NamedReader {
    private final String name;
    private final Reader reader;

    public NamedReader(Reader reader, String name) {
        this.reader = reader;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Reader getReader() {
        return reader;
    }
}
