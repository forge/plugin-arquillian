package org.jboss.seam.forge.arquillian.container;

import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.shrinkwrap.descriptor.spi.Node;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

public class Jboss7Managed implements Container {
    @Inject
    ProfileBuilder builder;

    @Inject
    Project project;

    @Inject
    Shell shell;

    @Override
    public void installDependencies(String arquillianVersion) {
        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);

        DependencyBuilder dep1 = DependencyBuilder.create()
                .setGroupId("org.jboss.as")
                .setArtifactId("jboss-as-arquillian-container-managed")
                .setScopeType(ScopeType.TEST);

        List<Dependency> dependencies = dependencyFacet.resolveAvailableVersions(dep1);
        Dependency dependency = shell.promptChoiceTyped("Which version of JBoss AS 7 do you want to use?", dependencies, dependencies.get(dependencies.size() - 1));

        builder.addProfile("arq-jbossas-7-managed", dependency);


        String jbossHome = shell.promptCommon("What is your JBoss home?", PromptType.FILE_PATH);

        Node xml = XMLParser.parse("<arquillian xmlns=\"http://jboss.org/schema/arquillian\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "            xsi:schemaLocation=\"http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd\"></arquillian>");

        Node container = xml.create("container").attribute("qualifier", "jboss").attribute("default", "true");
        container.create("configuration").create("property").attribute("name", "jbossHome").text(jbossHome);
        container.create("protocol").attribute("type", "jmx-as7").create("property").attribute("name", "executionType").text("REMOTE");

        ResourceFacet resources = project.getFacet(ResourceFacet.class);
        FileResource<?> resource = (FileResource<?>)resources.getResourceFolder().getChild("arquillian.xml");
        resource.setContents(XMLParser.toXMLString(xml));


    }
}
