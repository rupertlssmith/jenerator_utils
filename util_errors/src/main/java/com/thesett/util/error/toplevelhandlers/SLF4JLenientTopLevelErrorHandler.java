package com.thesett.util.error.toplevelhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thesett.util.error.SystemError;

/**
 * WebMethodTopLevelErrorHandler provides consistent top-level error handling, for unhandled exceptions. It is used to
 * wrap HTTP request handlers.
 *
 * <p/>This is the only place in the application that 'LOG.error' should be called.
 *
 * <p/>This error handler is a less strict one; unhandled runtime errors are simply logged.
 */
public class SLF4JLenientTopLevelErrorHandler extends BaseTopLevelErrorHandler {
    /** Used for logging the errors. */
    private static final Logger LOG = LoggerFactory.getLogger(SLF4JLenientTopLevelErrorHandler.class);

    /** {@inheritDoc} */
    protected void handleError(Error e) {
        e.printStackTrace();
        System.exit(-1);
    }

    /** {@inheritDoc} */
    protected void handleSystem(SystemError e, Throwable t) {
        LOG.error(e.getErrorCode() + " " + e.getMessage(), t);
    }

    /** {@inheritDoc} */
    protected void handleRuntime(RuntimeException e) {
        LOG.error(ERROR_CODE_UNKNOWN_BUG + " " + e.getMessage(), e);
    }

    /** {@inheritDoc} */
    protected void handleChecked(Exception e) {
        LOG.error(ERROR_CODE_UNKNOWN_BUG + " " + e.getMessage(), e);
    }
}
