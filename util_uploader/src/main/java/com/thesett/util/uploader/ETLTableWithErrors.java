package com.thesett.util.uploader;

import java.util.List;
import java.util.Map;

/**
 * ETLTableWithErrors is an {@link ETLTable} with additional error messages against its rows.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Pair table data with error messages. </td></tr>
 * </table></pre>
 */
public class ETLTableWithErrors extends ETLTable {
    private final List<String> messages;

    public ETLTableWithErrors(String name, List<Map<String, Object>> data, List<String> messages) {
        super(name, data);
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
