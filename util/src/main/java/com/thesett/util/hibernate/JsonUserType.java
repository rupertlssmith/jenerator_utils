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
import org.hibernate.usertype.UserType;

/**
 * JsonUserType is a custom Hibernate type that transforms any object into JSON and stores it as a long varchar.
 *
 * <p/>This is an abstract class and needs to be extended to implement the {@link #returnedClass()} method, which
 * specified which class this works with.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Store object graphs as JSON in a database column. </td></tr>
 * </table></pre>
 */
public abstract class JsonUserType implements UserType {
    /** Map the JSON as long varchar type. */
    private static final int[] SQL_TYPES = { Types.LONGVARCHAR };

    /** {@inheritDoc} */
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y) {
            return true;
        } else if (x == null || y == null) {
            return false;
        } else {
            return x.equals(y);
        }
    }

    /** {@inheritDoc} */
    public int hashCode(Object x) throws HibernateException {
        return null == x ? 0 : x.hashCode();
    }

    /** {@inheritDoc} */
    public boolean isMutable() {
        return true;
    }

    /** {@inheritDoc} */
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
        throws HibernateException, SQLException {
        if (value == null) {
            st.setString(index, null);
        } else {
            st.setString(index, convertObjectToJson(value));
        }
    }

    /** {@inheritDoc} */
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
        throws HibernateException, SQLException {
        if (!rs.wasNull()) {
            String content = rs.getString(names[0]);

            if (content != null) {
                return convertJsonToObject(content);
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    public Object deepCopy(Object value) throws HibernateException {
        String json = convertObjectToJson(value);

        return convertJsonToObject(json);
    }

    /** {@inheritDoc} */
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }

    /** {@inheritDoc} */
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    /** {@inheritDoc} */
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    /**
     * Creates a Jackson Java type mapping for the type of class returned by {@link #returnedClass()}.
     *
     * @param  mapper The object mapper.
     *
     * @return A Jackson Java type mapping for the type of class returned by {@link #returnedClass()}.
     */
    public JavaType createJavaType(ObjectMapper mapper) {
        return SimpleType.construct(returnedClass());
    }

    /** {@inheritDoc} */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * Transforms a JSON string into an object.
     *
     * @param  content The JSON string to parse.
     *
     * @return The JSON as an object.
     */
    Object convertJsonToObject(String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaType type = createJavaType(mapper);

            return mapper.readValue(content, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transforms an object into JSON.
     *
     * @param  object The object to convert to JSON.
     *
     * @return The object as a JSON string.
     */
    String convertObjectToJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
