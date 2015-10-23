package com.thesett.test.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * FireOnceRule is a rule that fires once per test class, but does not need to be declared as a static, which is the
 * normal case for once-per-test-class rules and methods in JUnit. This must be used in conjunction with a
 * {@link BeforeClassResetRule} to reset the behaviour at the end of every test class.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Indicate when a non-static once-per-test-class rule should be fired. </td></tr>
 * </table></pre>
 */
public class FireOnceRule implements TestRule {
    /** <tt>true</tt> iff the rule should be fired. */
    private static boolean fireRule = true;

    /** The reset rule to reset on. */
    private final BeforeClassResetRule beforeClassResetRule;

    /**
     * Creates a fire once rule, using the supplied reset rule to reset at the end of each test class.
     *
     * @param beforeClassResetRule The reset rule to reset on.
     */
    public FireOnceRule(BeforeClassResetRule beforeClassResetRule) {
        this.beforeClassResetRule = beforeClassResetRule;
    }

    /** {@inheritDoc} */
    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
                public void evaluate() throws Throwable {
                    // Here is BEFORE_CODE
                    if (beforeClassResetRule.checkAndConsumeReset()) {
                        fireRule = true;
                    }

                    try {
                        statement.evaluate();
                    } finally {
                        if (fireRule) {
                            fireRule = false;
                        }
                    }
                }
            };
    }

    /**
     * Indicates that once-per-test-class rules should fire.
     *
     * @return <tt>true</tt> iff once-per-test-class rules should fire.
     */
    public boolean shouldFireRule() {
        return fireRule;
    }
}
