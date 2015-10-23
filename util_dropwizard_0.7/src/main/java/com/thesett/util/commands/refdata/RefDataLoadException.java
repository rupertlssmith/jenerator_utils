package com.thesett.util.commands.refdata;

/**
 * RefDataLoadException is used to report errors encountered whilst loading reference data.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Indicate a failure to load reference data. </td></tr>
 * </table></pre>
 */
public class RefDataLoadException extends Exception {
    /** Error code prefix. */
    private final String prefix;

    /** Error code. */
    private final int code;

    /**
     * Creates a reference data loader exception.
     *
     * @param message A message for the user.
     * @param prefix  The error type prefix.
     * @param code    The error code.
     * @param cause   Any underlying cause.
     */
    public RefDataLoadException(String message, String prefix, int code, Throwable cause) {
        super(message, cause);
        this.prefix = prefix;
        this.code = code;
    }

    /** {@inheritDoc} */
    public String getMessage() {
        return prefix + code + ": " + super.getMessage();
    }
}
