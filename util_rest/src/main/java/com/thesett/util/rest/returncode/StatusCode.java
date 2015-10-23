package com.thesett.util.rest.returncode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.NameBinding;

/**
 * StatusCode is an annotation making use of {@link NameBinding}, to implement a default HTTP status code override
 * feature. This means the normal status code can be supplied in an annotation and the interface does not need to
 * explicitly reference the response, leading to cleaner interfaces.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Allow default HTTP response codes to be specified in annotations. </td></tr>
 * </table></pre>
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface StatusCode {
    /**
     * Supplies the HTTP response code for the success case.
     *
     * @return The HTTP response code for the success case.
     */
    int code();
}
