package com.thesett.util.uploader.test;

import com.thesett.util.uploader.ETLTable;

/**
 * ETLTableTestDataSupplier describes a supplier of examples and counter-examples of tables of data to pass to an ETL
 * process, in order to confirm the successful processing of the examples, and rejection of the counter-examples.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Provide examples of valid data. </td></tr>
 * </table> Provide counter-examples of invalid data. </pre>
 */
public interface ETLTableTestDataSupplier {
    /**
     * Provides a list of examples that are valid and should process cleanly.
     *
     * @return A list of examples that are valid and should process cleanly.
     */
    ETLTable examples();

    /**
     * Provides a list of examples that are not valid and should fail to process.
     *
     * @return A list of examples that are not valid and should fail to process.
     */
    ETLTable counterExamples();
}
