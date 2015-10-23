package com.thesett.util.uploader;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import com.thesett.util.console.ConsoleUtils;
import com.thesett.util.function.Function;
import com.thesett.util.queue.Sink;

/**
 * StdoutETLTableSinkByType consumes tables of data, and prints them to standard out, given a function to select
 * appropriate table writer by source name.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Print multiple types of table to standard out. </td></tr>
 * </table></pre>
 */
public class StdoutETLTableSinkByType implements Sink<ETLTable> {
    /** Holds the source name to table writer mapping. */
    private final Function<String, TableWriter> nameToTableWriter;

    /**
     * Creates a standard out table writer, given a function to map table sources to table writers.
     *
     * @param nameToTableWriter The function mapping table sources to table writers.
     */
    public StdoutETLTableSinkByType(Function<String, TableWriter> nameToTableWriter) {
        this.nameToTableWriter = nameToTableWriter;
    }

    /** {@inheritDoc} */
    public boolean offer(ETLTable table) {
        try {
            Writer stdoutWriter = new OutputStreamWriter(ConsoleUtils.stdout(), Charset.forName("UTF-8"));

            if (table instanceof ETLTableWithErrors) {
                ETLTableWithErrors tableWithErrors = (ETLTableWithErrors) table;
                nameToTableWriter.apply(table.getName())
                    .writeTableWithErrors(stdoutWriter, tableWithErrors.getData(), tableWithErrors.getMessages());
            } else {
                nameToTableWriter.apply(table.getName()).writeTable(stdoutWriter, table.getData());
            }
        } catch (UploadException e) {
            throw new IllegalStateException(e);
        }

        return true;
    }
}
