package com.thesett.util.error.toplevelhandlers;

import com.thesett.util.error.SystemError;

/**
 * Provides standard unhandled exception processing. Exceptions are classified as errors, system exceptions, unhandled
 * runtimes (bugs), and unhandled checked exceptions.
 */
public abstract class BaseTopLevelErrorHandler implements TopLevelErrorHandler {
    /** The error code to use for unhandled runtime exceptions. */
    public static final String ERROR_CODE_UNKNOWN_BUG = "UNKN_9999";

    /** {@inheritDoc} */
    public void handleThrowable(Throwable t) {
        if (t instanceof Error) {
            handleError((Error) t);
        } else if (t instanceof SystemError) {
            handleSystem((SystemError) t, t);
        } else if (t instanceof RuntimeException) {
            handleRuntime((RuntimeException) t);
        } else {
            handleChecked((Exception) t);
        }
    }

    /**
     * Handles errors. Generally speaking the stack trace should be printed to stdout and System.exit invoked.
     *
     * @param e The error.
     */
    protected abstract void handleError(Error e);

    /**
     * Handles system exceptions, that is, those that provide error codes.
     *
     * @param e The system error.
     * @param t The system error as a throwable.
     */
    protected abstract void handleSystem(SystemError e, Throwable t);

    /**
     * Handles runtime exceptions. Generally speaking, these should be logged as errors. If operating strictly,
     * System.exit may be invoked.
     *
     * @param e The unhanded runtime exception.
     */
    protected abstract void handleRuntime(RuntimeException e);

    /**
     * Handles unhandled checked exception. Generally speaking, these should be logged as errors.
     *
     * @param e The unhanded checked exception.
     */
    protected abstract void handleChecked(Exception e);
}
