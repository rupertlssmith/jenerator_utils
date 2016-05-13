/*
 * Copyright The Sett Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thesett.util.security.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.util.ThreadState;

/**
 * ShiroUtils provides a way of setting or removing a subject on the current thread.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Manage a subject on the current thread. </td></rt>
 * <rt><td> Provide a more complete tear down for tests. </td></rt>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class ShiroUtils
{
    /** The Shiro state against the current thread. */
    private static ThreadState subjectThreadState;

    /**
     * Sets up a security manager and subject on the current thread.
     *
     * @param subject The subject to attach to the current thread.
     */
    public static void setSubject(Subject subject)
    {
        clearSubject();
        subjectThreadState = createThreadState(subject);
        subjectThreadState.bind();
    }

    /**
     * Provides the subject on the current thread, if any.
     *
     * @return The subject on the current thread, or <tt>null</tt> if there is none.
     */
    public static Subject getSubject()
    {
        return SecurityUtils.getSubject();
    }

    /** Removes the subject from the current thread. */
    public static void clearSubject()
    {
        if (subjectThreadState != null)
        {
            subjectThreadState.clear();
            subjectThreadState = null;
        }
    }

    /**
     * Performs a more complete tear down of shiro, removing the subject from the current thread and closing the
     * security manager.
     */
    public static void tearDownShiro()
    {
        clearSubject();

        try
        {
            SecurityManager securityManager = getSecurityManager();
            LifecycleUtils.destroy(securityManager);
        }
        catch (UnavailableSecurityManagerException e)
        {
            // This can be ignored as it only happens when there is no security manager.
        }

        setSecurityManager(null);
    }

    protected static void setSecurityManager(SecurityManager securityManager)
    {
        SecurityUtils.setSecurityManager(securityManager);
    }

    protected static SecurityManager getSecurityManager()
    {
        return SecurityUtils.getSecurityManager();
    }

    protected static ThreadState createThreadState(Subject subject)
    {
        return new SubjectThreadState(subject);
    }
}
