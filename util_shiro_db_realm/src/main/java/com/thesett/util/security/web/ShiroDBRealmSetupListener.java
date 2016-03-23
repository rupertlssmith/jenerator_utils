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
package com.thesett.util.security.web;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.thesett.util.security.dao.UserSecurityDAO;
import com.thesett.util.security.realm.ShiroDBRealm;

import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.util.WebUtils;

/**
 * ShiroDBRealmSetupListener is an initializer for {@link ShiroDBRealm}s, that can be attached to the lifecycle of a
 * servlet container. When the servlet context is created, it will initialize any {@link ShiroDBRealm}s that have been
 * set up in Shiro, and will destroy them when the servlet context ends.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Initialize Shiro DB realms on Web container start. </td></tr>
 * <tr><td> Destroy Shiro DB realms on Web container stop. </td></tr>
 * </table></pre>
 */
public class ShiroDBRealmSetupListener implements ServletContextListener
{
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(ShiroDBRealmSetupListener.class.getName());

    /** <tt>true</tt> iff only the first configured realm should be initialized and destroyed. */
    private final boolean initFirstRealmOnly;

    /** The DAO to interact with user data through. */
    private final UserSecurityDAO userSecurityDAO;

    /** Creates a Shiro DB relam Web lifecycle controller. */
    public ShiroDBRealmSetupListener(UserSecurityDAO userSecurityDAO)
    {
        this(userSecurityDAO, false);
    }

    /**
     * Creates a Shiro DB realm Web lifecycle controller.
     *
     * @param initFirstRealmOnly <tt>true</tt> iff only the first configured realm should be initialized and destroyed.
     */
    public ShiroDBRealmSetupListener(UserSecurityDAO userSecurityDAO, boolean initFirstRealmOnly)
    {
        this.initFirstRealmOnly = initFirstRealmOnly;
        this.userSecurityDAO = userSecurityDAO;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Initializes the Shiro DB realms.
     */
    public void contextInitialized(ServletContextEvent sce)
    {
        LOG.fine("public void contextInitialized(ServletContextEvent sce): called");

        RealmSecurityManager rsm = getRealmSecurityManager(sce);

        for (Realm r : rsm.getRealms())
        {
            if (r instanceof ShiroDBRealm)
            {
                initializeRealm((ShiroDBRealm) r);

                if (initFirstRealmOnly)
                {
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Destroys the Shiro DB realms.
     */
    public void contextDestroyed(ServletContextEvent sce)
    {
        LOG.fine("public void contextDestroyed(ServletContextEvent sce): called");

        RealmSecurityManager rsm = getRealmSecurityManager(sce);

        for (Realm r : rsm.getRealms())
        {
            if (r instanceof ShiroDBRealm)
            {
                destroyRealm((ShiroDBRealm) r);

                if (initFirstRealmOnly)
                {
                    break;
                }
            }
        }
    }

    /**
     * Obtains the Shiro RealmSecurityManager for this Servlet container.
     *
     * @param  sce A context event notifying what this Servlet container is.
     *
     * @return The Shiro RealmSecurityManager for this Servlet container.
     */
    protected RealmSecurityManager getRealmSecurityManager(ServletContextEvent sce)
    {
        LOG.fine("protected RealmSecurityManager getRealmSecurityManager(ServletContextEvent sce): called");

        WebEnvironment we = WebUtils.getWebEnvironment(sce.getServletContext());

        return (RealmSecurityManager) we.getSecurityManager();
    }

    /**
     * Sets up the specified Shiro DB realm.
     *
     * @param realm The Shiro DB realm.
     */
    protected void initializeRealm(ShiroDBRealm realm)
    {
        LOG.fine("protected void initializeRealm(ShiroDBRealm realm): called");

        realm.intialize(userSecurityDAO);
    }

    /**
     * l Destroys the specified Shiro DB realm.
     *
     * @param realm The Shiro DB realm.
     */
    protected void destroyRealm(ShiroDBRealm realm)
    {
        LOG.fine("protected void destroyRealm(ShiroDBRealm realm): called");

        realm.close();
    }
}
