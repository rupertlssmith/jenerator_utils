package com.thesett.util.swagger;

import com.fasterxml.jackson.databind.type.SimpleType;

import com.thesett.util.archetype.EnumType;

import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;

import io.swagger.models.Model;

import java.lang.reflect.Type;

import java.util.Iterator;


/**
 * EnumTypeModelConverter matches jenerator enum types, and supplies a model that hides the implementation details,
 * and presents the enum types as {@link com.thesett.util.model.RefDataItem}s.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Present enum types as reference data items. </td></tr>
 * </table></pre>
 */
public class EnumTypeModelConverter extends DefaultModelConverter {

    /** {@inheritDoc} */
    public Model resolve(Type type, ModelConverterContext modelConverterContext,
        Iterator<ModelConverter> iterator) {

        if (type instanceof SimpleType) {
            SimpleType simpleType = (SimpleType) type;

            Class<?> rawClass = simpleType.getRawClass();

            if (EnumType.class.isAssignableFrom(rawClass)) {
                System.out.println(rawClass);
            }
        }

        return super.resolve(type, modelConverterContext, iterator);
    }
}
