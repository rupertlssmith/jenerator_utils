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
package com.thesett.util.hibernate;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.SimpleType;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.usertype.UserType;

/**
 * JsonUserType is a custom Hibernate type that transforms any object into Json for storage.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Store object graphs as JSON in a database column. </td></tr>
 * </table></pre>
 */
public class JsonUserType implements UserType
{
    /** Map the JSON as long varchar type. */
    private static final int[] SQL_TYPES = { Types.LONGVARCHAR, Types.LONGVARCHAR };

    public Class returnedClass()
    {
        return Object.class;
    }

    /** {@inheritDoc} */
    public boolean equals(Object x, Object y) throws HibernateException
    {
        if (x == y)
        {
            return true;
        }
        else if ((x == null) || (y == null))
        {
            return false;
        }
        else
        {
            return x.equals(y);
        }
    }

    /** {@inheritDoc} */
    public int hashCode(Object x) throws HibernateException
    {
        return (null == x) ? 0 : x.hashCode();
    }

    /** {@inheritDoc} */
    public boolean isMutable()
    {
        return true;
    }

    /** {@inheritDoc} */
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
        throws HibernateException, SQLException
    {
        if (value == null)
        {
            st.setString(index, Object.class.getName());
            st.setString(index + 1, null);
        }
        else
        {
            st.setString(index, value.getClass().getName());
            st.setString(index + 1, convertObjectToJson(value));
        }
    }

    /** {@inheritDoc} */
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
        throws HibernateException, SQLException
    {
        Class clazz = null;
        String className = rs.getString(names[0]);

        if (rs.wasNull())
        {
            return null;
        }

        try
        {
            clazz = ReflectHelper.classForName(className, this.getClass());
        }
        catch (ClassNotFoundException e)
        {
            throw new HibernateException("Class " + className + " not found", e);
        }

        String content = rs.getString(names[1]);

        if (rs.wasNull())
        {
            return null;
        }

        return convertJsonToObject(content, clazz);
    }

    /** {@inheritDoc} */
    public Object deepCopy(Object value) throws HibernateException
    {
        if (value == null)
        {
            return null;
        }

        String json = convertObjectToJson(value);

        return convertJsonToObject(json, value.getClass());
    }

    /** {@inheritDoc} */
    public Object replace(Object original, Object target, Object owner) throws HibernateException
    {
        return deepCopy(original);
    }

    /** {@inheritDoc} */
    public Serializable disassemble(Object value) throws HibernateException
    {
        return (Serializable) deepCopy(value);
    }

    /** {@inheritDoc} */
    public Object assemble(Serializable cached, Object owner) throws HibernateException
    {
        return deepCopy(cached);
    }

    /**
     * Creates a Jackson Java type mapping for the type of class returned by {@link #returnedClass()}.
     *
     * @param  mapper The object mapper.
     *
     * @return A Jackson Java type mapping for the type of class returned by {@link #returnedClass()}.
     */
    public JavaType createJavaType(ObjectMapper mapper)
    {
        return SimpleType.construct(returnedClass());
    }

    /** {@inheritDoc} */
    public int[] sqlTypes()
    {
        return SQL_TYPES;
    }

    /**
     * Transforms a JSON string into an object.
     *
     * @param  content The JSON string to parse.
     * @param  clazz   The class to parse into.
     *
     * @return The JSON as an object.
     */
    Object convertJsonToObject(String content, Class clazz)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(content, clazz);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Transforms an object into JSON.
     *
     * @param  object The object to convert to JSON.
     *
     * @return The object as a JSON string.
     */
    String convertObjectToJson(Object object)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            return mapper.writeValueAsString(object);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
