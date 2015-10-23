package com.thesett.util.uploader.test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BaseTableTestDataSupplier provides some helper methods for constructing tables of test data.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Allow table columns to be defined. </td></tr>
 * <tr><td> Populate table columns. </td></tr>
 * </table></pre>
 */
public abstract class BaseTableTestDataSupplier implements ETLTableTestDataSupplier {
    /** Holds the column names for the test data table. */
    private final String[] columns;

    /**
     * Defines the column names for the test data table.
     *
     * @param columns The column names.
     */
    public BaseTableTestDataSupplier(String... columns) {
        this.columns = columns;
    }

    /**
     * Creates a test table row with the specified values in it.
     *
     * @param  values The values to insert in the table row.
     *
     * @return A test table row.
     */
    protected Map<String, Object> asMap(final String... values) {
        return new LinkedHashMap<String, Object>() {
                {
                    for (int i = 0; i < columns.length; i++) {
                        if (values[i] != null) {
                            put(columns[i], values[i]);
                        }
                    }
                }
            };
    }
}
