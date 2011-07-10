package org.jboss.seam.forge.arquillian.container;

import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.shrinkwrap.descriptor.spi.Node;

import javax.inject.Inject;
import java.util.List;

public abstract class AbstractJBoss7Container implements Container
{
   protected ProfileBuilder builder;

   protected Project project;

   protected Shell shell;

   protected AbstractJBoss7Container(Shell shell, Project project, ProfileBuilder builder)
   {
      this.shell = shell;
      this.project = project;
      this.builder = builder;
   }

   protected abstract Dependency getContainerDependency();
   protected abstract String getProfileName();

   @Override
   public void installDependencies(String arquillianVersion)
   {
      Dependency dependency = createDependency();

      builder.addProfile(getProfileName(), dependency);

      String jbossHome = shell.promptCommon("What is your JBoss home?", PromptType.FILE_PATH);

      ResourceFacet resources = project.getFacet(ResourceFacet.class);
      FileResource<?> resource = (FileResource<?>) resources.getResourceFolder().getChild("arquillian.xml");
      if (resource.exists())
      {
         editExistingArquillianConfig(jbossHome, resource);
      } else
      {
         createNewArquillianConfig(jbossHome, resource);
      }
   }

   private Dependency createDependency()
   {
      DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);

      Dependency dep1 = getContainerDependency();

      List<Dependency> dependencies = dependencyFacet.resolveAvailableVersions(dep1);

      if (dependencies.isEmpty())
      {
         throw new RuntimeException("Dependency " + dep1.toCoordinates() + " could not be resolved. Does your POM contain the correct repositories?");
      }

      return shell.promptChoiceTyped("Which version of JBoss AS 7 do you want to use?", dependencies, dependencies.get(dependencies.size() - 1));
   }



   private void createNewArquillianConfig(String jbossHome, FileResource<?> resource)
   {
      Node xml = XMLParser.parse("<arquillian xmlns=\"http://jboss.org/schema/arquillian\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
              "            xsi:schemaLocation=\"http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd\"></arquillian>");
      addJbossContainer(jbossHome, xml);
      resource.setContents(XMLParser.toXMLString(xml));
   }

   private void editExistingArquillianConfig(String jbossHome, FileResource<?> resource)
   {

      Node existingConfigFile = XMLParser.parse(resource.getResourceInputStream());
      Node container = existingConfigFile.getSingle("container@qualifier=jboss");
      if (container == null)
      {
         addJbossContainer(jbossHome, existingConfigFile);
         resource.setContents(XMLParser.toXMLString(existingConfigFile));
      }
   }

   private void addJbossContainer(String jbossHome, Node xml)
   {
      Node container = xml.create("container@qualifier=jboss&default=true");
      container.create("configuration").create("property@name=jbossHome").text(jbossHome);
      container.create("protocol@type=jmx-as7").create("property@name=executionType").text("REMOTE");
   }
}
