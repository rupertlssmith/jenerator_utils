package com.thesett.util.uploader;

import java.util.Map;

/**
 * RowETLProcessor describes an ETL processor that handles a single row of data.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Process a single row of data, reporting any errors. </td></tr>
 * </table></pre>
 */
public interface RowETLProcessor {
    /**
     * Processes one row of data.
     *
     * @param  data  The row of data to process.
     * @param  jobId The unique upload job id.
     *
     * @throws UploadException If the row cannot be completely processed.
     */
    void processRow(Map<String, Object> data, String jobId) throws UploadException;
}
