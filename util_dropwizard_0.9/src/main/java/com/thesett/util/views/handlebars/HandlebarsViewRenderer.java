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
package com.thesett.util.views.handlebars;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.GuavaTemplateCache;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.dropwizard.views.View;
import io.dropwizard.views.ViewRenderer;

/**
 * Renders handlebars templates from data views.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Indicate which templates are renderable with handlebars. </td></tr>
 * <tr><td> Render handlebars templates from views. </td></tr>
 * </table></pre>
 */
public class HandlebarsViewRenderer implements ViewRenderer
{
    private static final Logger LOG = Logger.getLogger(HandlebarsViewRenderer.class.getName());

    /** Caches template files. */
    private static final Cache<TemplateSource, Template> templateCache = CacheBuilder.newBuilder().build();

    /** Caches compiled templates. */
    static final LoadingCache<String, Template> compilationCache =
        CacheBuilder.newBuilder().build(new CacheLoader<String, Template>()
            {
                public Template load(String srcUrl) throws Exception
                {
                    return loadTemplate(srcUrl);
                }
            });

    /** The handlebars compiler. */
    protected static Handlebars handlebars = new Handlebars().with(new GuavaTemplateCache(templateCache));

    /** The caching switch. */
    protected static boolean useCache = true;

    /** {@inheritDoc} */
    public void configure(Map<String, String> stringStringMap)
    {
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Templates ending with ".hbs" are rendered by this.
     */
    public boolean isRenderable(View view)
    {
        return view.getTemplateName().endsWith(".hbs");
    }

    /** {@inheritDoc} */
    public void render(View view, Locale locale, OutputStream output) throws IOException, WebApplicationException
    {
        try(Writer writer = new OutputStreamWriter(output, view.getCharset().or(Charsets.UTF_8)))
        {
            if (view instanceof Layout)
            {
                Writer buffer = new StringWriter();

                Layout layout = (Layout) view;
                Template layoutTemplate = loadTemplate(layout.getLayoutTemplateName());
                Template viewTemplate = loadTemplate(layout.getTemplateName());

                viewTemplate.apply(view, buffer);

                layout.setBody(buffer.toString());
                layoutTemplate.apply(layout, writer);
            }
            else
            {
                loadTemplate(view.getTemplateName()).apply(view, writer);
            }
        }
        catch (IOException | WebApplicationException e)
        {
            // The exception handling in dropwizard view renderer is so bad. So rethrowing this as an illegal state
            // - system exception.
            throw new IllegalStateException(e);
        }
    }

    /** {@inheritDoc} */
    public String getSuffix()
    {
        return "";
    }

    /**
     * Loads a handlebars template and compiles it. This is used by the cache, or can be used directly to force
     * re-compilation of templates each time.
     *
     * @param  srcUrl The template source.
     *
     * @return A compiles handlebars template.
     *
     * @throws IOException If some IO error occurs whilst loading the template.
     */
    private static Template loadTemplate(String srcUrl) throws IOException
    {
        return handlebars.compile(srcUrl.replaceAll(".hbs$", ""));
    }

    private Template getTemplate(String srcUrl) throws IOException
    {
        if (useCache)
        {
            try
            {
                return compilationCache.get(srcUrl);
            }
            catch (ExecutionException e)
            {
                throw new IllegalStateException(e);
            }
        }
        else
        {
            return loadTemplate(srcUrl);
        }
    }
}
