package com.thesett.util.entity;

/**
 * EntityValidationException is used to indicate that an entity is invalid. This is often raised when an entity is being
 * persisted to a database, or during validation checks prior to storing an entity, but can be used in other situations
 * too.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Indicate that an entity is not valid for purpose. </td></tr>
 * </table></pre>
 */
public class EntityValidationException extends EntityException {
    /**
     * Creates a validation exception.
     *
     * @param message The validation error message.
     */
    public EntityValidationException(String message) {
        super(message);
    }
}
