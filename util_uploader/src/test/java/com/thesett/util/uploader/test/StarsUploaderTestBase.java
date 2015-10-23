package com.thesett.util.uploader.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.thesett.util.uploader.ETLTable;
import com.thesett.util.uploader.RowETLProcessor;
import com.thesett.util.uploader.TableETLProcessor;
import com.thesett.util.uploader.TableETLProcessorByType;
import com.thesett.util.uploader.Uploader;
import com.thesett.util.function.Function;

/**
 * StarsUploaderTestBase is an {@link UploaderTestBase} that runs against a mocked out set of Stars services.
 */
public abstract class StarsUploaderTestBase extends UploaderTestBase {
    /**
     * Creates an uploader test case.
     *
     * @param example       The example table to try and upload.
     * @param expectedValid <tt>true</tt> iff the example is expected to be valid.
     */
    public StarsUploaderTestBase(ETLTable example, boolean expectedValid) {
        this.example = example;
        this.expectedValid = expectedValid;
    }

    /**
     * Should supply a mapping from source names to row processors.
     *
     * @return A mapping from source names to row processors.
     */
    public abstract Function<String, RowETLProcessor> getRowProcessorMapping();

    /** {@inheritDoc} */
    public void buildTestStack() {
        TableETLProcessor tableProcessor = new TableETLProcessorByType(null, getRowProcessorMapping());

        uploader = new Uploader(tableSource, errorSink, tableProcessor);
    }

    /**
     * Turns the examples and counter examples from a {@link ETLTableTestDataSupplier} into a suitable format for a
     * parametrized JUnit test case.
     *
     * @param  testData The test data.
     *
     * @return The test data as a collection of test parameters.
     */
    protected static Collection<Object[]> buildExamples(ETLTableTestDataSupplier testData) {
        Collection<Object[]> data = new ArrayList<Object[]>();

        ETLTable examples = testData.examples();
        ETLTable counterExamples = testData.counterExamples();

        // Split the tables into many tables, one row in each.
        splitTable(data, examples, true);
        splitTable(data, counterExamples, false);

        return data;
    }

    /**
     * Splits a table of examples up into many tables with one row in each.
     *
     * @param data          The data collection to add the tables to.
     * @param examples      The example table to split up.
     * @param expectedValid <tt>true</tt> iff the example table contains valid examples.
     */
    private static void splitTable(Collection<Object[]> data, ETLTable examples, boolean expectedValid) {
        for (Map<String, Object> row : examples.getData()) {
            List<Map<String, Object>> singleRowTable = new LinkedList<>();
            singleRowTable.add(row);

            ETLTable table = new ETLTable(examples.getName(), singleRowTable);
            data.add(new Object[] { table, expectedValid });
        }
    }

}
