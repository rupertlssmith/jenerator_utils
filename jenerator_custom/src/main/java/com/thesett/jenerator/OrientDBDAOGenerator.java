package com.thesett.jenerator;

import java.util.Map;

import com.thesett.aima.state.ComponentType;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.thesett.aima.state.Type;
import com.thesett.catalogue.generator.BaseGenerator;
import com.thesett.catalogue.generator.CamelCaseRenderer;
import com.thesett.catalogue.generator.ComponentTypeDecorator;
import com.thesett.catalogue.generator.TypeDecoratorFactory;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.EntityTypeVisitor;

/**
 * OrientDBDAOGenerator codegens DAOs on top of OrientDB for all entities in the data model.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td>  </td></tr>
 * </table></pre>
 */
public class OrientDBDAOGenerator extends BaseGenerator implements EntityTypeVisitor {
    /** Defines the name of the template group for creating DAO implementations. */
    private static final String DAO_IMPL_TEMPLATES_GROUP = "HibernateDAO";

    /** Holds the string template group to generate DAO implementations from. */
    private final STGroup daoImplTemplates;

    /** Holds a file output handler that overwrites files. */
    protected RenderTemplateHandler fileOutputProcessedTemplateHandler =
        new FileOutputRenderTemplateHandler(false, true);

    private String modelPackage;

    /**
     * Creates a Java generator to output to the specified directory root.
     *
     * @param templateDir An alternative directory to load templates from, may be <tt>null</tt> to use defaults.
     */
    public OrientDBDAOGenerator(String templateDir) {
        super(templateDir);

        daoImplTemplates = new STGroupFile(templateGroupToFileName(DAO_IMPL_TEMPLATES_GROUP));
        daoImplTemplates.registerRenderer(String.class, new CamelCaseRenderer());
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
        RenderTemplateHandler[] handlers = new RenderTemplateHandler[] { fileOutputProcessedTemplateHandler };

        templates = new STGroup[] { daoImplTemplates };
        names = new String[] { nameToJavaFileName(outputDir, "", type.getName(), "DAOImpl") };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
    }

    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }
}
