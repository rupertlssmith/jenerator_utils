package com.thesett.util.commands.refdata;

import java.util.Map;

/**
 * Bundles together the name of a reference data table, and the items that should populate it.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Represent the contents of a reference data table. </td></tr>
 * </table></pre>
 */
public class RefDataBundle {
    /** Holds the name of the table. */
    private final String tableName;

    /** Holds the original name of the ref data type in the model. */
    private final String typeName;

    /** Holds the entries for the table. */
    private final Map<Long, String> dataMap;

    /**
     * Creates a reference data bundle.
     *
     * @param tableName The name of the table.
     * @param typeName  The original name of the ref data type in the model.
     * @param dataMap   The entries for the table.
     */
    RefDataBundle(String tableName, String typeName, Map<Long, String> dataMap) {
        this.tableName = tableName;
        this.typeName = typeName;
        this.dataMap = dataMap;
    }

    /**
     * Provides the name of the table.
     *
     * @return The name of the table.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Provides the original name of the ref data type in the model.
     *
     * @return The original name of the ref data type in the model.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Provides the entries for the table.
     *
     * @return The entries for the table.
     */
    public Map<Long, String> getDataMap() {
        return dataMap;
    }
}
