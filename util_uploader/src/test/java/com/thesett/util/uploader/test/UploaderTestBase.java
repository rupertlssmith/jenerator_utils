package com.thesett.util.uploader.test;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.thesett.util.uploader.ETLTable;
import com.thesett.util.uploader.Uploader;
import com.thesett.test.rules.BeforeClassResetRule;
import com.thesett.test.rules.FireOnceRule;
import com.thesett.util.commands.refdata.RefDataUtils;
import com.thesett.util.queue.FifoLinkedQueue;
import com.thesett.util.queue.Queue;

/**
 * UploaderTestBase defines test cases against {@link Uploader}s, that are driven by tables of valid and invalid data
 * examples. A check is made that all valid examples are successfully processed, and all invalid examples are rejected.
 */
@RunWith(Parameterized.class)
public abstract class UploaderTestBase {
    /** The reset rule for the fire once rule. */
    @ClassRule
    public static BeforeClassResetRule resetRule = new BeforeClassResetRule();

    /** Fire once detector to permit non-static per-class setups. */
    @Rule
    public FireOnceRule fireOnceRule = new FireOnceRule(resetRule);

    /** The uploader to test. */
    protected Uploader uploader;

    /** Used to stage input data to the uploader. */
    protected Queue<ETLTable> tableSource = new FifoLinkedQueue<>();

    /** Used to capture rejected data from the uploader. */
    protected Queue<ETLTable> errorSink = new FifoLinkedQueue<>();

    /** The current example under test. */
    protected ETLTable example;

    /** <tt>true</tt> iff the current example is expected to be valid. */
    protected boolean expectedValid;

    /** Loads all reference data caches into memory, from their defining CSV files. */
    @Before
    public void loadReferenceData() {
        if (fireOnceRule.shouldFireRule()) {
            RefDataUtils.loadReferenceDataToCacheOnly();
        }
    }

    @Test
    public void testExamples() {
        String tableDump = dumpTable(example);

        int exampleSize = example.getData().size();
        tableSource.offer(example);
        uploader.upload();

        if (expectedValid) {
            if (!errorSink.isEmpty()) {
                Assert.fail("Was not expected to fail: " + tableDump);
            }
        } else {
            if (errorSink.size() != exampleSize) {
                Assert.fail("Was expected to fail: " + tableDump);
            }
        }
    }

    private String dumpTable(ETLTable table) {
        StringBuilder result = new StringBuilder();

        for (Map<String, Object> row : table.getData()) {
            result.append(row.toString());
        }

        return result.toString();
    }
}
