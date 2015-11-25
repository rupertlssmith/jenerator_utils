package com.thesett.util.error.toplevelhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SLF4JStrictTopLevelErrorHandler extends BaseTopLevelErrorHandler {
    /** Used for logging the errors. */
    private static final Logger LOG = LoggerFactory.getLogger(SLF4JStrictTopLevelErrorHandler.class);

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
        System.exit(-1);
    }

    /** {@inheritDoc} */
    protected void handleChecked(Exception e) {
        LOG.error(ERROR_CODE_UNKNOWN_BUG + " " + e.getMessage(), e);
    }
}
