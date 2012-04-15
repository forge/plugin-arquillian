package org.jboss.forge.arquillian;

import org.apache.maven.model.Profile;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaExecutionFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.java.JavaResource;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.events.PickupResource;
import org.jboss.forge.shell.plugins.*;
import org.jboss.forge.arquillian.commandcompleter.ContainerCommandCompleter;
import org.jboss.forge.arquillian.commandcompleter.ProfileCommandCompleter;
import org.jboss.forge.arquillian.container.*;
import org.jboss.forge.arquillian.container.ContainerDirectoryParser;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

@Alias("arquillian")
@RequiresFacet(JavaSourceFacet.class)
@Help("A plugin that helps setting up Arquillian tests")
public class ArquillianPlugin implements Plugin {

    static {
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "class");
        properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        Velocity.init(properties);
    }

    public static final String ARQ_CORE_VERSION_PROP_NAME = "version.arquillian_core";
    public static final String ARQ_CORE_VERSION_PROP = "${" + ARQ_CORE_VERSION_PROP_NAME + "}";

    public static final String JUNIT_VERSION_PROP_NAME = "version.junit";
    public static final String JUNIT_VERSION_PROP = "${" + JUNIT_VERSION_PROP_NAME + "}";

    public static final String TESTNG_VERSION_PROP_NAME = "version.testng";
    public static final String TESTNG_VERSION_PROP = "${" + TESTNG_VERSION_PROP_NAME + "}";

    @Inject
    private Project project;

    @Inject
    BeanManager beanManager;

    @Inject
    private Event<PickupResource> pickup;

    @Inject
    @Current
    private JavaResource resource;

    @Inject
    private Shell shell;

    private String arquillianVersion;

    private DependencyFacet dependencyFacet;

    @Inject
    ContainerInstaller containerInstaller;

    @Inject
    private ContainerDirectoryParser containerDirectoryParser;


    @Inject
    @Any
    Event<ContainerInstallEvent> installEvent;

    @SetupCommand
    public void installContainer(
            @Option(name = "container", required = true, completer = ContainerCommandCompleter.class) String containerId,
            @Option(name = "testframework", required = false, defaultValue = "junit") String testframework) {

        dependencyFacet = project.getFacet(DependencyFacet.class);

        installArquillianBom();

        if (testframework.equals("junit")) {
            installJunitDependencies();
        } else {
            installTestNgDependencies();
        }

        List<Container> containers = containerDirectoryParser.getContainers();

        boolean foundContainer = false;
        for (Container container : containers) {
            if (container.getId().equals(containerId)) {
                containerInstaller.installContainer(container);

                installEvent.fire(new ContainerInstallEvent(container));

                foundContainer = true;
                break;
            }
        }

        if (!foundContainer) {
            throw new RuntimeException("Container not recognized");
        }
        
        ResourceFacet resources = project.getFacet(ResourceFacet.class);
        FileResource<?> resource = (FileResource<?>) resources.getTestResourceFolder().getChild("arquillian.xml");
        if (!resource.exists()) {
            Node arquillianConfig = createNewArquillianConfig();
            resource.setContents(XMLParser.toXMLString(arquillianConfig));
        }
    }

    @Command(value = "configure-container")
    public void configureContainer(@Option(name = "profile", required = true, completer = ProfileCommandCompleter.class) String profileId) {

        Profile profile = getProfile(profileId);
        Container container = getContainer(profile);

        Configuration configuration = shell.promptChoiceTyped("Which property do you want to set?", container.getConfigurations());

        ResourceFacet resources = project.getFacet(ResourceFacet.class);
        FileResource<?> resource = (FileResource<?>) resources.getTestResourceFolder().getChild("arquillian.xml");

        Node xml = null;
        if (!resource.exists()) {
            xml = createNewArquillianConfig();
        } else {
            xml = XMLParser.parse(resource.getResourceInputStream());
        }

        String value = shell.prompt("What value do you want to set?");
        addPropertyToArquillianConfig(xml, container.getId(), configuration.getName(), value);

        resource.setContents(XMLParser.toXMLString(xml));
    }

    private Container getContainer(Profile profile) {
        for (Container container : containerDirectoryParser.getContainers()) {
            if (container.getId().equals(profile.getId())) {
                return container;
            }
        }

        throw new RuntimeException("Container not found for profile " + profile);
    }

    private Profile getProfile(String profile) {
        MavenCoreFacet mavenCoreFacet = project.getFacet(MavenCoreFacet.class);
        List<Profile> profileList = mavenCoreFacet.getPOM().getProfiles();
        for (Profile p : profileList) {
            if (p.getId().equals(profile)) {
                return p;
            }
        }

        throw new RuntimeException("Profile " + profile + " could not be found");
    }

    private Node createNewArquillianConfig() {
        return XMLParser.parse("<arquillian xmlns=\"http://jboss.org/schema/arquillian\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "            xsi:schemaLocation=\"http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd\"></arquillian>");
    }

    private void addPropertyToArquillianConfig(Node xml, String container, String key, String value) {

        xml.getOrCreate("container@qualifier=" + container)
           .getOrCreate("configuration")
           .getOrCreate("property@name=" + key)
           .text(value);
    }


    @Command(value = "create-test", help = "Create a new test class with a default @Deployment method")
    public void createTest(
            @Option(name = "class", required = true, type = PromptType.JAVA_CLASS) JavaResource classUnderTest,
            @Option(name = "enableJPA", required = false, flagOnly = true) boolean enableJPA,
            final PipeOut out) throws FileNotFoundException {
        JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);

        JavaSource<?> javaSource = classUnderTest.getJavaSource();

        VelocityContext context = new VelocityContext();
        context.put("package", javaSource.getPackage());
        context.put("ClassToTest", javaSource.getName());
        context.put("classToTest", javaSource.getName().toLowerCase());
        context.put("packageImport", javaSource.getPackage());
        context.put("enableJPA", enableJPA);

        StringWriter writer = new StringWriter();
        Velocity.mergeTemplate("TemplateTest.vtl", "UTF-8", context, writer);

        JavaClass testClass = JavaParser.parse(JavaClass.class, writer.toString());
        java.saveTestJavaSource(testClass);

        pickup.fire(new PickupResource(java.getTestJavaResource(testClass)));
    }

    /**
     * This command exports an Archive generated by a @Deployment method to disk. Because the project's classpath is not
     * in the classpath of Forge, the @Deployment method can't be called directly.The plugin works in the following
     * steps: 1 - Generate a new class to the src/test/java folder 2 - Compile the user's classes using mvn test-compile
     * 3 - Run the generated class using mvn exec:java (so that the project's classes are on the classpath) 4 - Delete
     * the generated class
     */
    @Command(value = "export", help = "Export a @Deployment configuration to a zip file on disk.")
    @RequiresResource(JavaResource.class)
    public void exportDeployment(@Option(name = "keepExporter", flagOnly = true) boolean keepExporter, PipeOut out) {

        JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        try {
            JavaResource testJavaResource = java.getTestJavaResource("forge/arquillian/DeploymentExporter.java");
            if (!testJavaResource.exists()) {
                generateExporterClass(java);
            }

            runExporterClass(out);

            if (!keepExporter) {
                testJavaResource.delete();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error while calling generated DeploymentExporter ", ex);
        }
    }

    private void runExporterClass(PipeOut out) throws IOException {
        JavaExecutionFacet facet = project.getFacet(JavaExecutionFacet.class);
        facet.executeProjectClass("forge.arquillian.DeploymentExporter", resource.getJavaSource().getQualifiedName());
    }

    private void generateExporterClass(JavaSourceFacet java) throws FileNotFoundException {

        VelocityContext context = new VelocityContext();

        StringWriter writer = new StringWriter();
        Velocity.mergeTemplate("DeploymentExporter.vtl", "UTF-8", context, writer);
        JavaClass deploymentExporter = JavaParser.parse(JavaClass.class, writer.toString());

        java.saveTestJavaSource(deploymentExporter);
        java.saveTestJavaSource(deploymentExporter);
    }

    private void installJunitDependencies() {
        DependencyBuilder junitDependency = createJunitDependency();
        if (!dependencyFacet.hasEffectiveDependency(junitDependency)) {
            List<Dependency> dependencies = dependencyFacet.resolveAvailableVersions(junitDependency);
            Dependency dependency = shell.promptChoiceTyped("Which version of JUnit do you want to install?", dependencies, DependencyUtil.getLatestNonSnapshotVersion(dependencies));

            dependencyFacet.setProperty(JUNIT_VERSION_PROP_NAME, dependency.getVersion());
            dependencyFacet.addDirectDependency(
                    DependencyBuilder.create(dependency).setVersion(JUNIT_VERSION_PROP));
        }

        DependencyBuilder junitArquillianDependency = createJunitArquillianDependency();
        if (!dependencyFacet.hasEffectiveDependency(junitArquillianDependency)) {
            dependencyFacet.addDirectDependency(junitArquillianDependency);
        }
    }

    private void installTestNgDependencies() {
        DependencyBuilder testngDependency = createTestNgDependency();
        if (!dependencyFacet.hasEffectiveDependency(testngDependency)) {
            List<Dependency> dependencies = dependencyFacet.resolveAvailableVersions(testngDependency);
            Dependency dependency = shell.promptChoiceTyped("Which version of TestNG do you want to install?", dependencies, DependencyUtil.getLatestNonSnapshotVersion(dependencies));

            dependencyFacet.setProperty(TESTNG_VERSION_PROP_NAME, dependency.getVersion());
            dependencyFacet.addDirectDependency(
                    DependencyBuilder.create(dependency).setVersion(TESTNG_VERSION_PROP));
        }

        DependencyBuilder testNgArquillianDependency = createTestNgArquillianDependency();
        if (!dependencyFacet.hasEffectiveDependency(testNgArquillianDependency)) {
            dependencyFacet.addDirectDependency(testNgArquillianDependency);
        }
    }

    private void installArquillianBom() {
        DependencyBuilder arquillianBom = DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian")
                .setArtifactId("arquillian-bom")
                .setPackagingType("pom")
                .setScopeType(ScopeType.IMPORT);

        arquillianVersion = dependencyFacet.getProperty(ARQ_CORE_VERSION_PROP_NAME);
        if(arquillianVersion == null) {
            List<Dependency> dependencies = dependencyFacet.resolveAvailableVersions(arquillianBom);
            Dependency dependency = shell.promptChoiceTyped("Which version of Arquillian do you want to install?", dependencies, DependencyUtil.getLatestNonSnapshotVersion(dependencies));
            arquillianVersion = dependency.getVersion();
            dependencyFacet.setProperty(ARQ_CORE_VERSION_PROP_NAME, arquillianVersion);
        }

        // need to set version after resolve is done, else nothing will resolve.
        if(!dependencyFacet.hasDirectManagedDependency(arquillianBom)) {
            arquillianBom.setVersion(ARQ_CORE_VERSION_PROP);
            dependencyFacet.addDirectManagedDependency(arquillianBom);
        }
    }

    private DependencyBuilder createJunitDependency() {
        return DependencyBuilder.create()
                .setGroupId("junit")
                .setArtifactId("junit")
                .setScopeType(ScopeType.TEST);
    }

    private DependencyBuilder createJunitArquillianDependency() {
        return DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian.junit")
                .setArtifactId("arquillian-junit-container")
                .setScopeType(ScopeType.TEST);
    }

    private DependencyBuilder createTestNgDependency() {
        return DependencyBuilder.create()
                .setGroupId("org.testng")
                .setArtifactId("testng")
                .setScopeType(ScopeType.TEST);
    }

    private DependencyBuilder createTestNgArquillianDependency() {
        return DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian.testng")
                .setArtifactId("arquillian-testng-container")
                .setScopeType(ScopeType.TEST);
    }
}
