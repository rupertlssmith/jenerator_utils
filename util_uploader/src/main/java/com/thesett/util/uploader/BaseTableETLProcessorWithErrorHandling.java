package com.thesett.util.uploader;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.thesett.util.progress.ProgressIndicator;

/**
 * BaseTableETLProcessorWithErrorHandling implements a loop over a list of rows of data from a data table. Each row is
 * processed by a processing function with can produce a {@link UploadException}s if processing on that row fails. The
 * processing loop will collect failed rows, but allow subsequent processing to continue on the remaining rows. At the
 * end of the loop, all of the errors are gathered up and reported in one go.
 *
 * <p/>A {@link ProgressIndicator} can also be attached to this table processor and will be updated as progress is made
 * through the table rows.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Split many rows of data into individual rows for processing. </td></tr>
 * <tr><td> Provide progress updates on the rows being processed. </td></tr>
 * <tr><td> Capture and report all failed rows. </td></tr>
 * </table></pre>
 */
public abstract class BaseTableETLProcessorWithErrorHandling implements TableETLProcessor {
    /** An optional progress indicator, may be <tt>null</tt>. */
    protected final ProgressIndicator progressIndicator;

    /**
     * Creates the ETL table processor.
     *
     * @param progressIndicator An optional progress indicator, may be <tt>null</tt>.
     */
    public BaseTableETLProcessorWithErrorHandling(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    /** {@inheritDoc} */
    public void processTable(ETLTable table, String jobId) throws UploadException {
        RowETLProcessor rowProcessor = getRowProcessorForTable(table);

        if (progressIndicator != null) {
            progressIndicator.initWorkToDo(table.getName(), table.getData().size());
        }

        List<FailedRow> failedRows = new LinkedList<>();
        String sourceName = table.getName();
        int workDone = 0;

        for (Map<String, Object> data : table.getData()) {
            // Clone the row of data, so that the original is preserved and can be printed as a rejected row.
            Map<String, Object> original = new LinkedHashMap<String, Object>(data);

            try {
                rowProcessor.processRow(data, jobId);
            } catch (UploadException e) { // NOSONAR

                // The exception is not re-thrown because a compensating action is being taken, which is to add the
                // row to the list of failed rows of data. A "no sonar" tag has been used to indicate that a
                // compensating action is being taken deliberately.
                failedRows.add(new FailedRow(original, e.getMessage()));
            }

            if (progressIndicator != null) {
                workDone++;
                progressIndicator.onWorkDone(workDone);
            }
        }

        // Check if there were failed rows, and generate a failure for the file if so.
        if (!failedRows.isEmpty()) {
            throw new UploadExceptionWithFailedRows(sourceName, failedRows);
        }
    }

    /**
     * Implementations should provide an appropriate row processor for the specified table.
     *
     * @param  table The table to get an ETL row processor for.
     *
     * @return An ETL row processor for the table.
     */
    protected abstract RowETLProcessor getRowProcessorForTable(ETLTable table);
}
