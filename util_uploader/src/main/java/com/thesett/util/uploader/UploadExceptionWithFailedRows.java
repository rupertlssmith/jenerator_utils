package com.thesett.util.uploader;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * UploadExceptionWithFailedRows is an {@link UploadException} that contains a list of raw CSV data rows that failed to
 * upload, along with error messages about why they failed.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> List failed rows of CSV data with error messages. </td></tr>
 * </table></pre>
 */
public class UploadExceptionWithFailedRows extends UploadException {
    /** A name identifying the source of the failed row. */
    private final String sourceName;

    /** Holds the list of failed rows. */
    private final List<FailedRow> failedRows;

    /**
     * Creates an upload exception with a list of failed rows.
     *
     * @param failedRows The list of failed rows.
     */
    public UploadExceptionWithFailedRows(String sourceName, List<FailedRow> failedRows) {
        super("Upload error with failed rows");
        this.sourceName = sourceName;
        this.failedRows = failedRows;
    }

    /**
     * Provides the list of failed rows.
     *
     * @return The list of failed rows.
     */
    public List<FailedRow> getFailedRows() {
        return failedRows;
    }

    /**
     * Provides the list of failed rows as an {@link ETLTable}.
     *
     * @return The list of failed rows as a table.
     */
    public ETLTableWithErrors getFailedRowsAsTable() {
        List<Map<String, Object>> rows = new LinkedList<Map<String, Object>>();
        List<String> messages = new LinkedList<>();

        for (FailedRow row : failedRows) {
            rows.add(row.getData());
            messages.add(row.getMessage());
        }

        return new ETLTableWithErrors(sourceName, rows, messages);
    }
}
