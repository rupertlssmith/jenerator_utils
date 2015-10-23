package com.thesett.util.config.hyperexpress;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.strategicgains.hyperexpress.domain.hal.HalResource;
import com.strategicgains.hyperexpress.serialization.jackson.HalResourceDeserializer;
import com.strategicgains.hyperexpress.serialization.jackson.HalResourceSerializer;

/**
 * HalResourceModule is a Jackson module for HyperExpress, that packages its serializer and deserializer for processing
 * HAL.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Provide serdes for HAL.  </td></tr>
 * </table></pre>
 */
public class HalResourceModule extends SimpleModule {
    /** Creates a module for serdes on HAL. */
    public HalResourceModule() {
        addDeserializer(HalResource.class, new HalResourceDeserializer());
        addSerializer(HalResource.class, new HalResourceSerializer());
    }
}
