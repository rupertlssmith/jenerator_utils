package com.thesett.util.uploader;

import java.util.List;
import java.util.Map;

/**
 * ETLTable describes a table of data for ETL processing. It provides the actual data, along with a name that identifies
 * the source or type of the data.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Pair a table name with table data. </td></tr>
 * </table></pre>
 */
public class ETLTable {
    private final String name;
    private final List<Map<String, Object>> data;

    public ETLTable(String name, List<Map<String, Object>> data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }
}
