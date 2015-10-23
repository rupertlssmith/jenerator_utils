package com.thesett.test.stack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.thesett.test.rules.BeforeClassResetRule;
import com.thesett.test.rules.FireOnceRule;
import com.thesett.util.commands.refdata.RefDataUtils;
import com.thesett.util.entity.Entity;
import com.thesett.util.entity.EntityAlreadyExistsException;
import com.thesett.util.entity.EntityValidationException;

@RunWith(Parameterized.class)
public abstract class ValidationTestBase<E extends Entity<K>, K extends Serializable> {
    /** The reset rule for the fire once rule. */
    @ClassRule
    public static BeforeClassResetRule resetRule = new BeforeClassResetRule();

    /** Fire once detector to permit non-static per-class setups. */
    @Rule
    public FireOnceRule fireOnceRule = new FireOnceRule(resetRule);

    /** A test controller to coordinate the test case. */
    protected CRUDTestController<E, K> testController;

    /** The test data supplier for the test case. */
    protected CRUDTestDataSupplier<E, K> testData;

    /** The example to test. */
    protected E example;

    /** <tt>true</tt> iff the {@link #example} is valid. */
    protected boolean expectedValid;

    /** An equality by value checker configured for the model being tested. */
    protected final ModelEqualityByValue equality;

    /**
     * Creates the validation test.
     *
     * @param equality An equality by value checker configured for the model being tested.
     */
    protected ValidationTestBase(ModelEqualityByValue equality) {
        this.equality = equality;
    }

    /**
     * Creates a test stack made of {@link CRUDTestController}s composed together. This method must set up the
     * {@link #testController} field.
     */
    public abstract void buildTestStack();

    public static <E extends Entity<K>, K extends Serializable> Collection<Object[]> buildExamples(
        CRUDTestDataSupplier<E, K> testData) {
        RefDataUtils.loadReferenceDataToCacheOnly();

        Collection<Object[]> data = new ArrayList<Object[]>();

        List<E> examples = testData.examples();
        List<E> counterExamples = testData.counterExamples();

        for (E example : examples) {
            data.add(new Object[] { example, true });
        }

        for (E counterExample : counterExamples) {
            data.add(new Object[] { counterExample, false });
        }

        return data;
    }

    @Test
    public void testCreateRetrieve() {
        boolean failedValidation = false;

        try {
            E created = testController.create(example);
            E retrieved = testController.retrieve(example.getId());

            if (!equality.checkEqualByValue(created, retrieved)) {
                Assert.fail("Retrieved entity should be equal to created entity.\n" + "created is " +
                    equality.toStringByValue(created) + "\n" + "retrieved is " + equality.toStringByValue(retrieved));
            }

            testController.apply(); // Runs the test case and validates the results.
        } catch (EntityAlreadyExistsException e) {
            throw new IllegalStateException(e);
        } catch (EntityValidationException e) {
            failedValidation = true;
            Assert.assertFalse("Failed validation but was expected to pass: " + e.getMessage(), expectedValid);
        }

        if (!expectedValid) {
            Assert.assertTrue("Did not fail validation but was expected to fail: " + example, failedValidation);
        }
    }
}
