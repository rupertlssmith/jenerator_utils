package com.thesett.util.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import com.thesett.util.string.StringUtils;

/**
 * JodaTimeModule is a Jackson serializer module for working with JodaTime data types.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Serialize/deserialize LocalDate  </td></tr>
 * </table></pre>
 */
public class JodaTimeModule extends SimpleModule {
    /** Creates a JodaTime Jackson serializer. */
    public JodaTimeModule() {
        addDeserializer(LocalDate.class, new LocalDateDeserializer());
        addSerializer(LocalDate.class, new LocalDateSerializer());
        addDeserializer(DateTime.class, new DateTimeDeserializer());
        addSerializer(DateTime.class, new DateTimeSerializer());
    }

    /**
     * Deserializer for LocalDate.
     */
    public static class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
        /** {@inheritDoc} */
        public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
                String valueAsString = jp.getValueAsString();

                if (!StringUtils.nullOrEmpty(valueAsString)) {
                    return new LocalDate(valueAsString);
                } else {
                    return null;
                }
            }

            throw ctxt.mappingException("Expected JSON String");
        }
    }

    /**
     * Serializer for LocalDate.
     */
    public static class LocalDateSerializer extends JsonSerializer<LocalDate> {
        /** {@inheritDoc} */
        public void serialize(LocalDate value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.toString());
        }
    }

    /**
     * Deserializer for DateTime.
     */
    public static class DateTimeDeserializer extends JsonDeserializer<DateTime> {
        /** {@inheritDoc} */
        public DateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
                String valueAsString = jp.getValueAsString();

                if (!StringUtils.nullOrEmpty(valueAsString)) {
                    return new DateTime(jp.getValueAsString());
                } else {
                    return null;
                }
            }

            throw ctxt.mappingException("Expected JSON String");
        }
    }

    /**
     * Serializer for DateTime.
     */
    public static class DateTimeSerializer extends JsonSerializer<DateTime> {
        /** {@inheritDoc} */
        public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.toString());
        }
    }
}
