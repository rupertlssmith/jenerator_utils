package com.thesett.jenerator;

import com.thesett.aima.state.Type;
import com.thesett.catalogue.generator.*;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.EntityTypeVisitor;
import com.thesett.catalogue.model.impl.Relationship;
import com.thesett.common.util.FileUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.LinkedList;
import java.util.List;

/**
 * DropWizardTopGenerator generates an application entry point for running the generated API under DropWizard.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td> Generate an application entry point under DropWizard. </td></tr>
 * </table></pre>
 */
public class SpringBootTopGenerator extends BaseGenerator implements EntityTypeVisitor {
    /** Defines the name of the template group for creating local service interfaces. */
    private static final String DROPWIZARD_APPLICATION_TEMPLATES_GROUP = "SpringBootJerseyComponent";

    /** Defines the name of the template to create the opening section of output files. */
    protected static final String FILE_OPEN_TEMPLATE = "file_open";

    /** Defines the name of the template to invoke on all entities. */
    protected static final String FOR_BEANS_TEMPLATE = "for_beans";

    /** Defines the name of the template to create the closing section of output files. */
    protected static final String FILE_CLOSE_TEMPLATE = "file_close";

    /** Holds the name of the class to output the service factory interface. */
    private static final String APPLICATION_CLASS_NAME = "Main";

    /** Holds the string template group to generate the application top from. */
    private final STGroup applicationTemplates;

    /** Holds the name of the file to output the application top to. */
    private String applicationFileName = "JerseyComponent";

    /** Output handler used to build up the application methods in. */
    private final BufferingTemplateHandler applicationHandler = new BufferingTemplateHandler();

    /** Holds a list of entities to generate for. */
    private final List<Type> typesToGenerate = new LinkedList<>();

    /** Holds a file output handler that overwrites files. */
    protected RenderTemplateHandler fileOutputRenderTemplateHandler =
        new FileOutputRenderTemplateHandler(false, true);

    /** Holds a file output handler that appends to files. */
    protected RenderTemplateHandler fileOutputRenderTemplateHandlerAppend =
        new FileOutputRenderTemplateHandler(true, true);

    private String modelPackage;
    private String clientOutputDir;
    private String localOutputDir;
    private String clientPackage;
    private String localPackage;

    /**
     * Creates a Java generator to output to the specified directory root.
     *
     * @param templateDir An alternative directory to load templates from, may be <tt>null</tt> to use defaults.
     */
    public SpringBootTopGenerator(String templateDir) {
        super(templateDir);

        applicationTemplates = new STGroupFile(templateGroupToFileName(DROPWIZARD_APPLICATION_TEMPLATES_GROUP));
        applicationTemplates.registerRenderer(String.class, new CamelCaseRenderer());
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Ensures that the opening section of the hibernate config file is created.
     */
    public void preApply(Catalogue catalogue) {
        // Output the configuration sections built up in the output buffers in the correct order to the configuration file.
        applicationFileName = nameToJavaFileName(outputDir, outputPackage, "", APPLICATION_CLASS_NAME, "");

        generateTemplateOpenToFile(applicationFileName, applicationTemplates, outputPackage, FILE_OPEN_TEMPLATE, false);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Ensures that the closing section of the hibernate config file is created.
     */
    public void postApply(Catalogue catalogue) {
        RenderTemplateHandler[] handlers = new RenderTemplateHandler[] { applicationHandler };
        STGroup[] templates = new STGroup[] { applicationTemplates };
        String[] names = new String[] { nameToJavaFileName(outputDir, "", "", "ServiceFactory") };

        generate(model, typesToGenerate, templates, names, handlers, FOR_BEANS_TEMPLATE);

        flushHandlerToFile(applicationFileName, applicationHandler, true);

        generateTemplateCloseToFile(applicationFileName, applicationTemplates, FILE_CLOSE_TEMPLATE);
    }

    /**
     * Generates a bean class for a component type in the catalogue model.
     *
     * @param type The component type to create a bean for.
     */
    public void visit(EntityType type) {
        Type decoratedType = (ComponentTypeDecorator) TypeDecoratorFactory.decorateType(type);
        typesToGenerate.add(decoratedType);

        for (Relationship relationship : type.getRelationships().values()) {
            System.out.println("=============================");
            System.out.println(relationship.getName());
            System.out.println(relationship.getFrom());
            System.out.println(relationship.getTo());
            System.out.println(relationship.getTarget());
            System.out.println(relationship.getTargetFieldName());
            System.out.println(relationship.isOwner());
        }
    }

    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }

    public void setClientOutputDir(String clientOutputDir) {
        this.clientOutputDir = clientOutputDir;
    }

    public void setLocalOutputDir(String localOutputDir) {
        this.localOutputDir = localOutputDir;
    }

    public void setClientPackage(String clientPackage) {
        this.clientPackage = clientPackage;
    }

    public void setLocalPackage(String localPackage) {
        this.localPackage = localPackage;
    }

    /**
     * Applies a sequence of templates to a catalogue model, type from the catalogue, and set of fields from the type,
     * and optionally an extra set of fields. These parameters are the parameters that the string template function is
     * expecting and they are passed to such a template, and the results written to the specified handler.
     *
     * @param model        The catalogue model.
     * @param types        The types to generate for.
     * @param templates    The sequence of templates to apply.
     * @param outputName   A sequence of named resources, such as files, to write the results to.
     * @param handler      A sequence of output handlers, to apply to the results.
     * @param templateName The name of the template to apply.
     */
    protected void generate(Catalogue model, List<Type> types, STGroup[] templates, String[] outputName,
        RenderTemplateHandler[] handler, String templateName) {
        for (int i = 0; i < templates.length; i++) {
            // Instantiate the template to generate from.
            ST stringTemplate = templates[i].getInstanceOf(templateName);

            stringTemplate.add("decorators", types);
            stringTemplate.add("catalogue", model);
            stringTemplate.add("package", outputPackage);

            handler[i].render(stringTemplate, outputName[i]);
        }
    }

    private void flushHandlerToFile(String fileName, BufferingTemplateHandler templateHandler, boolean append) {
        FileUtils.writeObjectToFile(fileName, templateHandler, append);
        templateHandler.clear();
    }

    private void generateTemplateOpenToFile(String fileName, STGroup templateGroup, String packageName,
        String fileOpenTemplate, boolean append) {
        // Instantiate the template to generate from.
        ST stringTemplate = templateGroup.getInstanceOf(fileOpenTemplate);
        stringTemplate.add("package", packageName);
        stringTemplate.add("catalogue", model);

        fileOutputRenderTemplateHandler.render(stringTemplate, fileName);
    }

    private void generateTemplateCloseToFile(String fileName, STGroup templateGroup, String fileCloseTemplate) {
        // Instantiate the template to generate from.
        ST stringTemplate = templateGroup.getInstanceOf(fileCloseTemplate);

        fileOutputRenderTemplateHandlerAppend.render(stringTemplate, fileName);
    }
}
