package com.thesett.util.uploader;

import java.util.List;

/**
 * UploadMultiException is a {@link UploadException} that collects together multiple errors.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Collate a list of multiple CSV load errors. </td></tr>
 * </table></pre>
 */
public class UploadMultiException extends UploadException {
    /** Holds multiple upload errors as exceptions. */
    private final List<UploadException> errors;

    /**
     * Creates a reference data loader exception.
     *
     * @param message A message for the user.
     */
    public UploadMultiException(String message, List<UploadException> errors) {
        super(message);
        this.errors = errors;
    }

    /** Provides a list of the multiple errors. */
    public List<UploadException> getErrors() {
        return errors;
    }
}
