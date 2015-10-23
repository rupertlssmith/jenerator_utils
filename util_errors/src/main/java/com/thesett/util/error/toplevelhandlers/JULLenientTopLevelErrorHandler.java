package com.thesett.util.error.toplevelhandlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.thesett.util.error.SystemError;

/**
 * WebMethodTopLevelErrorHandler provides consistent top-level error handling, for unhandled exceptions. It is used to
 * wrap HTTP request handlers.
 *
 * <p/>This is the only place in the application that 'LOG.error' should be called.
 *
 * <p/>This error handler is a less strict one; unhandled runtime errors are simply logged.
 */
public class JULLenientTopLevelErrorHandler extends BaseTopLevelErrorHandler {
    /** Used for logging the errors. */
    private static final Logger LOG = Logger.getLogger(JULLenientTopLevelErrorHandler.class.getName());

    /** {@inheritDoc} */
    protected void handleError(Error e) {
        e.printStackTrace();
        System.exit(-1);
    }

    /** {@inheritDoc} */
    protected void handleSystem(SystemError e, Throwable t) {
        LOG.log(Level.SEVERE, e.getErrorCode() + " " + e.getMessage(), t);
    }

    /** {@inheritDoc} */
    protected void handleRuntime(RuntimeException e) {
        LOG.log(Level.SEVERE, ERROR_CODE_UNKNOWN_BUG + " " + e.getMessage(), e);
    }

    /** {@inheritDoc} */
    protected void handleChecked(Exception e) {
        LOG.log(Level.SEVERE, ERROR_CODE_UNKNOWN_BUG + " " + e.getMessage(), e);
    }
}
