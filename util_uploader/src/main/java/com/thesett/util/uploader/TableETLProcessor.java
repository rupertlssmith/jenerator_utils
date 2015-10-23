package com.thesett.util.uploader;

/**
 * TableETLProcessor defines an ETL procedure over a whole table of data.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Perform ETL over a table of data. </td></tr>
 * </table></pre>
 */
public interface TableETLProcessor {
    /**
     * Processes a table data, gathering up multiple errors that may occur, to be reported at the end.
     *
     * @param  table The table of data.
     * @param  jobId The unique upload job id.
     *
     * @throws UploadException If one or more row errors are encountered.
     */
    void processTable(ETLTable table, String jobId) throws UploadException;
}
