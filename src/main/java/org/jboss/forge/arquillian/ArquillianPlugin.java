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

        DependencyBuilder bomDependency = DependencyBuilder.create().setGroupId("org.jboss.arquillian").setArtifactId("arquillian-bom");
        List<Dependency> bomVersions = dependencyFacet.resolveAvailableVersions(bomDependency);
        Dependency bom = shell.promptChoiceTyped("What version of Arquillian do you want to use?", bomVersions, bomVersions.get(bomVersions.size() - 1));

        dependencyFacet.addManagedDependency(bom);

        if (testframework.equals("junit")) {
            installJunitDependencies();
        } else {
            installTestNgDependencies();
        }

        List<Container> containers = containerDirectoryParser.getContainers();

        boolean foundContainer = false;
        for (Container container : containers) {
            if (container.getId().equals(containerId)) {
                shell.println(container.getName());
                containerInstaller.installContainer(container);

                installEvent.fire(new ContainerInstallEvent(container));

                foundContainer = true;
                break;
            }
        }

        if (!foundContainer) {
            throw new RuntimeException("Container not recognized");
        }

    }

    @Command(value = "configure-container")
    public void configureContainer(@Option(name = "profile", required = true, completer = ProfileCommandCompleter.class) String profileId) {

        Profile profile = getProfile(profileId);
        Container container = getContainer(profile);

        Version version = getContainerVersion(profile, container);

        Configuration configuration = shell.promptChoiceTyped("Which property do you want to set?", version.getConfigurations());


        ResourceFacet resources = project.getFacet(ResourceFacet.class);
        FileResource<?> resource = (FileResource<?>) resources.getTestResourceFolder().getChild("arquillian.xml");

        Node xml = null;
        if (!resource.exists()) {
            xml = createNewArquillianConfig(null, resource);
        } else {
            xml = XMLParser.parse(resource.getResourceInputStream());
        }

        addPropertyToArquillianConfig(xml, container.getId(), configuration.getName(), "myval");

        resource.setContents(XMLParser.toXMLString(xml));
    }

    private Version getContainerVersion(Profile profile, Container container) {
        if (container.getVersions() != null) {
            for (Version version : container.getVersions()) {
                if (version.getName().equals(getContainerVersionFromProfile(profile, container))) {
                    return version;
                }
            }
        }

        throw new RuntimeException("Container version could not be extracted for profile " + profile);
    }

    private String getContainerVersionFromProfile(Profile profile, Container container) {
        for (org.apache.maven.model.Dependency dependency : profile.getDependencies()) {
            if (container.getArtifact_id().equals(dependency.getArtifactId())) {
                return dependency.getVersion();
            }
        }

        throw new RuntimeException("Container version could not be extracted for profile " + profile);
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

    private Node createNewArquillianConfig(String jbossHome, FileResource<?> resource) {
        return XMLParser.parse("<arquillian xmlns=\"http://jboss.org/schema/arquillian\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "            xsi:schemaLocation=\"http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd\"></arquillian>");
    }

    private void addPropertyToArquillianConfig(Node xml, String container, String key, String value) {

        Node config = xml.getSingle("container@qualifier=" + container);
        if (config == null) {
            //TODO: finish this
            config = xml.createChild("container@qualifier=");
        }

        config.createChild("configuration").createChild("property@name=jbossHome").text(jbossHome);
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
        if (!dependencyFacet.hasDependency(junitDependency)) {
            List<Dependency> dependencies = dependencyFacet.resolveAvailableVersions(junitDependency);
            Dependency dependency = shell.promptChoiceTyped("Which version of JUnit do you want to install?", dependencies);
            dependencyFacet.addDependency(dependency);
        }

        DependencyBuilder junitArquillianDependency = createJunitArquillianDependency();
        if (!dependencyFacet.hasDependency(junitArquillianDependency)) {
            List<Dependency> dependencies = dependencyFacet.resolveAvailableVersions(junitArquillianDependency);
            Dependency dependency = shell.promptChoiceTyped("Which version of Arquillian do you want to install?", dependencies, dependencies.get(dependencies.size() - 1));
            arquillianVersion = dependency.getVersion();
            dependencyFacet.addDependency(dependency);
        } else {
            arquillianVersion = dependencyFacet.getDependency(junitArquillianDependency).getVersion();
        }
    }

    private void installTestNgDependencies() {
        DependencyBuilder testngDependency = createTestNgDependency();
        if (!dependencyFacet.hasDependency(testngDependency)) {
            List<Dependency> dependencies = dependencyFacet.resolveAvailableVersions(testngDependency);
            Dependency dependency = shell.promptChoiceTyped("Which version of TestNG do you want to install?", dependencies);
            dependencyFacet.addDependency(dependency);
        }

        DependencyBuilder testNgArquillianDependency = createTestNgArquillianDependency();
        if (!dependencyFacet.hasDependency(testNgArquillianDependency)) {
            List<Dependency> dependencies = dependencyFacet.resolveAvailableVersions(testNgArquillianDependency);
            Dependency dependency = shell.promptChoiceTyped("Which version of Arquillian do you want to install?", dependencies, dependencies.get(dependencies.size() - 1));
            arquillianVersion = dependency.getVersion();
            dependencyFacet.addDependency(dependency);
        } else {
            arquillianVersion = dependencyFacet.getDependency(testNgArquillianDependency).getVersion();
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
                .setVersion(arquillianVersion);
    }
}
