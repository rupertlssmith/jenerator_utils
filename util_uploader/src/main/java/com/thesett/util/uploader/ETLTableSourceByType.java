package com.thesett.util.uploader;

import java.util.List;
import java.util.Map;

import com.thesett.util.function.Function;
import com.thesett.util.queue.MappedSource;
import com.thesett.util.queue.Source;

/**
 * ETLTableSourceByType provides parsed data tables for multiple types of table, given a function to select appropriate
 * table reader by source name.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Perform data extraction over a multiple types of table. </td></tr>
 * </table></pre>
 */
public class ETLTableSourceByType implements Source<ETLTable> {
    /** A mapper over the table readers to read tables. */
    private final MappedSource<NamedReader, ETLTable> mappedSource;

    /** A function mapping from table types to appropriate table readers. */
    private final Function<String, TableReader> nameToTableReader;

    /**
     * Builds a legacy data table source, on a source of named readers of table data.
     *
     * @param readerSource      A source of named table readers.
     * @param nameToTableReader A function mapping from source names to appropriate table readers.
     */
    public ETLTableSourceByType(Source<NamedReader> readerSource, Function<String, TableReader> nameToTableReader) {
        this.nameToTableReader = nameToTableReader;

        mappedSource = new MappedSource<NamedReader, ETLTable>(new NamedReaderETLTableFunction(), readerSource);
    }

    /** {@inheritDoc} */
    public ETLTable poll() {
        return mappedSource.poll();
    }

    /** {@inheritDoc} */
    public ETLTable peek() {
        return mappedSource.peek();
    }

    /**
     * A function mapping a named reader onto a table. This is achieved by looking up the appropriate type of table
     * reader using the function mapping held in {@link ETLTableSourceByType#nameToTableReader}.
     */
    private class NamedReaderETLTableFunction implements Function<NamedReader, ETLTable> {
        public ETLTable apply(NamedReader namedReader) {
            try {
                List<Map<String, Object>> data =
                    nameToTableReader.apply(namedReader.getName()).readTable(namedReader.getReader());

                return new ETLTable(namedReader.getName(), data);
            } catch (UploadException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
