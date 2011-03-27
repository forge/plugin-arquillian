package org.jboss.seam.forge.arquillian;

import org.jboss.seam.forge.arquillian.container.Container;
import org.jboss.seam.forge.parser.JavaParser;
import org.jboss.seam.forge.parser.java.JavaClass;
import org.jboss.seam.forge.parser.java.JavaSource;
import org.jboss.seam.forge.project.Project;
import org.jboss.seam.forge.project.dependencies.DependencyBuilder;
import org.jboss.seam.forge.project.facets.DependencyFacet;
import org.jboss.seam.forge.project.facets.JavaSourceFacet;
import org.jboss.seam.forge.resources.java.JavaResource;
import org.jboss.seam.forge.shell.PromptType;
import org.jboss.seam.forge.shell.events.InstallFacets;
import org.jboss.seam.forge.shell.plugins.*;
import org.jboss.seam.forge.shell.util.BeanManagerUtils;
import org.jboss.seam.forge.spec.cdi.CDIFacet;
import org.jboss.seam.forge.spec.jpa.PersistenceFacet;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.FileNotFoundException;

@Alias("arquillian")
@RequiresFacet(CDIFacet.class)
public class ArquillianPlugin implements Plugin {
    private static final String TESTNG_VERSION = "5.12.1";
    private static final String JUNIT_VERSION = "4.8.2";

    @Inject private Project project;
    @Inject BeanManager beanManager;
    @Inject @Named("arquillianVersion") String arquillianVersion;
    @Inject
    private Event<InstallFacets> request;


    @Command("setup")
    public void setup(@Option(name = "test-framework", defaultValue = "junit", required = false) String testFramework,
                      @Option(name = "container", required = true) ArquillianContainer container,
                      final PipeOut out) {
        installFacet();

        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);

        if (testFramework.equals("testng")) {
            installTestNgDependencies(dependencyFacet);
        } else {
            installJunitDependencies(dependencyFacet);
        }

        Container contextualInstance = BeanManagerUtils.getContextualInstance(beanManager, container.getContainer());
        contextualInstance.installDependencies();
    }

    private void installJunitDependencies(DependencyFacet dependencyFacet) {
        DependencyBuilder junitDependency = createJunitDependency();
        if (!dependencyFacet.hasDependency(junitDependency)) {
            dependencyFacet.addDependency(junitDependency);
        }

        DependencyBuilder junitArquillianDependency = createJunitArquillianDependency();
        if (!dependencyFacet.hasDependency(junitArquillianDependency)) {
            dependencyFacet.addDependency(junitArquillianDependency);
        }
    }

    private void installTestNgDependencies(DependencyFacet dependencyFacet) {
        DependencyBuilder testngDependency = createTestNgDependency();
        if (!dependencyFacet.hasDependency(testngDependency)) {
            dependencyFacet.addDependency(testngDependency);
        }

        DependencyBuilder testNgArquillianDependency = createTestNgArquillianDependency();
        if (!dependencyFacet.hasDependency(testNgArquillianDependency)) {
            dependencyFacet.addDependency(testNgArquillianDependency);
        }
    }

    @Command("create-test")
    public void createTest(
            @Option(name = "class", required = true, type = PromptType.JAVA_CLASS) JavaResource classUnderTest,
            @Option(name = "enableJPA", required = false, flagOnly = true) boolean enableJPA,
            final PipeOut out) throws FileNotFoundException {
        JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);

        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        boolean junit = dependencyFacet.hasDependency(createJunitDependency());

        JavaSource<?> javaSource = classUnderTest.getJavaSource();
        out.println("TestClass: " + javaSource.getQualifiedName());
        JavaClass testclass = JavaParser.create(JavaClass.class)
                .setPackage(javaSource.getPackage())
                .setName(javaSource.getName() + "Test")
                .setPublic()
                .addAnnotation("RunWith")
                .setLiteralValue("Arquillian.class")
                .getOrigin();

        if (!junit) {
            testclass.setSuperType("Arquillian");
        }

        String testInstanceName = javaSource.getName().toLowerCase();
        testclass.addField()
                .setType(javaSource.getName())
                .setPrivate()
                .setName(testInstanceName)
                .addAnnotation(Inject.class);

        testclass.addMethod()
                .setStatic()
                .setName("createDeployment")
                .setPublic()
                .setReturnType("JavaArchive")
                .setBody(createDeploymentFor(javaSource, enableJPA))
                .addAnnotation("Deployment");

        testclass.addMethod()
                .setName("testIsDeployed")
                .setPublic()
                .setReturnTypeVoid()
                .setBody(createTestMethod(testInstanceName))
                .addAnnotation("Test");

        testclass.addImport("javax.enterprise.inject.spi.BeanManager");
        testclass.addImport("javax.inject.Inject");
        testclass.addImport("org.jboss.arquillian.api.Deployment");
        testclass.addImport("org.jboss.arquillian.junit.Arquillian");
        testclass.addImport("org.jboss.shrinkwrap.api.ShrinkWrap");
        testclass.addImport("org.jboss.shrinkwrap.api.ArchivePaths");
        testclass.addImport("org.jboss.shrinkwrap.api.spec.JavaArchive");
        testclass.addImport("org.jboss.shrinkwrap.api.asset.EmptyAsset");


        if (junit) {
            testclass.addImport("org.junit.Assert");
            testclass.addImport("org.junit.Test");
            testclass.addImport("org.junit.runner.RunWith");
        } else if (dependencyFacet.hasDependency(createTestNgDependency())) {
            testclass.addImport("org.testng.annotations.Test");
        }

        java.saveTestJavaSource(testclass);


    }

    private String createTestMethod(String instanceName) {
        return "Assert.assertNotNull(" + instanceName + ");";
    }

    private String createDeploymentFor(JavaSource<?> javaSource, boolean enableJPA) {
        StringBuilder b = new StringBuilder();
        b.append("return ShrinkWrap.create(JavaArchive.class, \"test.jar\")")
                .append(".addClass(").append(javaSource.getName()).append(".class)")
                .append(".addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create(\"beans.xml\"))");

        if(enableJPA) {
            b.append(".addAsManifestResource(\"persistence.xml\", ArchivePaths.create(\"persistence.xml\"))");
        }

        b.append(";");
        return b.toString();

    }

    private void installFacet() {
        if (!project.hasFacet(ArquillianFacet.class)) {
            request.fire(new InstallFacets(PersistenceFacet.class));
        }
    }

    private DependencyBuilder createJunitDependency() {
        DependencyBuilder dependencyBuilder = DependencyBuilder.create();
        dependencyBuilder.setGroupId("junit");
        dependencyBuilder.setArtifactId("junit");
        dependencyBuilder.setVersion(JUNIT_VERSION);
        return dependencyBuilder;
    }

    private DependencyBuilder createJunitArquillianDependency() {
        DependencyBuilder dependencyBuilder = DependencyBuilder.create();
        dependencyBuilder.setGroupId("org.jboss.arquillian");
        dependencyBuilder.setArtifactId("arquillian-junit");
        dependencyBuilder.setVersion(arquillianVersion);
        return dependencyBuilder;
    }


    private DependencyBuilder createTestNgDependency() {
        DependencyBuilder dependencyBuilder = DependencyBuilder.create();
        dependencyBuilder.setGroupId("org.testng");
        dependencyBuilder.setArtifactId("testng");
        dependencyBuilder.setVersion(TESTNG_VERSION);
        return dependencyBuilder;
    }

    private DependencyBuilder createTestNgArquillianDependency() {
        DependencyBuilder dependencyBuilder = DependencyBuilder.create();
        dependencyBuilder.setGroupId("org.jboss.arquillian");
        dependencyBuilder.setArtifactId("arquillian-testng");
        dependencyBuilder.setVersion(arquillianVersion);
        return dependencyBuilder;
    }


}
