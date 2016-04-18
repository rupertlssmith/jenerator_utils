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

import java.security.PublicKey;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.thesett.util.security.realm.ShiroJWTRealm;

import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.util.WebUtils;

/**
 * ShiroJWTRealmSetupListener is an initializer for {@link ShiroJWTRealm}s, that can be attached to the lifecycle of a
 * servlet container. When the servlet context is created, it will initialize any {@link ShiroJWTRealm}s that have been
 * set up in Shiro, and will destroy them when the servlet context ends.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Initialize Shiro DB realms on Web container start. </td></tr>
 * <tr><td> Destroy Shiro DB realms on Web container stop. </td></tr>
 * </table></pre>
 */
public class ShiroJWTRealmSetupListener implements ServletContextListener
{
    /** Used for debugging purposes. */
    private static final Logger LOG = Logger.getLogger(ShiroJWTRealmSetupListener.class.getName());

    /** <tt>true</tt> iff only the first configured realm should be initialized and destroyed. */
    private final boolean initFirstRealmOnly;

    /** The public key for checking access tokens against. */
    private final PublicKey publicKey;

    /** Creates a Shiro DB relam Web lifecycle controller. */
    public ShiroJWTRealmSetupListener(PublicKey publicKey)
    {
        this(publicKey, false);
    }

    /**
     * Creates a Shiro DB realm Web lifecycle controller.
     *
     * @param initFirstRealmOnly <tt>true</tt> iff only the first configured realm should be initialized and destroyed.
     */
    public ShiroJWTRealmSetupListener(PublicKey publicKey, boolean initFirstRealmOnly)
    {
        this.initFirstRealmOnly = initFirstRealmOnly;
        this.publicKey = publicKey;
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
            if (r instanceof ShiroJWTRealm)
            {
                initializeRealm((ShiroJWTRealm) r);

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
            if (r instanceof ShiroJWTRealm)
            {
                destroyRealm((ShiroJWTRealm) r);

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
    protected void initializeRealm(ShiroJWTRealm realm)
    {
        LOG.fine("protected void initializeRealm(ShiroJWTRealm realm): called");

        realm.intialize(publicKey);
    }

    /**
     * l Destroys the specified Shiro DB realm.
     *
     * @param realm The Shiro DB realm.
     */
    protected void destroyRealm(ShiroJWTRealm realm)
    {
        LOG.fine("protected void destroyRealm(ShiroJWTRealm realm): called");

        realm.close();
    }
}
