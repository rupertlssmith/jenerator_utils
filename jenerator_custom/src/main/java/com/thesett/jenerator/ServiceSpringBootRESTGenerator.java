package com.thesett.jenerator;

import com.thesett.aima.state.ComponentType;
import com.thesett.aima.state.Type;
import com.thesett.catalogue.generator.BaseGenerator;
import com.thesett.catalogue.generator.CamelCaseRenderer;
import com.thesett.catalogue.generator.ComponentTypeDecorator;
import com.thesett.catalogue.generator.TypeDecoratorFactory;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.EntityTypeVisitor;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.Map;

/**
 * ServiceSpringBootRESTGenerator codegens service implementations for Spring Boot.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td>  </td></tr>
 * </table></pre>
 */
public class ServiceSpringBootRESTGenerator extends BaseGenerator implements EntityTypeVisitor {
    /** Defines the name of the template group for creating REST services. */
    private static final String REST_IMPL_TEMPLATES_GROUP = "ServiceSpringBootRESTImpl";

    /** Holds the string template group to generate REST services. */
    private final STGroup restImplTemplates;

    /** Holds a file output handler that overwrites files. */
    protected RenderTemplateHandler fileOutputRenderTemplateHandler =
        new FileOutputRenderTemplateHandler(false, false);

    private String modelPackage;

    /**
     * Creates a Java generator to output to the specified directory root.
     *
     * @param templateDir An alternative directory to load templates from, may be <tt>null</tt> to use defaults.
     */
    public ServiceSpringBootRESTGenerator(String templateDir) {
        super(templateDir);

        restImplTemplates = new STGroupFile(templateGroupToFileName(REST_IMPL_TEMPLATES_GROUP));
        restImplTemplates.registerRenderer(String.class, new CamelCaseRenderer());
    }

    /**
     * Generates a bean class for a component type in the catalogue model.
     *
     * @param type The component type to create a bean for.
     */
    public void visit(EntityType type) {
        ComponentType decoratedType = (ComponentTypeDecorator) TypeDecoratorFactory.decorateType(type);

        STGroup[] templates;
        String[] names;
        Map<String, Type> fields = decoratedType.getAllPropertyTypes();
        Map<String, Type> extraFields = null;
        RenderTemplateHandler[] handlers =
            new RenderTemplateHandler[] { fileOutputRenderTemplateHandler, fileOutputRenderTemplateHandler };

        templates = new STGroup[] { restImplTemplates };
        names = new String[] { nameToJavaFileName(outputDir, "", type.getName(), "Resource"), };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }

    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }
}
