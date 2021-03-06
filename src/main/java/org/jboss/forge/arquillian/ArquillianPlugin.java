/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian;

import static org.jboss.forge.arquillian.commandcompleter.TestFrameworkCompleter.OPTION_TEST_FRAMEWORK;
import static org.jboss.forge.arquillian.commandcompleter.ContainerCommandCompleter.*;

import org.apache.maven.model.Profile;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jboss.forge.arquillian.commandcompleter.ContainerCommandCompleter;
import org.jboss.forge.arquillian.commandcompleter.ProfileCommandCompleter;
import org.jboss.forge.arquillian.commandcompleter.TestFrameworkCompleter;
import org.jboss.forge.arquillian.container.Configuration;
import org.jboss.forge.arquillian.container.Container;
import org.jboss.forge.arquillian.container.ContainerDirectoryParser;
import org.jboss.forge.arquillian.container.ContainerType;
import org.jboss.forge.arquillian.testframework.ProvidesFacetForQualifier;
import org.jboss.forge.arquillian.testframework.TestFrameworkFacet;
import org.jboss.forge.arquillian.testframework.TestFrameworkFacetInstaller;
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
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.java.JavaResource;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.events.PickupResource;
import org.jboss.forge.shell.plugins.*;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

@Alias("arquillian")
@RequiresProject
@RequiresFacet({ DependencyFacet.class, JavaSourceFacet.class })
@Help("This plugin will help you setting up Arquillian tests.")
public class ArquillianPlugin implements Plugin
{

   static
   {
      Properties properties = new Properties();
      properties.setProperty("resource.loader", "class");
      properties.setProperty("class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

      Velocity.init(properties);
   }

   public static final String ARQ_CORE_VERSION_PROP_NAME = "version.arquillian_core";
   public static final String ARQ_CORE_VERSION_PROP = "${" + ARQ_CORE_VERSION_PROP_NAME + "}";

   private String arquillianVersion;

   private DependencyFacet dependencyFacet;

   @Inject
   private Project project;

   @Inject
   private BeanManager beanManager;

   @Inject
   private Event<PickupResource> pickup;

   @Inject
   @Current
   private JavaResource resource;

   @Inject
   private Shell shell;

   @Inject
   private ContainerInstaller containerInstaller;

   @Inject
   private ContainerDirectoryParser containerDirectoryParser;

   @Inject
   @Any
   private Event<ContainerInstallEvent> installEvent;

   @Inject
   private Event<InstallFacets> installFacetsEvent;

   @Inject
   @Any
   private Instance<TestFrameworkFacetInstaller> testFrameworkFacetInstallers;

   @SetupCommand
   public void installContainer(
         @Option(name = OPTION_CONTAINER_NAME, required = true, completer = ContainerCommandCompleter.class) String containerName,
         @Option(name = OPTION_CONTAINER_TYPE, required = false) ContainerType containerType,
         @Option(name = OPTION_TEST_FRAMEWORK, required = false, completer = TestFrameworkCompleter.class, defaultValue = "junit") String selectedTestFramework)
   {
      String containerId = Container.idForDisplayName(containerName);
      this.dependencyFacet = project.getFacet(DependencyFacet.class);

      installArquillianBom();
      installTestFramework(selectedTestFramework);
      installContainer(containerId);
      configureSelectedContainer(containerId);
   }

   private void installContainer(String containerId)
   {
      List<Container> containers;
      try
      {
         containers = containerDirectoryParser.getContainers();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }

      boolean foundContainer = false;
      for (Container container : containers)
      {
         if (container.getId().equals(containerId))
         {
            containerInstaller.installContainer(container);

            installEvent.fire(new ContainerInstallEvent(container));

            foundContainer = true;
            break;
         }
      }

      if (!foundContainer)
      {
         throw new RuntimeException("Container not recognized");
      }
   }

   private void configureSelectedContainer(String containerId)
   {
      ResourceFacet resources = project.getFacet(ResourceFacet.class);
      FileResource<?> resource = (FileResource<?>) resources.getTestResourceFolder().getChild("arquillian.xml");
      Node arquillianConfig = null;
      if (!resource.exists())
      {
         arquillianConfig = createNewArquillianConfig();
      }
      else
      {
         arquillianConfig = XMLParser.parse(resource.getResourceInputStream());
      }

      // Make sure a container config exists for this container (otherwise activating it will fail)
      Node containerConfig = arquillianConfig.getSingle("container@qualifier=" + containerId);
      if (containerConfig == null)
      {
         arquillianConfig.createChild("container@qualifier=" + containerId);
         resource.setContents(XMLParser.toXMLString(arquillianConfig));
      }
   }

   @Command(value = "configure-container")
   public void configureContainer(@Option(name = "profile", required = true, completer = ProfileCommandCompleter.class) String profileId)
   {
      // loop, user presses ctrl-c to exit
      while (true)
      {
         Profile profile = getProfile(profileId);
         Container container;
         try
         {
            container = getContainer(profile);
         } catch (IOException e)
         {
            throw new RuntimeException(e);
         }

         // TODO: show current values in options list
         Configuration configuration = shell.promptChoiceTyped(
               "Which property do you want to set? (default values shown)\n(Press Enter to return to shell)",
               container.getConfigurations(), null);
         if (configuration == null)
         {
            break;
         }

         ResourceFacet resources = project.getFacet(ResourceFacet.class);
         FileResource<?> resource = (FileResource<?>) resources.getTestResourceFolder().getChild("arquillian.xml");

         Node xml = null;
         if (!resource.exists())
         {
            xml = createNewArquillianConfig();
         } else
         {
            xml = XMLParser.parse(resource.getResourceInputStream());
         }

         // TODO show current value
         String value = shell.prompt("What value do you want to assign to the " + configuration.getName() + " property?");
         addPropertyToArquillianConfig(xml, container.getId(), configuration.getName(), value);

         resource.setContents(XMLParser.toXMLString(xml));
      }
   }

   private Container getContainer(Profile profile) throws IOException
   {
      String profileId = profile.getId().replaceFirst("^arq-", "arquillian-");
      for (Container container : containerDirectoryParser.getContainers())
      {
         if (container.getProfileId().equals(profileId))
         {
            return container;
         }
      }

      throw new RuntimeException("Container not found for profile " + profile);
   }

   private Profile getProfile(String profile)
   {
      MavenCoreFacet mavenCoreFacet = project.getFacet(MavenCoreFacet.class);
      List<Profile> profileList = mavenCoreFacet.getPOM().getProfiles();
      for (Profile p : profileList)
      {
         if (p.getId().equals(profile))
         {
            return p;
         }
      }

      throw new RuntimeException("Profile " + profile + " could not be found");
   }

   private Node createNewArquillianConfig()
   {
      return XMLParser
            .parse("<arquillian xmlns=\"http://jboss.org/schema/arquillian\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                  + "            xsi:schemaLocation=\"http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd\"></arquillian>");
   }

   private void addPropertyToArquillianConfig(Node xml, String container, String key, String value)
   {
      xml.getOrCreate("container@qualifier=" + container)
         .getOrCreate("configuration")
         .getOrCreate("property@name=" + key)
         .text(value);
   }

   @Command(value = "create-test", help = "Create a new test class with a default @Deployment method")
   public void createTest(
         @Option(name = "class", required = true, type = PromptType.JAVA_CLASS) JavaResource classUnderTest,
         @Option(name = "enableJPA", required = false, flagOnly = true) boolean enableJPA, final PipeOut out)
         throws FileNotFoundException
   {
      final TestFrameworkFacet testFrameworkFacet = project.getFacet(TestFrameworkFacet.class);
      final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
      final JavaSource<?> javaSource = classUnderTest.getJavaSource();

      final VelocityContext context = initializeVelocityContext(enableJPA, javaSource);

      final StringWriter writer = new StringWriter();
      Velocity.mergeTemplate(testFrameworkFacet.getTemplateName(), "UTF-8", context, writer);

      final JavaClass testClass = JavaParser.parse(JavaClass.class, writer.toString());
      java.saveTestJavaSource(testClass);
      pickup.fire(new PickupResource(java.getTestJavaResource(testClass)));
   }

   private VelocityContext initializeVelocityContext(boolean enableJPA, JavaSource<?> javaSource)
   {
      VelocityContext context = new VelocityContext();
      context.put("package", javaSource.getPackage());
      context.put("ClassToTest", javaSource.getName());
      context.put("classToTest", javaSource.getName().toLowerCase());
      context.put("packageImport", javaSource.getPackage());
      context.put("enableJPA", enableJPA);
      return context;
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
   public void exportDeployment(@Option(name = "keepExporter", flagOnly = true) boolean keepExporter, PipeOut out)
   {

      final JavaSourceFacet javaSourceFacet = project.getFacet(JavaSourceFacet.class);
      try
      {
         JavaResource testJavaResource = javaSourceFacet.getTestJavaResource("forge/arquillian/DeploymentExporter.java");
         if (!testJavaResource.exists())
         {
            generateExporterClass(javaSourceFacet);
         }

         runExporterClass(out);

         if (!keepExporter)
         {
            testJavaResource.delete();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException("Error while calling generated DeploymentExporter ", ex);
      }
   }

   private void runExporterClass(PipeOut out) throws IOException
   {
      JavaExecutionFacet facet = project.getFacet(JavaExecutionFacet.class);
      facet.executeProjectClass("forge.arquillian.DeploymentExporter", resource.getJavaSource().getQualifiedName());
   }

   private void generateExporterClass(JavaSourceFacet java) throws FileNotFoundException
   {

      VelocityContext context = new VelocityContext();

      StringWriter writer = new StringWriter();
      Velocity.mergeTemplate("DeploymentExporter.vtl", "UTF-8", context, writer);
      JavaClass deploymentExporter = JavaParser.parse(JavaClass.class, writer.toString());

      java.saveTestJavaSource(deploymentExporter);
      java.saveTestJavaSource(deploymentExporter);
   }

   private void installArquillianBom()
   {
      DependencyBuilder arquillianBom = DependencyBuilder.create().setGroupId("org.jboss.arquillian")
            .setArtifactId("arquillian-bom").setPackagingType("pom").setScopeType(ScopeType.IMPORT);

      arquillianVersion = dependencyFacet.getProperty(ARQ_CORE_VERSION_PROP_NAME);
      if (arquillianVersion == null)
      {
         List<Dependency> dependencies = dependencyFacet.resolveAvailableVersions(arquillianBom);
         Dependency dependency = shell.promptChoiceTyped("Which version of Arquillian do you want to install?",
               dependencies, DependencyUtil.getLatestNonSnapshotVersion(dependencies));
         arquillianVersion = dependency.getVersion();
         dependencyFacet.setProperty(ARQ_CORE_VERSION_PROP_NAME, arquillianVersion);
      }

      // need to set version after resolve is done, else nothing will resolve.
      if (!dependencyFacet.hasDirectManagedDependency(arquillianBom))
      {
         arquillianBom.setVersion(ARQ_CORE_VERSION_PROP);
         dependencyFacet.addDirectManagedDependency(arquillianBom);
      }
   }

   private void installTestFramework(String selectedTestFramework)
   {
      resolveTestFrameworkInstaller(selectedTestFramework).install();
   }

   private TestFrameworkFacetInstaller resolveTestFrameworkInstaller(String testFramework)
   {
      try
      {
         return testFrameworkFacetInstallers.select(new ProvidesFacetForQualifier(testFramework)).get();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to resolve provider for selected test framework [" + testFramework + "]", e);
      }

   }
}
