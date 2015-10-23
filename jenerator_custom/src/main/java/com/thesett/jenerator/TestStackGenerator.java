package com.thesett.jenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
 * TestStackGenerator generates a test suite for the generated code.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td>  </td></tr>
 * </table></pre>
 */
public class TestStackGenerator extends BaseGenerator implements EntityTypeVisitor {
    /** Defines the name of the template group for creating the baseline test. */
    private static final String BASELINE_CRUD_TEST_TEMPLATES_GROUP = "BaselineCRUDTest";

    /** Defines the name of the template group for creating the baseline test. */
    private static final String JSON_SERDES_CRUD_TEST_TEMPLATES_GROUP = "JsonSerDesCRUDTest";

    /** Defines the name of the template group for creating the baseline test. */
    private static final String WEB_SERVICE_ISOLATION_CRUD_TEST_TEMPLATES_GROUP = "ServiceWebServiceIsolationCRUDTest";

    /** Defines the name of the template group for creating the baseline test. */
    private static final String WEB_SERVICE_ISOLATION_VALIDATION_TEST_TEMPLATES_GROUP =
        "ServiceWebServiceIsolationValidationTest";

    /** Defines the name of the template group for creating the baseline test. */
    private static final String DATABASE_CRUD_TEST_TEMPLATES_GROUP = "DatabaseCRUDTest";

    /** Defines the name of the template group for creating the baseline test. */
    private static final String DATABASE_VALIDATION_TEST_TEMPLATES_GROUP = "DatabaseValidationTest";

    /** Defines the name of the template group for creating the baseline test. */
    private static final String FULL_STACK_CRUD_TEST_TEMPLATES_GROUP = "FullStackCRUDTest";

    /** Defines the name of the template group for creating the test data scaffolding. */
    private static final String TEST_DATA_TEMPLATES_GROUP = "TestData";

    /** Defines the name of the template group for creating the test setup controller. */
    private static final String TEST_SETUP_CONTROLLER_TEMPLATES_GROUP = "TestSetupController";

    /** Holds the name of the class to output the . */
    private static final String BASELINE_CRUD_TEST_CLASS_NAME = "BaselineCRUDTest";

    /** Holds the name of the class to output the . */
    private static final String JSON_SERDES_CRUD_TEST_CLASS_NAME = "JsonSerDesCRUDTest";

    /** Holds the name of the class to output the . */
    private static final String WEB_SERVICE_ISOLATION_CRUD_TEST_CLASS_NAME = "ServiceWebServiceIsolationCRUDTest";

    /** Holds the name of the class to output the . */
    private static final String WEB_SERVICE_ISOLATION_VALIDATION_TEST_CLASS_NAME =
        "ServiceWebServiceIsolationValidationTest";

    /** Holds the name of the class to output the . */
    private static final String DATABASE_CRUD_TEST_CLASS_NAME = "DatabaseCRUDTest";

    /** Holds the name of the class to output the . */
    private static final String DATABASE_VALIDATION_TEST_CLASS_NAME = "DatabaseValidationTest";

    /** Holds the name of the class to output the . */
    private static final String FULL_STACK_CRUD_TEST_CLASS_NAME = "FullStackCRUDTest";

    /** Holds the name of the class to output the . */
    private static final String TEST_DATA_CLASS_NAME = "TestData";

    /** Holds the name of the class to output the . */
    private static final String TEST_SETUP_CONTROLLER_CLASS_NAME = "AppTestSetupController";

    /** Defines the name of the template to invoke on all entities. */
    protected static final String FOR_BEANS_TEMPLATE = "for_beans";

    /** Holds the string template group to generate the baseline test from. */
    private STGroup baselineCrudTestTemplates;

    /** Holds the string template group to generate the baseline test from. */
    private STGroup jsonSerdesCrudTestTemplates;

    /** Holds the string template group to generate the baseline test from. */
    private STGroup webServiceIsolationCrudTestTemplates;

    /** Holds the string template group to generate the baseline test from. */
    private STGroup webServiceIsolationValidationTestTemplates;

    /** Holds the string template group to generate the baseline test from. */
    private STGroup databaseCrudTestTemplates;

    /** Holds the string template group to generate the baseline test from. */
    private STGroup databaseValidationTestTemplates;

    /** Holds the string template group to generate the baseline test from. */
    private STGroup fullStackCrudTestTemplates;

    /** Holds the string template group to generate the test data scaffolding from. */
    private STGroup testDataTemplates;

    /** Holds the string template group to generate the test setup controller from. */
    private STGroup testSetupControllerTemplates;

    /** Holds the name of the test setup controller file. */
    private String testSetupControllerFileName;

    /** Output handler for the test setup controller. */
    private BufferingTemplateHandler testSetupControllerHandler = new BufferingTemplateHandler();

    /** Holds a list of entities to generate for. */
    private List<Type> typesToGenerate = new LinkedList<>();

    /** Holds a file output handler that overwrites files. */
    protected FileOutputRenderTemplateHandler fileOutputRenderTemplateHandler =
        new FileOutputRenderTemplateHandler(false);

    /** Holds a file output handler that appends to files. */
    protected FileOutputRenderTemplateHandler fileOutputRenderTemplateHandlerAppend =
        new FileOutputRenderTemplateHandler(true);

    private String modelPackage;
    private String unitTestOutputDir;
    private String integrationTestOutputDir;

    /**
     * Creates a Java generator to output to the specified directory root.
     *
     * @param templateDir An alternative directory to load templates from, may be <tt>null</tt> to use defaults.
     */
    public TestStackGenerator(String templateDir) {
        super(templateDir);

        baselineCrudTestTemplates = new STGroupFile(templateGroupToFileName(BASELINE_CRUD_TEST_TEMPLATES_GROUP));
        baselineCrudTestTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        jsonSerdesCrudTestTemplates = new STGroupFile(templateGroupToFileName(JSON_SERDES_CRUD_TEST_TEMPLATES_GROUP));
        jsonSerdesCrudTestTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        webServiceIsolationCrudTestTemplates =
            new STGroupFile(templateGroupToFileName(WEB_SERVICE_ISOLATION_CRUD_TEST_TEMPLATES_GROUP));
        webServiceIsolationCrudTestTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        webServiceIsolationValidationTestTemplates =
            new STGroupFile(templateGroupToFileName(WEB_SERVICE_ISOLATION_VALIDATION_TEST_TEMPLATES_GROUP));
        webServiceIsolationValidationTestTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        databaseCrudTestTemplates = new STGroupFile(templateGroupToFileName(DATABASE_CRUD_TEST_TEMPLATES_GROUP));
        databaseCrudTestTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        databaseValidationTestTemplates =
            new STGroupFile(templateGroupToFileName(DATABASE_VALIDATION_TEST_TEMPLATES_GROUP));
        databaseValidationTestTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        fullStackCrudTestTemplates = new STGroupFile(templateGroupToFileName(FULL_STACK_CRUD_TEST_TEMPLATES_GROUP));
        fullStackCrudTestTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        baselineCrudTestTemplates = new STGroupFile(templateGroupToFileName(BASELINE_CRUD_TEST_TEMPLATES_GROUP));
        baselineCrudTestTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        baselineCrudTestTemplates = new STGroupFile(templateGroupToFileName(BASELINE_CRUD_TEST_TEMPLATES_GROUP));
        baselineCrudTestTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        baselineCrudTestTemplates = new STGroupFile(templateGroupToFileName(BASELINE_CRUD_TEST_TEMPLATES_GROUP));
        baselineCrudTestTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        testDataTemplates = new STGroupFile(templateGroupToFileName(TEST_DATA_TEMPLATES_GROUP));
        testDataTemplates.registerRenderer(String.class, new CamelCaseRenderer());

        testSetupControllerTemplates = new STGroupFile(templateGroupToFileName(TEST_SETUP_CONTROLLER_TEMPLATES_GROUP));
        testSetupControllerTemplates.registerRenderer(String.class, new CamelCaseRenderer());
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Generates the opening sections of the many entities -> one file templates.
     */
    public void preApply(Catalogue catalogue) {
        // Generate the opening section of the many entities -> one file templates.
        testSetupControllerFileName =
            nameToJavaFileName(unitTestOutputDir, outputPackage, "", TEST_SETUP_CONTROLLER_CLASS_NAME, "");

        generateTemplateOpenToFile(testSetupControllerFileName, testSetupControllerTemplates, outputPackage,
            FILE_OPEN_TEMPLATE, false);
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Generates the body and closing sections of the many entities -> one file templates.
     */
    public void postApply(Catalogue catalogue) {
        RenderTemplateHandler[] handlers = new RenderTemplateHandler[] { testSetupControllerHandler };
        STGroup[] templates = new STGroup[] { testSetupControllerTemplates };
        String[] names = new String[] { testSetupControllerFileName };

        generate(model, typesToGenerate, templates, names, handlers, FOR_BEANS_TEMPLATE);

        flushHandlerToFile(testSetupControllerFileName, testSetupControllerHandler, true);

        generateTemplateCloseToFile(testSetupControllerFileName, testSetupControllerTemplates, FILE_CLOSE_TEMPLATE);
    }

    /**
     * Generates a bean class for a component type in the catalogue model.
     *
     * @param type The component type to create a bean for.
     */
    public void visit(EntityType type) {
        // Add all entities to the list of types to generate in the many entities -> one file templates.
        ComponentTypeDecorator decoratedType = (ComponentTypeDecorator) TypeDecoratorFactory.decorateType(type);
        typesToGenerate.add(decoratedType);

        // Generate the entities for the one entity -> one file templates.
        STGroup[] templates;
        String[] names;
        Map<String, Type> fields = decoratedType.getAllPropertyTypes();
        Map<String, Type> extraFields = null;
        RenderTemplateHandler[] handlers =
            new RenderTemplateHandler[] {
                fileOutputRenderTemplateHandler, fileOutputRenderTemplateHandler, fileOutputRenderTemplateHandler,
                fileOutputRenderTemplateHandler, fileOutputRenderTemplateHandler, fileOutputRenderTemplateHandler,
                fileOutputRenderTemplateHandler, fileOutputRenderTemplateHandler
            };

        templates =
            new STGroup[] {
                baselineCrudTestTemplates, jsonSerdesCrudTestTemplates, webServiceIsolationCrudTestTemplates,
                webServiceIsolationValidationTestTemplates, databaseCrudTestTemplates, databaseValidationTestTemplates,
                fullStackCrudTestTemplates, testDataTemplates
            };

        String prevOutputPackage = outputPackage;
        outputPackage = outputPackage + "." + type.getName();

        names =
            new String[] {
                nameToJavaFileName(unitTestOutputDir, "", type.getName(), BASELINE_CRUD_TEST_CLASS_NAME),
                nameToJavaFileName(unitTestOutputDir, "", type.getName(), JSON_SERDES_CRUD_TEST_CLASS_NAME),
                nameToJavaFileName(unitTestOutputDir, "", type.getName(), WEB_SERVICE_ISOLATION_CRUD_TEST_CLASS_NAME),
                nameToJavaFileName(unitTestOutputDir, "", type.getName(),
                    WEB_SERVICE_ISOLATION_VALIDATION_TEST_CLASS_NAME),
                nameToJavaFileName(integrationTestOutputDir, "", type.getName(), DATABASE_CRUD_TEST_CLASS_NAME),
                nameToJavaFileName(integrationTestOutputDir, "", type.getName(), DATABASE_VALIDATION_TEST_CLASS_NAME),
                nameToJavaFileName(integrationTestOutputDir, "", type.getName(), FULL_STACK_CRUD_TEST_CLASS_NAME),
                nameToJavaFileName(unitTestOutputDir, "", type.getName(), TEST_DATA_CLASS_NAME)
            };

        generate(model, decoratedType, templates, names, fields, extraFields, handlers);
        outputPackage = prevOutputPackage;
    }

    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }

    public void setUnitTestOutputDir(String unitTestOutputDir) {
        this.unitTestOutputDir = unitTestOutputDir;
    }

    public String getIntegrationTestOutputDir() {
        return integrationTestOutputDir;
    }

    public void setIntegrationTestOutputDir(String integrationTestOutputDir) {
        this.integrationTestOutputDir = integrationTestOutputDir;
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
