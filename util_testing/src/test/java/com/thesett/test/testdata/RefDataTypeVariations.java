package com.thesett.test.testdata;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import com.thesett.util.memento.BeanMemento;
import com.thesett.util.memento.DirectMemento;
import com.thesett.util.memento.Memento;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.common.util.ReflectionUtils;

/**
 * RefDataTypeVariations provides an iterator over reference data instances, that have been constructed using the
 * different methods available; by name, by id, by NamedRefImpl, and by EnumeratedStringAttribute instance. This is
 * useful for constructing test data instances that cover the available methods.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Provide a list of reference data instances, constructed using different methods. </td></tr>
 * <tr><td> Provide a list of all valid reference data instances. </td></tr>
 * <tr><td> Provide a list of invalid reference data instances. </td></tr>
 * </table></pre>
 */
public class RefDataTypeVariations {
    /**
     * Provides a list of reference data instances, constructed using different methods.
     *
     * @param  type An instance of the reference data to supply variations of.
     * @param  <T>  The type of the reference data.
     *
     * @return A list of differently constructed instances.
     */
    public static <T> List<T> constructorVariations(T type) {
        LinkedList<T> variations = new LinkedList<T>();

        BeanMemento memento = new BeanMemento(type);
        memento.capture();

        Class typeClass = type.getClass();
        String typeName = StringUtils.uncapitalize(typeClass.getSimpleName());

        try {
            variations.add((T) ReflectionUtils.getConstructor(typeClass, new Class[] { long.class })
                .newInstance(memento.get(typeClass, "id")));
            variations.add((T) ReflectionUtils.getConstructor(typeClass, new Class[] { String.class })
                .newInstance(memento.get(typeClass, "id").toString()));
            variations.add((T) ReflectionUtils.getConstructor(typeClass,
                    new Class[] { EnumeratedStringAttribute.class }).newInstance(memento.get(typeClass, typeName)));
            variations.add((T) ReflectionUtils.getConstructor(typeClass, new Class[] { String.class })
                .newInstance(((EnumeratedStringAttribute) memento.get(typeClass, typeName)).getStringValue()));
        } catch (NoSuchFieldException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }

        checkAllEqual(variations);
        checkAllNotNull(variations);
        checkAllNotEqualTo(variations, new Object());

        return variations;
    }

    /**
     * Given a component, produces a list of valid variations on it against a particular reference data type.
     *
     * @param  component        The component to produce variations of.
     * @param  fieldName        The name of the component field to vary.
     * @param  refDataTypeClass The class of the reference data type to vary.
     * @param  <C>              The component type.
     * @param  <T>              The reference data type.
     *
     * @return A list of components copied from the original, with the particular reference data type set to all valid
     *         variations.
     */
    public static <C, T> List<C> withValidVariations(C component, String fieldName, Class<T> refDataTypeClass) {
        List<C> results = new LinkedList<>();

        BeanMemento memento = new BeanMemento(component);
        memento.capture();

        Class componentClass = component.getClass();

        List<T> validVariations = validVariations(refDataTypeClass);

        createComponentVariations(fieldName, results, memento, componentClass, validVariations);

        return results;
    }

    /**
     * Given a component, produces a list of invalid variations on it against a particular reference data type.
     *
     * @param  component        The component to produce variations of.
     * @param  fieldName        The name of the component field to vary.
     * @param  refDataTypeClass The class of the reference data type to vary.
     * @param  <C>              The component type.
     * @param  <T>              The reference data type.
     *
     * @return A list of components copied from the original, with the particular reference data type set to invalid
     *         variations.
     */
    public static <C, T> List<C> withInvalidVariations(C component, String fieldName, Class<T> refDataTypeClass) {
        List<C> results = new LinkedList<>();

        BeanMemento memento = new BeanMemento(component);
        memento.capture();

        Class componentClass = component.getClass();

        List<T> invalidVariations = invalidVariations(refDataTypeClass);

        createComponentVariations(fieldName, results, memento, componentClass, invalidVariations);

        return results;
    }

    /**
     * Given a component, produces a list of valid variations on it against a particular reference data type. This is
     * for the case where the reference data values are held as a set on the component, so more than one can be selected
     * at a time. The full set of variations can easily be very large in that case. This method produces a set that
     * consists of the first value, then the first and the next, then incrementally adding more values to the set. It
     * results in the same number of variations as the {@link #withValidVariations(Object, String, Class)} method, but
     * incrementally building up larger sets as described.
     *
     * @param  component        The component to produce variations of.
     * @param  fieldName        The name of the component field to vary.
     * @param  refDataTypeClass The class of the reference data type to vary.
     * @param  <C>              The component type.
     * @param  <T>              The reference data type.
     *
     * @return A list of components copied from the original, with the particular reference data type set to all valid
     *         variations.
     */
    public static <C, T> List<C> withSetOfValidVariations(C component, String fieldName, Class<T> refDataTypeClass) {
        List<C> results = new LinkedList<>();

        Memento memento = new DirectMemento(component);
        memento.capture();

        Class componentClass = component.getClass();

        List<T> validVariations = validVariations(refDataTypeClass);

        createComponentSetOfVariations(fieldName, results, memento, componentClass, validVariations);

        return results;
    }

    public static <C, T> List<C> withSetOfInvalidVariations(C component, String fieldName, Class<T> refDataTypeClass) {
        List<C> results = new LinkedList<>();

        Memento memento = new DirectMemento(component);
        memento.capture();

        Class componentClass = component.getClass();

        List<T> invalidVariations = invalidVariations(refDataTypeClass);

        createComponentSetOfVariations(fieldName, results, memento, componentClass, invalidVariations);

        return results;
    }

    /**
     * Provides a list of all valid reference data instances.
     *
     * @param  typeClass The class of the reference data type.
     * @param  <T>       The type of the reference data.
     *
     * @return A list of all valid reference data instances.
     */
    public static <T> List<T> validVariations(Class<T> typeClass) {
        LinkedList<T> variations = new LinkedList<T>();

        String typeName = StringUtils.uncapitalize(typeClass.getSimpleName());
        EnumeratedStringAttribute.EnumeratedStringAttributeFactory factory =
            EnumeratedStringAttribute.getFactoryForClass(typeName);

        for (EnumeratedStringAttribute attribute : factory.getType().getAllPossibleValuesSet(false)) {
            try {
                // Check if the enum is valid, invalid ones may have been created before this is called.
                if (attribute.getId() != -1) {
                    T typeInstance =
                        ReflectionUtils.getConstructor(typeClass, new Class[] { EnumeratedStringAttribute.class })
                        .newInstance(attribute);
                    variations.addAll(constructorVariations(typeInstance));
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        return variations;
    }

    /**
     * Provides a list of invalid reference data instances.
     *
     * @param  typeClass The class of the reference data type.
     * @param  <T>       The type of the reference data.
     *
     * @return A list of invalid reference data instances.
     */
    public static <T> List<T> invalidVariations(Class<T> typeClass) {
        LinkedList<T> variations = new LinkedList<T>();

        String typeName = StringUtils.uncapitalize(typeClass.getSimpleName());
        EnumeratedStringAttribute.EnumeratedStringAttributeFactory factory =
            EnumeratedStringAttribute.getFactoryForClass(typeName);

        try {
            T typeInstance = ReflectionUtils.getConstructor(typeClass, new Class[] { long.class }).newInstance(-1L);
            variations.addAll(constructorVariations(typeInstance));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }

        return variations;
    }

    /**
     * Checks that all of a set of variations are equal to each other.
     *
     * @param variations The set of variations to check.
     * @param <T>        The type of the reference data.
     */
    private static <T> void checkAllEqual(LinkedList<T> variations) {
        for (T variation : variations) {
            for (T compareTo : variations) {
                Assert.assertEquals(variation, compareTo);
            }
        }
    }

    /**
     * Checks that all of a set of variations are not equal to some value.
     *
     * @param variations The set of variations to check.
     * @param value      The variation to check not equal to.
     * @param <T>        The type of the reference data.
     */
    private static <T> void checkAllNotEqualTo(LinkedList<T> variations, Object value) {
        for (T variation : variations) {
            Assert.assertNotEquals(variation, value);
        }
    }

    /**
     * Checks that all of a set of variations are not null and not equal to null by their equals methods.
     *
     * @param variations The set of variations to check.
     * @param <T>        The type of the reference data.
     */
    private static <T> void checkAllNotNull(LinkedList<T> variations) {
        for (T variation : variations) {
            Assert.assertNotNull(variation);
            Assert.assertNotEquals(variation, null);
        }
    }

    private static <C, T> void createComponentSetOfVariations(String fieldName, List<C> results, Memento memento,
        Class componentClass, List<T> validVariations) {
        Set<T> validSetOfVariations = new HashSet<>();

        for (T variation : validVariations) {
            validSetOfVariations.add(variation);

            Set<T> copyOfValidSetOfVariations = new HashSet<>(validSetOfVariations);

            C componentVariation = (C) ReflectionUtils.newInstance(componentClass);
            memento.put(componentClass, fieldName, copyOfValidSetOfVariations);

            try {
                memento.restore(componentVariation);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException(e);
            }

            results.add(componentVariation);
        }
    }

    private static <C, T> void createComponentVariations(String fieldName, List<C> results, BeanMemento memento,
        Class componentClass, List<T> validVariations) {
        for (T variation : validVariations) {
            C componentVariation = (C) ReflectionUtils.newInstance(componentClass);
            memento.put(componentClass, fieldName, variation);

            try {
                memento.restore(componentVariation);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException(e);
            }

            results.add(componentVariation);
        }
    }
}
