package com.thesett.jenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.thesett.aima.state.ComponentType;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.thesett.aima.state.Type;
import com.thesett.catalogue.generator.BaseGenerator;
import com.thesett.catalogue.generator.BufferingTemplateHandler;
import com.thesett.catalogue.generator.CamelCaseRenderer;
import com.thesett.catalogue.generator.ComponentTypeDecorator;
import com.thesett.catalogue.generator.TypeDecoratorFactory;
import com.thesett.catalogue.model.Catalogue;
import com.thesett.catalogue.model.EntityType;
import com.thesett.catalogue.model.EntityTypeVisitor;
import com.thesett.common.util.FileUtils;

/**
 * ServiceIfaceGenerator codegens service interfaces to access the data model.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td>  </td></tr>
 * </table></pre>
 */
public class ServiceFactoryGenerator extends BaseGenerator implements EntityTypeVisitor {
    /** Defines the name of the template group for creating service factory interfaces. */
    private static final String SERVICE_FACTORY_INTERFACE_TEMPLATES_GROUP = "ServiceFactory";

    /** Defines the name of the template group for creating client service factories. */
    private static final String SERVICE_FACTORY_CLIENT_TEMPLATES_GROUP = "ServiceFactoryClient";

    /** Defines the name of the template group for creating local service interfaces. */
    private static final String SERVICE_FACTORY_LOCAL_TEMPLATES_GROUP = "ServiceFactoryLocal";

    /** Defines the name of the template group for creating service client interfaces. */
    private static final String CLIENT_INTERFACE_TEMPLATES_GROUP = "ServiceClientInterface";

    /** Defines the name of the template to create the opening section of output files. */
    protected static final String FILE_OPEN_TEMPLATE = "file_open";

    /** Defines the name of the template to invoke on all entities. */
    protected static final String FOR_BEANS_TEMPLATE = "for_beans";

    /** Defines the name of the template to create the closing section of output files. */
    protected static final String FILE_CLOSE_TEMPLATE = "file_close";

    /** Holds the name of the class to output the service factory interface. */
    private static final String SF_INTERFACE_CLASS_NAME = "ServiceFactory";

    /** Holds the name of the class to output the service factory client implementation. */
    private static final String SF_CLIENT_CLASS_NAME = "ClientServiceFactory";

    /** Holds the name of the class to output the service factory local implementation. */
    private static final String SF_LOCAL_CLASS_NAME = "LocalServiceFactory";

    /** Holds the string template group to generate service factory interfaces from. */
    private final STGroup sfInterfaceTemplates;

    /** Holds the string template group to generate service factory clients from. */
    private final STGroup sfClientTemplates;

    /** Holds the string template group to generate service factory local implementations from. */
    private final STGroup sfLocalTemplates;

    /** Holds the string template group to generate service client interfaces from. */
    private final STGroup clientInterfaceTemplates;

    /** Holds the name of the file to output the service factory interface. */
    private String sfInterfaceFileName = "ServiceFactory";

    /** Holds the name of the file to output the service factory client implementation. */
    private String sfClientFileName = "ClientServiceFactory";

    /** Holds the name of the file to output the service factory local implementation. */
    private String sfLocalFileName = "LocalServiceFactory";

    /** Output handler used to build up service factory interface methods in. */
    private final BufferingTemplateHandler sfInterfaceHandler = new BufferingTemplateHandler();

    /** Output handler used to build up service factory client methods in. */
    private final BufferingTemplateHandler sfClientHandler = new BufferingTemplateHandler();

    /** Output handler used to build up service factory local methods in. */
    private final BufferingTemplateHandler sfLocalHandler = new BufferingTemplateHandler();

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
    public ServiceFactoryGenerator(String templateDir) {
        super(templateDir);

        sfInterfaceTemplates = new STGroupFile(templateGroupToFileName(SERVICE_FACTORY_INTERFACE_TEMPLATES_GROUP));
        sfInterfaceTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        sfClientTemplates = new STGroupFile(templateGroupToFileName(SERVICE_FACTORY_CLIENT_TEMPLATES_GROUP));
        sfClientTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        sfLocalTemplates = new STGroupFile(templateGroupToFileName(SERVICE_FACTORY_LOCAL_TEMPLATES_GROUP));
        sfLocalTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        clientInterfaceTemplates = new STGroupFile(templateGroupToFileName(CLIENT_INTERFACE_TEMPLATES_GROUP));
        clientInterfaceTemplates.registerRenderer(String.class, new CamelCaseRenderer());
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Ensures that the opening section of the hibernate config file is created.
     */
    public void preApply(Catalogue catalogue) {
        // Output the configuration sections built up in the output buffers in the correct order to the configuration file.
        sfInterfaceFileName = nameToJavaFileName(outputDir, outputPackage, "", SF_INTERFACE_CLASS_NAME, "");
        sfClientFileName = nameToJavaFileName(clientOutputDir, clientPackage, "", SF_CLIENT_CLASS_NAME, "");
        sfLocalFileName = nameToJavaFileName(localOutputDir, localPackage, "", SF_LOCAL_CLASS_NAME, "");

        generateTemplateOpenToFile(sfInterfaceFileName, sfInterfaceTemplates, outputPackage, FILE_OPEN_TEMPLATE, false);
        generateTemplateOpenToFile(sfClientFileName, sfClientTemplates, clientPackage, FILE_OPEN_TEMPLATE, false);
        generateTemplateOpenToFile(sfLocalFileName, sfLocalTemplates, localPackage, FILE_OPEN_TEMPLATE, false);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Ensures that the closing section of the hibernate config file is created.
     */
    public void postApply(Catalogue catalogue) {
        RenderTemplateHandler[] handlers =
            new RenderTemplateHandler[] { sfInterfaceHandler, sfClientHandler, sfLocalHandler };
        STGroup[] templates = new STGroup[] { sfInterfaceTemplates, sfClientTemplates, sfLocalTemplates };
        String[] names =
            new String[] {
                nameToJavaFileName(outputDir, "", "", "ServiceFactory"),
                nameToJavaFileName(clientOutputDir, "", "", "ClientServiceFactory"),
                nameToJavaFileName(localOutputDir, "", "", "LocalServiceFactory")
            };

        generate(model, typesToGenerate, templates, names, handlers, FOR_BEANS_TEMPLATE);

        flushHandlerToFile(sfInterfaceFileName, sfInterfaceHandler, true);
        flushHandlerToFile(sfClientFileName, sfClientHandler, true);
        flushHandlerToFile(sfLocalFileName, sfLocalHandler, true);

        generateTemplateCloseToFile(sfInterfaceFileName, sfInterfaceTemplates, FILE_CLOSE_TEMPLATE);
        generateTemplateCloseToFile(sfClientFileName, sfClientTemplates, FILE_CLOSE_TEMPLATE);
        generateTemplateCloseToFile(sfLocalFileName, sfLocalTemplates, FILE_CLOSE_TEMPLATE);
    }

    /**
     * Generates a bean class for a component type in the catalogue model.
     *
     * @param type The component type to create a bean for.
     */
    public void visit(EntityType type) {
        // Add the type to be generated in the service factory interface and implementations.
        ComponentType decoratedType = (ComponentTypeDecorator) TypeDecoratorFactory.decorateType(type);
        typesToGenerate.add(decoratedType);

        // Generate the client service interfaces.
        STGroup[] templates;
        String[] names;
        Map<String, Type> fields = decoratedType.getAllPropertyTypes();
        Map<String, Type> extraFields = null;
        RenderTemplateHandler[] handlers = new RenderTemplateHandler[] { fileOutputRenderTemplateHandler };

        templates = new STGroup[] { clientInterfaceTemplates };
        names = new String[] { nameToJavaFileName(clientOutputDir, "", type.getName(), "Client"), };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
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
