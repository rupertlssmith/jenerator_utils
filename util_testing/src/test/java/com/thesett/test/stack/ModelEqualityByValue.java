/*
 * Copyright The Sett Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thesett.test.stack;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thesett.util.entity.Entity;
import com.thesett.util.memento.BeanMemento;
import com.thesett.util.memento.Memento;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * ModelEqualityByValue provides a method to compare two java beans fields for equality, externally to the bean itself,
 * so the bean does not have to implement an equals method. This is useful for entities with no equality, since equality
 * by id should not be used with hibernate, otherwise you cannot use the objects in a set or as keys in a map when they
 * are not yet initialized with database ids.
 *
 * <p/>When {@link Entity}s are encountered as fields of the objects being compares, they are only checked for equality
 * by id. This is because Hibernate may have not yet fetched their values, only identified which database entity they
 * are. This allows the equality test to work regardless of the slicing being applied to the model.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Compare beans for equality field by field. </td></tr>
 * </table></pre>
 */
public class ModelEqualityByValue
{
    private final String[] fieldsToExclude;

    public ModelEqualityByValue(String[] fieldsToExclude)
    {
        this.fieldsToExclude = fieldsToExclude;
    }

    /**
     * Checks if the fields of two objects are equal.
     *
     * @param  o1 The first object to check.
     * @param  o2 The second object to check.
     *
     * @return <tt>true</tt> iff the fields of the two objects are equal.
     */
    public boolean checkEqualByValue(Object o1, Object o2)
    {
        // Both sides null is trivially equal.
        if ((o1 == null) && (o2 == null))
        {
            return true;
        }

        // Allow empty collections and maps to match null.
        if ((o1 instanceof Map) && (o2 == null))
        {
            return ((Map) o1).isEmpty();
        }

        if ((o1 instanceof Collection) && (o2 == null))
        {
            return ((Collection) o1).isEmpty();
        }

        if ((o2 instanceof Map) && (o1 == null))
        {
            return ((Map) o2).isEmpty();
        }

        if ((o2 instanceof Collection) && (o1 == null))
        {
            return ((Collection) o2).isEmpty();
        }

        // Non-empty collections must match element by element.
        if ((o1 instanceof List) && (o2 instanceof List))
        {
            List list1 = (List) o1;
            List list2 = (List) o2;

            if (list1.size() != list2.size())
            {
                return false;
            }

            Iterator it1 = list1.iterator();
            Iterator it2 = list2.iterator();

            boolean result = true;

            for (int i = 0; i < list1.size(); i++)
            {
                Object item1 = it1.next();
                Object item2 = it2.next();

                result &= checkEqualByValue(item1, item2);

                if (!result)
                {
                    return false;
                }
            }

            return true;
        }

        // One side null and the other not cannot be equal (other than empty collections and maps above).
        if ((o1 == null) && (o2 != null))
        {
            return false;
        }

        if ((o2 == null) && (o1 != null))
        {
            return false;
        }

        // Compare field value types by their equality methods.
        if ((o1 instanceof Integer) || (o1 instanceof String) || (o1 instanceof Long) || (o1 instanceof Float) ||
                (o1 instanceof DateTime) || (o1 instanceof LocalDate) || (o1 instanceof Boolean) ||
                o1.getClass().getName().endsWith("Type"))
        {
            return o1.equals(o2);
        }

        // BigDecimal is special because compareTo must be used to accurately check for equality by value.
        if (o1 instanceof BigDecimal)
        {
            return ((BigDecimal) o1).compareTo((BigDecimal) o2) == 0;
        }

        // Capture the fields on the beans using mementos.
        Memento memento1 = new BeanMemento(o1);
        memento1.capture();

        Memento memento2 = new BeanMemento(o2);
        memento2.capture();

        // Remove fields to be ignored.
        removeFields(memento1, o1);
        removeFields(memento2, o2);

        // Compare all fields of the beans, field-by-field, recursively.
        Set<String> allFields = new HashSet<>();
        allFields.addAll(memento1.getAllFieldNames(o1.getClass()));
        allFields.addAll(memento2.getAllFieldNames(o2.getClass()));

        for (String field : allFields)
        {
            Object field1 = null;
            Object field2 = null;

            try
            {
                field1 = memento1.get(o1.getClass(), field);
                field2 = memento2.get(o2.getClass(), field);
            }
            catch (NoSuchFieldException e)
            {
                return false;
            }

            if ((field1 instanceof Entity) && (field2 instanceof Entity) && (((Entity) field1).getId() != null) &&
                    (((Entity) field2).getId() != null))
            {
                return checkEqualByValue(((Entity) field1).getId(), ((Entity) field2).getId());
            }
            else if (!checkEqualByValue(field1, field2))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the fields of two objects are equal.
     *
     * @param  o1 The first object to check.
     *
     * @return <tt>true</tt> iff the fields of the two objects are equal.
     */
    public String toStringByValue(Object o1)
    {
        if (o1 == null)
        {
            return "";
        }

        // Allow empty collections and maps to match null.
        if (o1 instanceof Map)
        {
            return "[...]";
        }

        if (o1 instanceof Collection)
        {
            return "[...]";
        }

        if ((o1 instanceof Integer) || (o1 instanceof String) || (o1 instanceof Long) || (o1 instanceof Float) ||
                (o1 instanceof DateTime) || (o1 instanceof LocalDate) || (o1 instanceof Boolean) ||
                (o1 instanceof BigDecimal) || o1.getClass().getName().endsWith("Type"))
        {
            return o1.toString();
        }

        // Capture the fields on the bean using mementos.
        Memento memento1 = new BeanMemento(o1);
        memento1.capture();
        removeFields(memento1, o1);

        // Compare all fields of the beans, field-by-field, recursively.
        String result = o1.getClass().getSimpleName() + " [ ";

        for (String field : memento1.getAllFieldNames(o1.getClass()))
        {
            Object value = null;

            try
            {
                value = memento1.get(o1.getClass(), field);
            }
            catch (NoSuchFieldException e)
            {
                result += field + " = " + toStringByValue(value);
            }
        }

        return result + " ]";
    }

    private void removeFields(Memento memento, Object o)
    {
        for (String field : fieldsToExclude)
        {
            memento.removeField(o.getClass(), field);
        }
    }
}
