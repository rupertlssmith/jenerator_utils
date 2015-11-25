package com.thesett.util.uploader.csv;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.thesett.util.uploader.UploadException;

/**
 * CSVHandlerUtils provides some helper methods for working with CSV data.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Read CSV data into a table. </td></tr>
 * <tr><td> Write CSV data into a table. </td></tr>
 * </table></pre>
 */
public class CSVHandlerUtils {
    public static final String ERROR_WHILST_WRITING_CSV_DATA_MESSAGE = "Error whilst writing CSV data.";
    public static final String ERROR_WHILST_LOADING_CSV_DATA_MESSAGE = "Error whilst loading CSV data.";

    /** Private constructor to prevent instantiation. */
    private CSVHandlerUtils() {
    }

    /**
     * Reads CSV data from a Reader into a list of rows (table).
     *
     * @param  reader The Reader to read the CSV data from.
     * @param  schema A CSV schema to define how the data is represented in the file.
     *
     * @return A list of rows of data as maps.
     *
     * @throws com.thesett.util.uploader.UploadException Iff there is an error reading the data.
     */
    public static List<Map<String, Object>> extractCSVFromReader(Reader reader, CsvSchema schema)
        throws UploadException {
        try {
            CsvMapper csvMapper = new CsvMapper();

            MappingIterator<Map<String, Object>> mappingIterator =
                csvMapper.reader(Map.class).with(schema).readValues(reader);

            return mappingIterator.readAll();
        } catch (IOException e) {
            throw new UploadException(ERROR_WHILST_LOADING_CSV_DATA_MESSAGE, e);
        }
    }

    /**
     * Outputs CSV data from a table to a Writer.
     *
     * @param  writer The Writer to output to.
     * @param  table  The data table to write.
     * @param  schema A CSV schema for the data table.
     *
     * @throws UploadException Iff there is an error writing the data.
     */
    public static void outputCSVToWriter(Writer writer, List<Map<String, Object>> table, CsvSchema schema)
        throws UploadException {
        CsvMapper csvMapper = new CsvMapper();

        // This is done to prevent the mapper being auto-closed on writeValue.
        csvMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

        try {
            for (Map<String, Object> row : table) {
                csvMapper.writer(schema).writeValue(writer, row);
            }
        } catch (JsonProcessingException e) {
            throw new UploadException(ERROR_WHILST_WRITING_CSV_DATA_MESSAGE, e);
        } catch (IOException e) {
            throw new UploadException(ERROR_WHILST_WRITING_CSV_DATA_MESSAGE, e);
        }
    }

    /**
     * Outputs CSV data from a table to a Writer. The rows of CSV data are preceded by error messages, so that the
     * resulting output is CSV data interleaved with error messages.
     *
     * @param  writer   The Writer to output to.
     * @param  table    The data table to write.
     * @param  messages A list of error messages.
     * @param  schema   A CSV schema for the data table.
     *
     * @throws UploadException Iff there is an error writing the data.
     */
    public static void outputCSVWithErrorsToWriter(Writer writer, List<Map<String, Object>> table,
        List<String> messages, CsvSchema schema) throws UploadException {
        CsvMapper csvMapper = new CsvMapper();

        // This is done to prevent the mapper being auto-closed on writeValue.
        csvMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

        try {
            Iterator<String> messageIterator = messages.iterator();

            for (Map<String, Object> row : table) {
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    entry.setValue(entry.getValue() == null ? null : entry.getValue().toString());
                }

                writer.write("\"");
                writer.write(messageIterator.next());
                writer.write("\"\n");
                csvMapper.writer(schema).writeValue(writer, row);
            }
        } catch (JsonProcessingException e) {
            throw new UploadException(ERROR_WHILST_WRITING_CSV_DATA_MESSAGE, e);
        } catch (IOException e) {
            throw new UploadException(ERROR_WHILST_WRITING_CSV_DATA_MESSAGE, e);
        }
    }
}
