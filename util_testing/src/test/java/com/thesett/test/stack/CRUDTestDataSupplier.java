package com.thesett.test.stack;

import java.io.Serializable;
import java.util.List;

import com.thesett.util.entity.Entity;

public interface CRUDTestDataSupplier<E extends Entity<K>, K extends Serializable> extends KeySupplier<K> {
    /**
     * Sets the id on the initial value AND the updated value.
     *
     * @param id The id to use for the test entities.
     */
    void setId(K id);

    /**
     * Provides an uninitialized instance of the entity, created using its default constructor.
     *
     * @return An uninitialized instance of the entity, created using its default constructor.
     */
    E getDefaultValue();

    /**
     * Supplies the initial value of an entity to test.
     *
     * @return An initial value of an entity to test.
     */
    E getInitialValue();

    /**
     * Supplies an updated value of an entity to test, to attempt to replace the initial value with.
     *
     * @return An update value of an entity to test.
     */
    E getUpdatedValue();

    /**
     * Provides a list of examples that are valid and should insert cleanly into the database.
     *
     * @return A list of examples that are valid and should insert cleanly into the database.
     */
    List<E> examples();

    /**
     * Provides a list of examples that are not valid and should fail to validate.
     *
     * @return A list of examples that are not valid and should fail to validate.
     */
    List<E> counterExamples();

    /**
     * Supplies an equality checker suitable for comparing items of test data by value, without using their .equals()
     * methods.
     *
     * @return An equality checker suitable for comparing items of test data by value.
     */
    ModelEqualityByValue getEqualityChecker();
}
