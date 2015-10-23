package com.thesett.util.uploader;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.thesett.util.queue.Sink;
import com.thesett.util.queue.Source;

/**
 * Uploader implements an end-to-end ETL process.
 *
 * <p/>Starting with a source of ETL data {@link Source}, it extracts all of the available {@link ETLTable}s. The tables
 * of data are fed to a {@link TableETLProcessor} which in turn passes their rows through a {@link RowETLProcessor}.
 *
 * <p/>Any rows of data that fail to make it through this process are captured and returned in an exception from the
 * {@link TableETLProcessor#processTable(ETLTable, String)} method. These failed rows are passed to an {@link Sink}
 * which takes responsibility for recording the errors.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Upload tables of data from a source. </td></tr>
 * <tr><td> Pass all failed rows to an error processor. </td></></tr>
 * </table></pre>
 */
public class Uploader {
    /** The source of tables of data to upload. */
    private final Source<ETLTable> source;

    /** An error processor to pass all failed rows to. */
    private final Sink<ETLTable> errorSink;

    /** The table processor implementing the ETL process. */
    private final TableETLProcessor processor;

    /**
     * Creates an uploader.
     *
     * @param source    The source of tables of data to upload.
     * @param errorSink An error processor to pass all failed rows to.
     * @param processor The table processor implementing the ETL process.
     */
    public Uploader(Source<ETLTable> source, Sink<ETLTable> errorSink, TableETLProcessor processor) {
        this.source = source;
        this.errorSink = errorSink;
        this.processor = processor;
    }

    /**
     * Implements the ETL procedure.
     *
     * @return A unique job id for the upload.
     */
    public String upload() {
        // Create a unique id for the job.
        UUID jobId = UUID.randomUUID();

        List<UploadException> uploadErrors = new LinkedList<>();

        ETLTable table = source.poll();

        while (table != null) {
            try {
                processor.processTable(table, jobId.toString());
            } catch (UploadException e) {
                // Compensating action taken, which is to add the exception to the collection of errors.
                // used because this is a deliberate compensating action.
                uploadErrors.add(e);
            }

            table = source.poll();
        }

        if (!uploadErrors.isEmpty()) {
            for (UploadException error : uploadErrors) {
                if (error instanceof UploadExceptionWithFailedRows) {
                    UploadExceptionWithFailedRows failedRows = (UploadExceptionWithFailedRows) error;

                    errorSink.offer(failedRows.getFailedRowsAsTable());
                }
            }
        }

        return jobId.toString();
    }
}
