package com.thesett.util.swagger;

import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;

import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.util.Iterator;


/**
 * DefaultModelConverter simply forwards to the next converter in the chain, and makes no modifications to the model
 * itself. It is intended to be used as a base class for implementing {@link ModelConverter}s.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> No-op model converter. </td></tr>
 * </table></pre>
 */
public abstract class DefaultModelConverter implements ModelConverter {

    /** {@inheritDoc} */
    public Property resolveProperty(Type type,
        ModelConverterContext modelConverterContext, Annotation[] annotations,
        Iterator<ModelConverter> iterator) {
        return iterator.next().resolveProperty(type, modelConverterContext,
                annotations, iterator);
    }

    /** {@inheritDoc} */
    public Model resolve(Type type, ModelConverterContext modelConverterContext,
        Iterator<ModelConverter> iterator) {
        return iterator.next().resolve(type, modelConverterContext, iterator);
    }
}
