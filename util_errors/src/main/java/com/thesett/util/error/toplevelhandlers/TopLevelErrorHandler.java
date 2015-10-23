package com.thesett.util.error.toplevelhandlers;

/**
 * A TopLevelErrorHandler is used to provide consistent top-level error handling for unhandled exceptions. It is often
 * used to wrap the top level of a thread, so that any unhandled exceptions causing the termination of a thread are
 * handled in a consistent manner.
 *
 * <p/>An implementation of this is usually the only place in an application that 'LOG.error' should be called.
 *
 * <p/>Generally, unhandled RuntimeExceptions should result in the termination of an application, as an unknown, and
 * potentially unsafe state has been reached. Log, swallow and continue is an inherently unsafe practice. Often
 * terminating the application will be seen as too strict, especially where an the effects of an error are isolated to a
 * single thread, in which case the exception may simply be logged.
 *
 * <p/>Errors may include out of memory conditions, so the use of a logger is not advised, in case the logger
 * implementation itself needs to allocate memory but cannot. Generally, it is a good idea to print Error stack traces
 * to standard error, and to log other Runtimes.
 */
public interface TopLevelErrorHandler {
    /**
     * Handles a throwable that has fallen through to the top-level of a unit of work.
     *
     * @param t The throwable to provide top-level error handling for.
     */
    void handleThrowable(Throwable t);
}
