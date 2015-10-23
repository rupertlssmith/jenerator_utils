package com.thesett.util.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * DateTimeParamConverterProvider converts JodaTime DateTime parameters to and from ISO format.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Convert JodaTime DateTime parameters for serialization over REST/JSON. </td></tr>
 * </table></pre>
 */
@Provider
public class DateTimeParamConverterProvider implements ParamConverterProvider {
    /** {@inheritDoc} */
    public <T> ParamConverter<T> getConverter(Class<T> type, Type genericType, Annotation[] annotations) {
        if (type.equals(DateTime.class)) {
            return (ParamConverter<T>) new DateTimeParamConverter();
        } else {
            return null;
        }

    }

    private static class DateTimeParamConverter implements ParamConverter<DateTime> {
        /** {@inheritDoc} */
        public DateTime fromString(String value) {
            try {
                return ISODateTimeFormat.dateTimeNoMillis().parseDateTime(value);
            } catch (IllegalArgumentException e) {
                return ISODateTimeFormat.dateTime().parseDateTime(value);
            }
        }

        /** {@inheritDoc} */
        public String toString(DateTime value) {
            return value.toString();
        }
    }
}
