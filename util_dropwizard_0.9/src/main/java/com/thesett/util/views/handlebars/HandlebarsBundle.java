package com.thesett.util.views.handlebars;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * HandlebarsBundle provides a configuration bundle for setting up the handlebars renderer.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Configure the handlebars rendered. </td></tr>
 * </table></pre>
 */
public abstract class HandlebarsBundle implements ConfiguredBundle<HandlebarsBundleConfig> {
    /** Holds additional template paths. */
    private final Collection<String> templatePaths = new LinkedHashSet<>();
    private final Map<String, String> overrides = new HashMap<>();

    public static <H> void registerHelperMissing(Helper<H> helper) {
        HandlebarsViewRenderer.handlebars.registerHelperMissing(helper);
    }

    public static <H> void registerHelper(String name, Helper<H> helper) {
        HandlebarsViewRenderer.handlebars.registerHelper(name, helper);
    }

    public static void setPrettyPrint(boolean prettyPrint) {
        HandlebarsViewRenderer.handlebars.setPrettyPrint(prettyPrint);
    }

    public static void setInfiniteLoops(boolean infiniteLoops) {
        HandlebarsViewRenderer.handlebars.setInfiniteLoops(infiniteLoops);
    }

    public void addTemplatePath(String path) {
        templatePaths.add(path);
    }

    public final void initialize(Bootstrap<?> bootstrap) { /* empty */
    }

    public final void run(HandlebarsBundleConfig configuration, Environment environment) {
        configureHandlebars(configuration);

        // Flatten any overrides into a single map.
        HandlebarsConfig handlebarsConfig = configuration.getHandlebars();

        List<Map<String, String>> configOverrides = handlebarsConfig.getOverrides();

        if (configOverrides != null) {
            for (Map<String, String> override : configOverrides) {
                overrides.putAll(override);
            }
        }

        // Set up templates to be loaded from the classpath or filesystem for overrides.
        if (!templatePaths.isEmpty()) {
            List<TemplateLoader> loaders = new LinkedList<>();

            for (String path : templatePaths) {
                // Check if there is a filesystem override.
                if (overrides.containsKey(path)) {
                    loaders.add(new FileTemplateLoader(overrides.get(path)));
                } else {
                    loaders.add(new ClassPathTemplateLoader(path));
                }
            }

            TemplateLoader[] templateLoaderArray = loaders.toArray(new TemplateLoader[loaders.size()]);
            HandlebarsViewRenderer.handlebars = HandlebarsViewRenderer.handlebars.with(templateLoaderArray);
        }

        HandlebarsViewRenderer.useCache = handlebarsConfig.isCacheTemplates();
        StringHelpers.register(HandlebarsViewRenderer.handlebars);
    }

    protected abstract void configureHandlebars(HandlebarsBundleConfig configuration);
}
