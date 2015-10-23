package com.thesett.util.uploader.csv;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.thesett.util.json.JodaTimeModule;
import static com.thesett.util.string.StringUtils.nullOrEmpty;
import com.thesett.util.uploader.RowETLProcessor;
import com.thesett.util.uploader.UploadException;

/**
 * BaseRowProcessor provides a useful set of methods for working with row data as part of an ETL process which uploads
 * to the stars database.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Upsert entities into the stars database. </td></tr>
 * <tr><td> Turn data rows into entities. </td></tr>
 * <tr><td> Transform and filter on columns of data. </td></tr>
 * </table></pre>
 */
public abstract class BaseRowUploader implements RowETLProcessor {
    private static final String FALSE = "false";
    private static final String TRUE = "true";

    /** Defines formatting of the date column in the normal UK format. */
    protected static final DateTimeFormatter UK_DATE_FORMAT = DateTimeFormat.forPattern("dd/MM/yyyy");

    /**
     * Extracts data from a row of data rows as JSON, transforms that into an instance of the entity to upload.
     *
     * @param  data        The data row.
     * @param  entityClass The type of entity to upload.
     *
     * @throws UploadException If the data row cannot be instantiated as an entity instance.
     */
    protected <T> T createEntity(Map<String, Object> data, Class entityClass) throws UploadException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaTimeModule());

        String jsonData = null;

        try {
            jsonData = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } catch (IOException e) {
            throw new UploadException("Got IOException whilst serializing data row to JSON: " + e.getMessage() +
                "\nFor JSON: " + jsonData, e);
        }

        try {
            Object entity = mapper.readValue(jsonData, entityClass);

            return (T) entity;
        } catch (IOException e) {
            throw new UploadException("Got IOException whilst creating entity instance: " + e.getMessage() +
                "\nFor JSON: " + jsonData, e);
        }
    }

    /**
     * Replaces a date column in the data map, with a {@link LocalDate} object containing the date extracted from the
     * column. If the date string is null or empty, it is completely removed from the data map.
     *
     * @param data       The data map to modify.
     * @param columnName The name of the column holding the date.
     */
    protected void replaceDate(Map<String, Object> data, String columnName) throws UploadException {
        String dateString = ((String) data.get(columnName)).trim();

        if (nullOrEmpty(dateString)) {
            data.remove(columnName);
        } else {
            LocalDate parsedDate = null;

            try {
                parsedDate = LocalDate.parse(dateString, BaseRowUploader.UK_DATE_FORMAT);
            } catch (IllegalArgumentException e) {
                throw new UploadException("Date does not meet format '" + dateString + "'.", e);
            }

            data.put(columnName, parsedDate);
        }
    }

    /**
     * Replaces a boolean column in the data map, with strings that map to valid boolean values. This is used to clean
     * up different boolean representations in data into a standard representation.
     *
     * <p/>If the string value in the column contains 'true', 'TRUE' or true in any combination of lower or upper case
     * letters, or the value 'yes' in any case, or the value '1', it is mapped to 'true'.
     *
     * <p/>If the string value does not map to 'true', it is mapped to 'false'.
     *
     * @param data          The data map to modify.
     * @param booleanColumn The name of the column holding the boolean value to clean up.
     */
    protected void replaceBoolean(Map<String, Object> data, String booleanColumn) {
        String booleanString = (String) data.get(booleanColumn);

        if (nullOrEmpty(booleanString)) {
            data.put(booleanColumn, FALSE);
        } else if ("TRUE".equalsIgnoreCase(booleanString)) {
            data.put(booleanColumn, TRUE);
        } else if ("YES".equalsIgnoreCase(booleanString)) {
            data.put(booleanColumn, TRUE);
        } else {
            data.put(booleanColumn, FALSE);
        }
    }

    /**
     * Extracts data from a list of data rows as JOSN. The named column is removed from the row, and its value returned.
     *
     * @param  data       The data map to modify.
     * @param  columnName The name of the column to remove.
     *
     * @return The contents of the column on this row.
     */
    protected String removeColumn(Map<String, Object> data, String columnName) {
        String result = (String) data.get(columnName);
        data.remove(columnName);

        return result;
    }
}
