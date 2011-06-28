package org.jboss.seam.forge.arquillian.container;

import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.shrinkwrap.descriptor.spi.Node;

import javax.inject.Inject;
import java.util.List;

public class Jboss7Managed implements Container
{
   @Inject
   ProfileBuilder builder;

   @Inject
   Project project;

   @Inject
   Shell shell;

   @Override
   public void installDependencies(String arquillianVersion)
   {
      Dependency dependency = createDependency();

      builder.addProfile("arq-jbossas-7-managed", dependency);

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

      DependencyBuilder dep1 = DependencyBuilder.create()
              .setGroupId("org.jboss.as")
              .setArtifactId("jboss-as-arquillian-container-managed")
              .setScopeType(ScopeType.TEST);

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
      boolean containerExists = false;

      Node existing = XMLParser.parse(resource.getResourceInputStream());
      for (Node node : existing.children())
      {
         if (node.name().equals("container") && node.attribute("qualifier").equals("jboss"))
         {
            containerExists = true;
            shell.println("JBoss container exists in Arquillian.xml, no changes were made");
            break;
         }
      }

      if (!containerExists)
      {
         addJbossContainer(jbossHome, existing);
         resource.setContents(XMLParser.toXMLString(existing));
      }
   }

   private void addJbossContainer(String jbossHome, Node xml)
   {
      Node container = xml.create("container").attribute("qualifier", "jboss").attribute("default", "true");
      container.create("configuration").create("property").attribute("name", "jbossHome").text(jbossHome);
      container.create("protocol").attribute("type", "jmx-as7").create("property").attribute("name", "executionType").text("REMOTE");
   }
}
