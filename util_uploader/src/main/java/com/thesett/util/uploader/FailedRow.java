package com.thesett.util.uploader;

import java.util.Map;

/**
 * FailedRow represents a row of CSV data that failed to upload. Such failures are held in a FailedRow along with a
 * relevant error message.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Holds a failed row along with an error message. </td></tr>
 * </table></pre>
 */
public class FailedRow {
    /** The parsed data row that failed. */
    private final Map<String, Object> data;

    /** The error message. */
    private final String message;

    /**
     * Records a failure against a row of data.
     *
     * @param data    The data row that failed.
     * @param message An error message.
     */
    public FailedRow(Map<String, Object> data, String message) {
        this.data = data;
        this.message = message;
    }

    /**
     * Provides the parsed data row that failed.
     *
     * @return The parsed data row that failed.
     */
    public Map getData() {
        return data;
    }

    /**
     * Provides the error message.
     *
     * @return The error message.
     */
    public String getMessage() {
        return message;
    }
}
