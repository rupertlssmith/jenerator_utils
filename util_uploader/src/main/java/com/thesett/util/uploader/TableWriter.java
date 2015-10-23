package com.thesett.util.uploader;

import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * TableWriter describes a sink of ETL Table data, that outputs table data to a Writer.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Output a table of data from a Writer. </td></tr>
 * </table></pre>
 */
public interface TableWriter {
    /**
     * Outputs a table of data to the specified Writer.
     *
     * @param writer The Writer to output to.
     * @param table  The table of data to write.
     */
    void writeTable(Writer writer, List<Map<String, Object>> table) throws UploadException;

    /**
     * Outputs a table of data with error messages to the specified Writer.
     *
     * @param writer   The Writer to output to.
     * @param table    The table of data to write.
     * @param messages The list of error messages.
     */
    void writeTableWithErrors(Writer writer, List<Map<String, Object>> table, List<String> messages)
        throws UploadException;
}
