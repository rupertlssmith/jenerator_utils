package com.thesett.test.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * BeforeClassResetRule maintains a 'reset' flag, that is set at the end of each test class. This reset can be consumed
 * in order to implement actions that require a reset at the end of each test class.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Set a reset flag at the end of each test class. </td></tr>
 * </table></pre>
 */
public class BeforeClassResetRule implements TestRule {
    /** <tt>true</tt> iff the rule should be fired. */
    private boolean fireRule = true;

    /** The reset flag. */
    private boolean reset = false;

    /** {@inheritDoc} */
    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
                public void evaluate() throws Throwable {
                    try {
                        statement.evaluate();
                    } finally {
                        reset = true;
                    }
                }
            };
    }

    /**
     * Checks if the reset flag is set, and sets it to <tt>false</tt> if it is. When this happens, <tt>true</tt> is
     * returned to indicate that a reset should be performed.
     *
     * @return <tt>true</tt> iff the reset flag has been set.
     */
    public boolean checkAndConsumeReset() {
        if (reset) {
            reset = false;

            return true;
        }

        return false;
    }

    public boolean isFireRule() {
        return fireRule;
    }

    public void setFireRule(boolean fireRule) {
        this.fireRule = fireRule;
    }
}
