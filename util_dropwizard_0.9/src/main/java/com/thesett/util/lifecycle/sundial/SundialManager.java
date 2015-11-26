package com.thesett.util.lifecycle.sundial;

import java.util.Properties;
import java.util.logging.Logger;

import com.xeiam.sundial.SundialJobScheduler;

import io.dropwizard.lifecycle.Managed;

import org.quartz.Scheduler;
import org.quartz.exceptions.SchedulerException;

/**
 * SundialManager implements a DropWizard managed lifecycle to control a Quartz scheduler.
 *
 * <p/>Note that when {@link #stop()} is called, the Quartz scheduler is not shut-down, but instead put into standby
 * mode. This is because Quartz implements a broken lifecycle, that once shut-down cannot be restarted.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Start the Quartz scheduler running. </td></tr>
 * <tr><td> Stop the Quartz scheduler running. </td></tr>
 * </table></pre>
 */
public class SundialManager implements Managed {
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(SundialManager.class.getName());

    private static final String DEFAULT_START_ON_LOAD = "true";
    private static final String DEFAULT_PERFORM_SHUTDOWN = "true";
    private static final String DEFAULT_LOCK_ON_LOAD = "false";
    private static final String DEFAULT_THREAD_POOL_SIZE = "10";
    private static final String DEFAULT_START_DELAY = "0";

    /**
     * Holds the configuration properties. See {@link #SundialManager(java.util.Properties)} for a description of the
     * available configuration properties.
     */
    private final Properties configProperties;

    /**
     * Creates the sundial lifecycle. The following properties may be set to configure sundial:
     *
     * <pre><p/><table><caption>Sundial Properties</caption>
     * <tr><td> performShutdown  </td><td> 'true' iff the scheduler should be stopped on the lifecycle stop. </td></tr>
     * <tr><td> startOnLoad      </td><td> 'true' iff the scheduler should be started on the lifecycle start. </td></tr>
     * <tr><td> startDelay       </td><td> A start delay in seconds. </td></tr>
     * <tr><td> threadPoolSize   </td><td> Size of the scheduler thread pool. </td></tr>
     * <tr><td> globalLockOnLoad </td><td> 'true' iff the scheduler should start out locked. </td></tr>
     * </table></pre>
     *
     * @param quartzProperties Configuration properties.
     */
    public SundialManager(Properties quartzProperties) {
        this.configProperties = quartzProperties;
    }

    /** {@inheritDoc} */
    public void start() throws SchedulerException {
        LOG.info("Sundial Initializer Manager loaded, initializing Scheduler...");

        boolean startOnLoad =
            Boolean.valueOf(configProperties.getProperty("startOnLoad", DEFAULT_START_ON_LOAD)).booleanValue();
        int startDelay = Integer.parseInt(configProperties.getProperty("startDelay", DEFAULT_START_DELAY));
        int threadPoolSize = Integer.parseInt(configProperties.getProperty("threadPoolSize", DEFAULT_THREAD_POOL_SIZE));
        boolean globalLockOnLoad =
            Boolean.valueOf(configProperties.getProperty("globalLockOnLoad", DEFAULT_LOCK_ON_LOAD)).booleanValue();

        SundialJobScheduler.createScheduler(threadPoolSize);

        if (startOnLoad) {
            if (startDelay <= 0) {
                // Start now
                SundialJobScheduler.getScheduler().start();
                LOG.info("Sundial scheduler has been started.");
            } else {
                // Start delayed
                SundialJobScheduler.getScheduler().startDelayed(startDelay);
                LOG.info("Sundial scheduler will start in " + startDelay + " seconds.");
            }
        }

        if (globalLockOnLoad) {
            SundialJobScheduler.lockScheduler();
            LOG.info("Sundial scheduler has been locked.");
        }

        LOG.info("Scheduler has been started.");
    }

    /** {@inheritDoc} */
    public void stop() throws SchedulerException {
        boolean performShutdown =
            Boolean.valueOf(configProperties.getProperty("performShutdown", DEFAULT_PERFORM_SHUTDOWN)).booleanValue();

        if (!performShutdown) {
            return;
        }

        if (SundialJobScheduler.getScheduler() != null) {
            SundialJobScheduler.getScheduler().standby();
        }

        LOG.info("Sundial Scheduler shutdown successfully.");
    }

    public Scheduler getQuartzScheduler() {
        return SundialJobScheduler.getScheduler();
    }
}
