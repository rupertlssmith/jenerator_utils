package com.thesett.util.uploader;

/**
 * UploadException is used to report errors encountered whilst loading data from CSV files.
 */
public class UploadException extends Exception {
    /**
     * Creates a reference data loader exception.
     *
     * @param message A message for the user.
     * @param cause   Any underlying cause.
     */
    public UploadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a reference data loader exception.
     *
     * @param message A message for the user.
     */
    public UploadException(String message) {
        super(message);
    }
}
