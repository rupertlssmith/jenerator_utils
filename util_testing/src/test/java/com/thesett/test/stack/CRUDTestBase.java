package com.thesett.test.stack;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import com.thesett.test.rules.BeforeClassResetRule;
import com.thesett.test.rules.FireOnceRule;
import com.thesett.util.entity.Entity;
import com.thesett.util.entity.EntityAlreadyExistsException;
import com.thesett.util.entity.EntityDeletionException;
import com.thesett.util.entity.EntityNotExistsException;
import com.thesett.util.entity.EntityValidationException;

public abstract class CRUDTestBase<E extends Entity<K>, K extends Serializable> {
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

    /** An equality by value checker configured for the model being tested. */
    protected final ModelEqualityByValue equality;

    /**
     * Creates the CRUD test.
     *
     * @param equality An equality by value checker configured for the model being tested.
     */
    protected CRUDTestBase(ModelEqualityByValue equality) {
        this.equality = equality;
    }

    /**
     * Creates a test stack made of {@link CRUDTestController}s composed together. This method must set up the
     * {@link #testController} field.
     */
    public abstract void buildTestStack();

    @Test
    public void checkToString() {
        testData.getInitialValue().toString();
    }

    @Test
    public void checkTestDataInitialAndUpdateAreDifferent() {
        Assert.assertFalse("Non-equal initial and update values should be specified in the test data set.",
            equality.checkEqualByValue(testData.getInitialValue(), testData.getUpdatedValue()));

        // Check the equality method too, if it is implemented.
        if (implementsEquality(testData.getInitialValue())) {
            Assert.assertNotEquals(testData.getInitialValue(), testData.getUpdatedValue());
        }
    }

    @Test
    public void checkTestDataInitialIsSet() {
        Assert.assertFalse("Test data should have 'initial' value set.",
            equality.checkEqualByValue(testData.getInitialValue(), null));

        // Check the equality method too, if it is implemented.
        if (implementsEquality(testData.getInitialValue())) {
            Assert.assertNotEquals(testData.getInitialValue(), null);
        }
    }

    @Test
    public void checkTestDataUpdatedIsSet() {
        Assert.assertFalse("Test data should have 'updated' value set.",
            equality.checkEqualByValue(testData.getUpdatedValue(), null));

        // Check the equality method too, if it is implemented.
        if (implementsEquality(testData.getInitialValue())) {
            Assert.assertNotEquals(testData.getUpdatedValue(), null);
        }
    }

    @Test
    public void checkEntityNotEqualToObject() {
        Assert.assertFalse(equality.checkEqualByValue(testData.getInitialValue(), new Object()));

        // Check the equality method too, if it is implemented.
        if (implementsEquality(testData.getInitialValue())) {
            Assert.assertNotEquals(testData.getInitialValue(), new Object());
        }
    }

    @Test
    public void checkEntityNotEqualToNull() {
        Assert.assertFalse(equality.checkEqualByValue(testData.getInitialValue(), null));

        // Check the equality method too, if it is implemented.
        if (implementsEquality(testData.getInitialValue())) {
            Assert.assertNotEquals(testData.getInitialValue(), null);
        }
    }

    @Test
    public void checkEntityEqualToItself() {
        Assert.assertTrue(equality.checkEqualByValue(testData.getInitialValue(), testData.getInitialValue()));

        // Check the equality method too, if it is implemented.
        if (implementsEquality(testData.getInitialValue())) {
            Assert.assertEquals(testData.getInitialValue(), testData.getInitialValue());
        }
    }

    @Test
    public void checkEntityNotEqualToDefaultValue() {
        Assert.assertFalse(equality.checkEqualByValue(testData.getInitialValue(), testData.getDefaultValue()));
        Assert.assertFalse(equality.checkEqualByValue(testData.getDefaultValue(), testData.getInitialValue()));

        Assert.assertFalse(equality.checkEqualByValue(testData.getUpdatedValue(), testData.getDefaultValue()));
        Assert.assertFalse(equality.checkEqualByValue(testData.getDefaultValue(), testData.getUpdatedValue()));

        // Check the equality method too, if it is implemented.
        if (implementsEquality(testData.getInitialValue())) {
            Assert.assertNotEquals(testData.getInitialValue(), testData.getDefaultValue());
            Assert.assertNotEquals(testData.getDefaultValue(), testData.getInitialValue());

            Assert.assertNotEquals(testData.getUpdatedValue(), testData.getDefaultValue());
            Assert.assertNotEquals(testData.getDefaultValue(), testData.getUpdatedValue());
        }
    }

    @Test
    public void checkDefaultHashcode() {
        testData.getDefaultValue().hashCode();
    }

    @Test
    public void checkBusinessKeysOnTestData() {
        Set<E> set = new HashSet<E>();
        set.add(testData.getInitialValue());
        set.add(testData.getUpdatedValue());

        Assert.assertTrue("Entities should implement hashCode to conform with equality by business keys.",
            set.contains(testData.getInitialValue()));
        Assert.assertTrue("Entities should implement hashCode to conform with equality by business keys.",
            set.contains(testData.getUpdatedValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNullFails() throws EntityAlreadyExistsException, EntityValidationException {
        testController.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRetreiveNullFails() throws EntityAlreadyExistsException, EntityValidationException {
        // Create and set id null needed to defeat test prerequisites supplying an id.
        testController.create(testData.getInitialValue());
        testData.setId(null);

        testController.retrieve(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNullFails() throws EntityNotExistsException, EntityAlreadyExistsException,
        EntityValidationException {
        // Create and set id null needed to defeat test prerequisites supplying an id.
        testController.create(testData.getInitialValue());
        testData.setId(null);

        testController.update(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteNullFails() throws EntityAlreadyExistsException, EntityValidationException, EntityDeletionException {
        // Create and set id null needed to defeat test prerequisites supplying an id.
        testController.create(testData.getInitialValue());
        testData.setId(null);

        testController.delete(null);
    }

    @Test
    public void testCreateRetrieve() throws EntityAlreadyExistsException, EntityValidationException {
        E created = testController.create(testData.getInitialValue());
        E retrieved = testController.retrieve(created.getId());

        Assert.assertTrue("Retrieved entity should be equal to created entity.",
            equality.checkEqualByValue(created, retrieved));

        // Check the equality method too, if it is implemented.
        if (implementsEquality(testData.getInitialValue())) {
            Assert.assertEquals(created, retrieved);
        }

        testController.apply(); // Runs the test case and validates the results.
    }

    @Test
    public void testUpdateRetrieve() throws EntityNotExistsException, EntityValidationException {
        E updated = testController.update(testData.getUpdatedValue().getId(), testData.getUpdatedValue());
        E retrieved = testController.retrieve(testData.getInitialValue().getId());

        Assert.assertTrue("Retrieved entity should be equal to updated entity.",
            equality.checkEqualByValue(updated, retrieved));

        // Check the equality method too, if it is implemented.
        if (implementsEquality(testData.getInitialValue())) {
            Assert.assertEquals(updated, retrieved);
        }

        testController.apply(); // Runs the test case and validates the results.
    }

    @Test
    public void testDeleteRetrieve() throws EntityDeletionException {
        testController.delete(testData.getInitialValue().getId());

        E retrieved = testController.retrieve(testData.getInitialValue().getId());

        Assert.assertNull("Retrieved entity should be null, as it was deleted.", retrieved);

        testController.apply(); // Runs the test case and validates the results.
    }

    @Test
    public void testCreateDelete() throws EntityAlreadyExistsException, EntityValidationException, EntityDeletionException {
        E created = testController.create(testData.getInitialValue());
        testController.delete(created.getId());

        testController.apply(); // Runs the test case and validates the results.
    }

    @Test
    public void testRetrieveDelete() throws EntityDeletionException {
        E retrieved = testController.retrieve(testData.getInitialValue().getId());
        testController.delete(testData.getInitialValue().getId());

        testController.apply(); // Runs the test case and validates the results.
    }

    @Test
    public void testUpdateDelete() throws EntityNotExistsException, EntityValidationException, EntityDeletionException {
        E updated = testController.update(testData.getUpdatedValue().getId(), testData.getUpdatedValue());
        testController.delete(testData.getInitialValue().getId());

        testController.apply(); // Runs the test case and validates the results.
    }

    @Test
    public void testRetrieveCreate() throws EntityValidationException {
        boolean createFailed = false;

        try {
            E retrieved = testController.retrieve(testData.getInitialValue().getId());
            E created = testController.create(testData.getInitialValue());
        } catch (EntityAlreadyExistsException e) {
            createFailed = true;
        }

        Assert.assertTrue("Created should have failed, as the entity already exists.", createFailed);

        testController.apply(); // Runs the test case and validates the results.
    }

    @Test
    public void testUpdateCreate() throws EntityNotExistsException, EntityValidationException {
        boolean createFailed = false;

        try {
            E updated = testController.update(testData.getUpdatedValue().getId(), testData.getUpdatedValue());
            E created = testController.create(testData.getInitialValue());
        } catch (EntityAlreadyExistsException e) {
            createFailed = true;
        }

        Assert.assertTrue("Created should have failed, as the entity already exists.", createFailed);

        testController.apply(); // Runs the test case and validates the results.
    }

    @Test
    public void testDeleteCreate() throws EntityAlreadyExistsException, EntityValidationException, EntityDeletionException {
        testController.delete(testData.getInitialValue().getId());

        E created = testController.create(testData.getInitialValue());

        /*Assert.assertFalse("Re-created entity should have a different id to its original instance.",
            testData.getInitialValue().getId(), created.getId());*/

        testController.apply(); // Runs the test case and validates the results.
    }

    @Test
    public void testCreateUpdate() throws EntityAlreadyExistsException, EntityNotExistsException,
        EntityValidationException {
        E created = testController.create(testData.getInitialValue());
        E updated = testController.update(testData.getUpdatedValue().getId(), testData.getUpdatedValue());

        Assert.assertTrue("Update should have returned the updated value of the entity.",
            equality.checkEqualByValue(updated, testData.getUpdatedValue()));

        // Check the equality method too, if it is implemented.
        if (implementsEquality(testData.getInitialValue())) {
            Assert.assertEquals(updated, testData.getUpdatedValue());
        }

        testController.apply(); // Runs the test case and validates the results.
    }

    @Test
    public void testDeleteUpdate() throws EntityValidationException, EntityDeletionException {
        boolean updateFailed = false;

        try {
            testController.delete(testData.getInitialValue().getId());

            E updated = testController.update(testData.getUpdatedValue().getId(), testData.getUpdatedValue());
        } catch (EntityNotExistsException e) {
            updateFailed = true;
        }

        Assert.assertTrue("Updated should have failed, as no entity existed to update.", updateFailed);

        testController.apply(); // Runs the test case and validates the results.
    }

    @Test
    public void testRetrieveUpdate() throws EntityNotExistsException, EntityValidationException {
        E retrieved = testController.retrieve(testData.getInitialValue().getId());
        E updated = testController.update(testData.getUpdatedValue().getId(), testData.getUpdatedValue());

        Assert.assertTrue("Update should have returned the updated value of the entity.",
            equality.checkEqualByValue(updated, testData.getUpdatedValue()));

        // Check the equality method too, if it is implemented.
        if (implementsEquality(testData.getInitialValue())) {
            Assert.assertEquals(updated, testData.getUpdatedValue());
        }

        testController.apply(); // Runs the test case and validates the results.
    }

    /**
     * Checks if an entity being tests implements equals.
     *
     * @param  value The entity to test.
     *
     * @return <tt>true</tt> iff the entity implements equals.
     */
    private boolean implementsEquality(E value) {
        try {
            Class<? extends Entity> aClass = value.getClass();
            Method method = aClass.getMethod("equals", Object.class);

            return method.getDeclaringClass().equals(aClass);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
