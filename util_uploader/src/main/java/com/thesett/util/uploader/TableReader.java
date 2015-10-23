package com.thesett.util.uploader;

import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * TableReader describes a source of ETL Table data, that extracts table data from a Reader.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Extract a table of data from a Reader. </td></tr>
 * </table></pre>
 */
public interface TableReader {
    /**
     * Assembles a table of data from the specified Reader.
     *
     * @param  reader The Reader to extract table data from.
     *
     * @throws UploadException If the data table cannot be parsed correctly from the reader.
     */
    List<Map<String, Object>> readTable(Reader reader) throws UploadException;
}
