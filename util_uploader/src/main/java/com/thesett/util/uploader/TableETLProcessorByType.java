package com.thesett.util.uploader;

import com.thesett.util.function.Function;
import com.thesett.util.progress.ProgressIndicator;

/**
 * TableETLProcessorByType is a table processor that uses a function to select an appropriate {@link RowETLProcessor}
 * for the table based on its source name.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Perform ETL over a multiple types of table. </td></tr>
 * </table></pre>
 */
public class TableETLProcessorByType extends BaseTableETLProcessorWithErrorHandling {
    /** Holds the source name to row processor mapping. */
    private final Function<String, RowETLProcessor> nameToRowProcessor;

    /**
     * Creates the table processor for the legacy data tables.
     *
     * @param progressIndicator  An optional progress indicator, may be <tt>null</tt>.
     * @param nameToRowProcessor A mapping from data source names to row processors for the appropriate type of data.
     */
    public TableETLProcessorByType(ProgressIndicator progressIndicator,
        Function<String, RowETLProcessor> nameToRowProcessor) {
        super(progressIndicator);

        this.nameToRowProcessor = nameToRowProcessor;
    }

    /** {@inheritDoc} */
    protected RowETLProcessor getRowProcessorForTable(ETLTable table) {
        return nameToRowProcessor.apply(table.getName());
    }
}
