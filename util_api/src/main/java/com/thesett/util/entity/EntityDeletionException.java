package com.thesett.util.entity;

/**
 * EntityDeletionException is used to indicate that an entity cannot be delete.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Indicate that an entity cannot be deleted. </td></tr>
 * </table></pre>
 */
public class EntityDeletionException extends Exception {
    /**
     * Creates a validation exception.
     *
     * @param message The validation error message.
     */
    public EntityDeletionException(String message) {
        super(message);
    }
}
