package com.thesett.util.entity;

/**
 * EntityException denotes an error whilst performing an operation on an entity. This acts as the parent of the
 * hierarchy of all entity exception.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Denotes an error whilst performing an operation on an entity. </td></tr>
 * </table></pre>
 */
public class EntityException extends Exception {
    /** Creates an entity exception. */
    public EntityException() {
    }

    /**
     * Creates an entity exception with an error message.
     *
     * @param message The error message.
     */
    public EntityException(String message) {
        super(message);
    }
}
