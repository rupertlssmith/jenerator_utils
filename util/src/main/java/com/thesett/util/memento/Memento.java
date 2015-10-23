package com.thesett.util.memento;

import java.util.Collection;

/**
 * A Memento provides indirect access to the fields of an object, allowing them all to be accessed by name. This enables
 * the state of an object to be decoupled from the object itself. Through this mechanism any object can have a snapshot
 * of its state externalized or its state restored from such a snapshot.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities
 * <tr><td> Create a memento from an object.
 * <tr><td> Write to an objects fields from a memento.
 * <tr><td> Read field values.
 * <tr><td> Modifiy field values.
 * <tr><td> Get list of all fields.
 * </table></pre>
 */
public interface Memento {
    /** Captures an objects properties in this memento. */
    void capture();

    /** Captures an objects non-null properties in this memento. */
    void captureNonNull();

    /**
     * Restores the properties currently in this memento to the specified object.
     *
     * @param  ob The object to which the values from this memento should be restored.
     *
     * @throws NoSuchFieldException If a setter method could not be found for a property.
     */
    void restore(Object ob) throws NoSuchFieldException;

    /**
     * Gets the value of the named property of the specified class.
     *
     * @param  cls      The class in which the property to get is declared.
     * @param  property The name of the property.
     *
     * @return The object value of the property.
     *
     * @throws NoSuchFieldException If the named field does not exist on the class.
     */
    Object get(Class cls, String property) throws NoSuchFieldException;

    /**
     * Places the specified value into the memento based on the property's declaring class and name.
     *
     * @param cls      The class in which the property is declared.
     * @param property The name of the property.
     * @param value    The value to store into this memento.
     */
    void put(Class cls, String property, Object value);

    /**
     * Removes a field value from the memento, if it exists.
     *
     * @param cls  The class to remove the field from.
     * @param name The name of the field to remove.
     */
    void removeField(Class cls, String name);

    /**
     * Generates a list of all the fields of the object that this memento maps for a given class.
     *
     * @param  cls The class to get all field names for.
     *
     * @return A collection of the field names or null if the specified class is not part of the objects class hierarchy
     *         chain.
     */
    Collection<String> getAllFieldNames(Class cls);
}
