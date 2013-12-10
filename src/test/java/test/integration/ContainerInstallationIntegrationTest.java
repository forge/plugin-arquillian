/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package test.integration;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.Root;
import org.jboss.forge.arquillian.ArquillianPlugin;
import org.jboss.forge.arquillian.container.Container;
import org.jboss.forge.arquillian.testframework.JUnitProvider;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.seam.render.RenderRoot;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.solder.SolderRoot;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ContainerInstallationIntegrationTest extends AbstractShellTest
{
   @Deployment
   public static JavaArchive getDeployment()
   {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
            .addPackages(true, Root.class.getPackage())
            .addPackages(true, RenderRoot.class.getPackage())
            .addPackages(true, SolderRoot.class.getPackage())
            .addPackages(true, ArquillianPlugin.class.getPackage(), Container.class.getPackage())
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   private Project installContainer(final String container, final List<DependencyMatcher> dependencies) throws Exception
   {
      Project project = initializeJavaProject();

      MavenCoreFacet coreFacet = project.getFacet(MavenCoreFacet.class);

      List<Profile> profiles = coreFacet.getPOM().getProfiles();

      assertThat(profiles.size(), is(0));

      queueInputLines(container, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
      getShell().execute("arquillian setup");

      assertThat(coreFacet.getPOM().getProfiles().size(), is(1));
      Profile profile = coreFacet.getPOM().getProfiles().get(0);

      for (DependencyMatcher dependency : dependencies)
      {
         assertThat(profile.getDependencies(), hasItem(dependency));
      }

      Model pom = coreFacet.getPOM();
      DependencyMatcher arqBom = new DependencyMatcher("arquillian-bom");

      assertThat("Verify arquillian:bom was added to DependencyManagement ",
            pom.getDependencyManagement().getDependencies(), hasItem(arqBom));

      assertNotNull("Verify that the plugin use a version property for arquillian core",
            pom.getProperties().get(ArquillianPlugin.ARQ_CORE_VERSION_PROP_NAME));

      assertNotNull("Verify that the plugin use a version property for junit",
            pom.getProperties().get(JUnitProvider.JUNIT_VERSION_PROP_NAME));

      ResourceFacet facet = project.getFacet(ResourceFacet.class);
      FileResource<?> arquillianXml = facet.getTestResource("arquillian.xml");

      assertThat(arquillianXml, is(notNullValue()));
      assertThat(arquillianXml.exists(), is(true));

      Node arquillianXmlRoot = XMLParser.parse(arquillianXml.getResourceInputStream());
      assertThat(arquillianXmlRoot.getSingle("container@qualifier=" + container), is(notNullValue()));

      return project;
   }

   @Test
   public void installOpenEJBContainer() throws Exception
   {
      installContainer("openejb-embedded-3.1",
            Arrays.asList(
                  new DependencyMatcher("arquillian-openejb-embedded-3.1"),
                  new DependencyMatcher("openejb-core")));
   }

   @Test
   public void installOpenWebBeansContainer() throws Exception
   {
      installContainer("openwebbeans-embedded-1",
            Arrays.asList(
                  new DependencyMatcher("arquillian-openwebbeans-embedded-1"),
                  new DependencyMatcher("openwebbeans-impl")));
   }

   @Test
   public void installGlassfishEmbeddedContainer() throws Exception
   {
      installContainer("glassfish-embedded-3.1",
            Arrays.asList(
                  new DependencyMatcher("arquillian-glassfish-embedded-3.1"),
                  new DependencyMatcher("glassfish-embedded-all")));
   }

   @Test
   public void installGlassfishManagedContainer() throws Exception
   {
      installContainer("glassfish-managed-3.1",
            Arrays.asList(
                  new DependencyMatcher("arquillian-glassfish-managed-3.1")));
   }

   @Test
   public void installGlassfishRemoteContainer() throws Exception
   {
      installContainer("glassfish-remote-3.1",
            Arrays.asList(
                  new DependencyMatcher("arquillian-glassfish-remote-3.1")));
   }

   @Test
   public void installJBossAS51ManagedContainer() throws Exception
   {
      installContainer("jbossas-managed-5.1",
            Arrays.asList(
                  new DependencyMatcher("arquillian-jbossas-managed-5.1")));
   }

   @Test
   public void installJBossAS51RemoteContainer() throws Exception
   {
      installContainer("jbossas-remote-5.1",
            Arrays.asList(
                  new DependencyMatcher("arquillian-jbossas-remote-5.1")));
   }

   @Test
   public void installJBossAS5RemoteContainer() throws Exception
   {
      installContainer("jbossas-remote-5",
            Arrays.asList(
                  new DependencyMatcher("arquillian-jbossas-remote-5")));
   }

   @Test
   public void installJBossAS6EmbeddedContainer() throws Exception
   {
      installContainer("jbossas-embedded-6",
            Arrays.asList(
                  new DependencyMatcher("arquillian-jbossas-embedded-6")));
   }

   @Test
   public void installJBossAS6ManagedContainer() throws Exception
   {
      installContainer("jbossas-managed-6",
            Arrays.asList(
                  new DependencyMatcher("arquillian-jbossas-managed-6")));
   }

   @Test
   public void installJBossAS6RemoteContainer() throws Exception
   {
      installContainer("jbossas-remote-6",
            Arrays.asList(
                  new DependencyMatcher("arquillian-jbossas-remote-6")));
   }

   @Test
   public void installJBossAS7ManagedContainer() throws Exception
   {
      installContainer("jbossas-managed-7",
            Arrays.asList(
                  new DependencyMatcher("jboss-as-arquillian-container-managed")));
   }

   @Test
   public void installJBossAS7RemoteContainer() throws Exception
   {
      installContainer("jbossas-remote-7",
            Arrays.asList(
                  new DependencyMatcher("jboss-as-arquillian-container-remote")));
   }

   @Test
   public void installJetty6EmbeddedContainer() throws Exception
   {
      installContainer("jetty-embedded-6.1",
            Arrays.asList(
                  new DependencyMatcher("arquillian-jetty-embedded-6.1"),
                  new DependencyMatcher("jetty")));
   }

   @Test
   public void installJetty7EmbeddedContainer() throws Exception
   {
      installContainer("jetty-embedded-7",
            Arrays.asList(
                  new DependencyMatcher("arquillian-jetty-embedded-7"),
                  new DependencyMatcher("jetty-webapp")));
   }

   @Test
   public void installTomcat6EmbeddedContainer() throws Exception
   {
      installContainer("tomcat-embedded-6",
            Arrays.asList(
                  new DependencyMatcher("arquillian-tomcat-embedded-6"),
                  new DependencyMatcher("catalina"),
                  new DependencyMatcher("catalina"),
                  new DependencyMatcher("coyote"),
                  new DependencyMatcher("jasper")));
   }

   @Test
   @Ignore("Not in default maven repo")
   public void installWAS7RemoteContainer() throws Exception
   {
      installContainer("was-remote-7",
            Arrays.asList(
                  new DependencyMatcher("arquillian-was-remote-7")));
   }

   @Test
   @Ignore("Not in default maven repo")
   public void installWAS8EmbeddedContainer() throws Exception
   {
      installContainer("was-embedded-8",
            Arrays.asList(
                  new DependencyMatcher("arquillian-was-embedded-8")));
   }

   @Test
   @Ignore("Not in default maven repo")
   public void installWAS8RemoteContainer() throws Exception
   {
      installContainer("was-remote-8",
            Arrays.asList(
                  new DependencyMatcher("arquillian-was-remote-8")));
   }

   @Test
   public void installTomcat6RemoteContainer() throws Exception
   {
      installContainer("tomcat-remote-6",
            Arrays.asList(
                  new DependencyMatcher("arquillian-tomcat-remote-6")));
   }

   @Test
   public void installWeldEEEmbeddedContainer() throws Exception
   {
      installContainer("weld-ee-embedded-1.1",
            Arrays.asList(
                  new DependencyMatcher("arquillian-weld-ee-embedded-1.1")));
   }

   @Test
   public void installWeldSEEmbeddedContainer() throws Exception
   {
      installContainer("weld-se-embedded-1",
            Arrays.asList(
                  new DependencyMatcher("arquillian-weld-se-embedded-1")));
   }

   @Test
   public void installWeldSEEmbedded1_1Container() throws Exception
   {
      installContainer("weld-se-embedded-1.1",
            Arrays.asList(
                  new DependencyMatcher("arquillian-weld-se-embedded-1.1")));
   }

   @Test
   public void installWWeblogicRemoteContainer() throws Exception
   {
      installContainer("wls-remote-10.3",
            Arrays.asList(
                  new DependencyMatcher("arquillian-wls-remote-10.3")));
   }

   @Test
   public void installMultipleTimesShouldOverwriteProfile() throws Exception
   {
      Project project = initializeJavaProject();

      MavenCoreFacet coreFacet = project.getFacet(MavenCoreFacet.class);

      List<Profile> profiles = coreFacet.getPOM().getProfiles();
      assertThat(profiles.size(), is(0));

      queueInputLines("JBOSS_AS_REMOTE_7", "19", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
      getShell().execute("arquillian setup");

      queueInputLines("JBOSS_AS_REMOTE_7", "19", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
      getShell().execute("arquillian setup");

      assertThat(coreFacet.getPOM().getProfiles().size(), is(1));
   }

   @Test
   public void installContainerWithDownload() throws Exception
   {
      Project project = initializeJavaProject();

      MavenCoreFacet coreFacet = project.getFacet(MavenCoreFacet.class);

      List<Profile> profiles = coreFacet.getPOM().getProfiles();
      assertThat(profiles.size(), is(0));

      // answer y to download server
      queueInputLines("JBOSS_AS_MANAGED_4.2", "", "", "", "", "", "", "y", "");
      getShell().execute("arquillian setup");

      assertThat(coreFacet.getPOM().getProfiles().size(), is(1));
      Profile profile = coreFacet.getPOM().getProfiles().get(0);

      assertThat(profile.getDependencies(), hasItems(
            new DependencyMatcher("arquillian-jbossas-managed-4.2"),
            new DependencyMatcher("jboss-server-manager"),
            new DependencyMatcher("dom4j"),
            new DependencyMatcher("jbossall-client")));

      assertThat(profile.getBuild().getPlugins().size(), is(2));
      assertThat(profile.getBuild().getPlugins().get(1).getArtifactId(), is("maven-dependency-plugin"));

   }

   class DependencyMatcher extends BaseMatcher<Dependency>
   {
      private final String artifactId;

      public DependencyMatcher(final String artifactId)
      {
         this.artifactId = artifactId;
      }

      @Override
      public boolean matches(final Object o)
      {
         Dependency d = (Dependency) o;
         return d.getArtifactId().equals(artifactId);
      }

      @Override
      public void describeTo(final Description description)
      {
      }
   }
}
