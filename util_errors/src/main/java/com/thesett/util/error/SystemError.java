package com.thesett.util.error;

/**
 * System errors could be runtimes or checked, it is debatable which is best. For this reason, it is worth defining an
 * interface that both types can implement, that provides the error code and message.
 *
 * <p/>A system exception is strictly for the attention of the sys admin, never the end user. System exceptions should
 * only appear in application or framework code of systems. When writing library code, stick to normal {@link Exception}
 * s to inform the user of your API about expected error conditions.
 *
 * <p/>A system exception represents a condition that prevents the system from continuing processing, and that requires
 * the attention of a sys admin to resolve. For example, cannot connect to the database, file system is full, and so on.
 *
 * <p/>It is worth defining error codes on system exceptions in a consistent manner. A tool like grep can be used to
 * extract all the error codes, to ensure they are all documented, and that each code is used once and only once in the
 * code. Having a simple regular pattern such as "SYS_00001" for the error codes can make the job of attaching a utility
 * to scan the logs simpler.
 */
public interface SystemError {
    /**
     * Provides the error code.
     *
     * @return The error code.
     */
    String getErrorCode();

    /**
     * Provides the error message.
     *
     * @return The error message.
     */
    String getMessage();
}
