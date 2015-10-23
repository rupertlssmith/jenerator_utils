package com.thesett.util.error.toplevelhandlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.thesett.util.error.SystemError;

/**
 * JULStrictTopLevelErrorHandler provides consistent top-level error handling, for unhandled exceptions. It makes use of
 * java.util.logging to log errors.
 *
 * <p/>This is the only place in the application that 'LOG.error' should be called.
 *
 * <p/>This error handler is a strict one; any unhandled runtime error results in System.exit(-1) being called. This may
 * be appropriate in some situations, and is certainly a good thing to do when developing highly resilient software.
 */
public class JULStrictTopLevelErrorHandler extends BaseTopLevelErrorHandler {
    /** Used for logging the errors. */
    private static final Logger LOG = Logger.getLogger(JULStrictTopLevelErrorHandler.class.getName());

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
        System.exit(-1);
    }

    /** {@inheritDoc} */
    protected void handleChecked(Exception e) {
        LOG.log(Level.SEVERE, ERROR_CODE_UNKNOWN_BUG + " " + e.getMessage(), e);
    }
}
