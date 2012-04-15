package test.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.Root;
import org.jboss.forge.arquillian.ArquillianPlugin;
import org.jboss.forge.arquillian.container.Container;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.seam.render.RenderRoot;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.io.IOUtil;
import org.jboss.solder.SolderRoot;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class PluginTest extends AbstractShellTest
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
      // .addManifestResource(new
      // ByteArrayAsset("<beans><alternatives><class>org.jboss.forge.arquillian.container.FileContainerDirectoryLocationProvider</class></alternatives></beans>".getBytes()),
      // ArchivePaths.create("beans.xml"));
   }

   private Project installContainer(final String container, final List<DependencyMatcher> dependencies) throws Exception
   {
      Project project = initializeJavaProject();

      MavenCoreFacet coreFacet = project.getFacet(MavenCoreFacet.class);

      List<Profile> profiles = coreFacet.getPOM().getProfiles();
      for (Profile profile : profiles) {
         System.out.println(profile.getId());
      }
      assertThat(profiles.size(), is(0));

      queueInputLines(container, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
      getShell().execute("arquillian setup");

      assertThat(coreFacet.getPOM().getProfiles().size(), is(1));
      Profile profile = coreFacet.getPOM().getProfiles().get(0);

      for (DependencyMatcher dependency : dependencies) {
         assertThat(profile.getDependencies(), hasItem(dependency));
      }

      Model pom = coreFacet.getPOM();
      DependencyMatcher arqBom = new DependencyMatcher("arquillian-bom");

      assertThat("Verify arquillian:bom was added to DependencyManagement ",
              pom.getDependencyManagement().getDependencies(), hasItem(arqBom));

      assertNotNull("Verify that the plugin use a version property for arquillian core",
              pom.getProperties().get(ArquillianPlugin.ARQ_CORE_VERSION_PROP_NAME));

      assertNotNull("Verify that the plugin use a version property for junit",
              pom.getProperties().get(ArquillianPlugin.JUNIT_VERSION_PROP_NAME));

      return project;
   }

   @Test
   public void installOpenEJBContainer() throws Exception
   {
      installContainer("OPENEJB_EMBEDDED_3.1.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-openejb-embedded-3.1"),
                        new DependencyMatcher("openejb-core")));
   }

   @Test
   public void installOpenWebBeansContainer() throws Exception
   {
      installContainer("OPENWEBBEANS_EMBEDDED_1.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-openwebbeans-embedded-1"),
                        new DependencyMatcher("openwebbeans-impl")));
   }

   @Test
   public void installGlassfishEmbeddedContainer() throws Exception
   {
      installContainer("GLASSFISH_EMBEDDED_3.1",
               Arrays.asList(
                        new DependencyMatcher("arquillian-glassfish-embedded-3.1"),
                        new DependencyMatcher("glassfish-embedded-all")));
   }

   @Test
   public void installGlassfishRemoteContainer() throws Exception
   {
      installContainer("GLASSFISH_REMOTE_3.1_(REST)",
               Arrays.asList(
                        new DependencyMatcher("arquillian-glassfish-remote-3.1")));
   }

   @Test
   public void installJBoss51ManagedContainer() throws Exception
   {
      installContainer("JBOSS_AS_MANAGED_5.1.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-jbossas-managed-5.1")));
   }

   @Test
   public void installJBoss51RemoteContainer() throws Exception
   {
      installContainer("JBOSS_AS_REMOTE_5.1.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-jbossas-remote-5.1")));
   }

   @Test
   public void installJBoss5RemoteContainer() throws Exception
   {
      installContainer("JBOSS_AS_REMOTE_5.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-jbossas-remote-5")));
   }

   @Test
   public void installJBoss6EmbeddedContainer() throws Exception
   {
      installContainer("JBOSS_AS_EMBEDDED_6.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-jbossas-embedded-6")));
   }

   @Test
   public void installJBoss6ManagedContainer() throws Exception
   {
      installContainer("JBOSS_AS_MANAGED_6.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-jbossas-managed-6")));
   }

   @Test
   public void installJBoss6RemoteContainer() throws Exception
   {
      installContainer("JBOSS_AS_REMOTE_6.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-jbossas-remote-6")));
   }

   @Test
   public void installJBoss7ManagedContainer() throws Exception
   {
      installContainer("JBOSS_AS_MANAGED_7.X",
               Arrays.asList(
                        new DependencyMatcher("jboss-as-arquillian-container-managed")));
   }

   @Test
   public void installJBoss7RemoteContainer() throws Exception
   {
      installContainer("JBOSS_AS_REMOTE_7.X",
               Arrays.asList(
                        new DependencyMatcher("jboss-as-arquillian-container-remote")));
   }

   @Test
   public void installJetty6EmbeddedContainer() throws Exception
   {
      installContainer("JETTY_EMBEDDED_6.1.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-jetty-embedded-6.1"),
                        new DependencyMatcher("jetty")));
   }

   @Test
   public void installJetty7EmbeddedContainer() throws Exception
   {
      installContainer("JETTY_EMBEDDED_7.X_AND_8.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-jetty-embedded-7"),
                        new DependencyMatcher("jetty-webapp")));
   }

   @Test
   public void installTomcat6EmbeddedContainer() throws Exception
   {
      installContainer("TOMCAT_EMBEDDED_6.X",
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
      installContainer("WEBSPHERE_AS_REMOTE_7.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-was-remote-7")));
   }

   @Test
   @Ignore("Not in default maven repo")
   public void installWAS8EmbeddedContainer() throws Exception
   {
      installContainer("WEBSPHERE_AS_EMBEDDED_8.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-was-embedded-8")));
   }

   @Test
   @Ignore("Not in default maven repo")
   public void installWAS8RemoteContainer() throws Exception
   {
      installContainer("WEBSPHERE_AS_REMOTE_8.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-was-remote-8")));
   }

   @Test
   public void installTomcat6RemoteContainer() throws Exception
   {
      installContainer("TOMCAT_REMOTE_6.X",
               Arrays.asList(
                        new DependencyMatcher("arquillian-tomcat-remote-6")));
   }

   @Test
   public void installWeldEEEmbeddedContainer() throws Exception
   {
      installContainer("WELD_EE_EMBEDDED_1.1",
               Arrays.asList(
                        new DependencyMatcher("arquillian-weld-ee-embedded-1.1")));
   }

   @Test
   public void installWeldSEEmbeddedContainer() throws Exception
   {
      installContainer("WELD_SE_EMBEDDED_1.0",
               Arrays.asList(
                        new DependencyMatcher("arquillian-weld-se-embedded-1")));
   }

   @Test
   public void installWeldSEEmbedded1_1Container() throws Exception
   {
      installContainer("WELD_SE_EMBEDDED_1.1",
               Arrays.asList(
                        new DependencyMatcher("arquillian-weld-se-embedded-1.1")));
   }

   @Test
   public void installWWeblogicRemoteContainer() throws Exception
   {
      installContainer("WEBLOGIC_REMOTE_10.3",
               Arrays.asList(
                        new DependencyMatcher("arquillian-wls-remote-10.3")));
   }

   @Test
   public void installMultipleTimesShouldOverwriteProfile() throws Exception
   {
      Project project = initializeJavaProject();

      MavenCoreFacet coreFacet = project.getFacet(MavenCoreFacet.class);

      List<Profile> profiles = coreFacet.getPOM().getProfiles();
      for (Profile profile : profiles) {
         System.out.println(profile.getId());
      }
      assertThat(profiles.size(), is(0));

      queueInputLines("JBOSS_AS_REMOTE_7.X", "19", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
      getShell().execute("arquillian setup");

      queueInputLines("JBOSS_AS_REMOTE_7.X", "19", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
      getShell().execute("arquillian setup");

      assertThat(coreFacet.getPOM().getProfiles().size(), is(1));

   }

   @Test
   public void installContainerWithDownload() throws Exception
   {
      Project project = initializeJavaProject();

      MavenCoreFacet coreFacet = project.getFacet(MavenCoreFacet.class);

      List<Profile> profiles = coreFacet.getPOM().getProfiles();
      for (Profile profile : profiles) {
         System.out.println(profile.getId());
      }
      assertThat(profiles.size(), is(0));

      queueInputLines("JBOSS_AS_MANAGED_4.2.X", "", "19", "10", "", "", "", "", "y", "");
      getShell().execute("arquillian setup");

      assertThat(coreFacet.getPOM().getProfiles().size(), is(1));
      Profile profile = coreFacet.getPOM().getProfiles().get(0);

      assertThat(profile.getDependencies(), hasItems(
               new DependencyMatcher("arquillian-jbossas-managed-4.2"),
               new DependencyMatcher("jboss-server-manager"),
               new DependencyMatcher("dom4j"),
               new DependencyMatcher("jbossall-client")));

      assertThat(profile.getBuild().getPlugins().size(), is(1));
      assertThat(profile.getBuild().getPlugins().get(0).getArtifactId(), is("maven-dependency-plugin"));

   }

   @Test
   public void configureContainer() throws Exception
   {
      Project project = initializeJavaProject();

      MavenCoreFacet coreFacet = project.getFacet(MavenCoreFacet.class);

      List<Profile> profiles = coreFacet.getPOM().getProfiles();
      for (Profile profile : profiles) {
         System.out.println(profile.getId());
      }
      assertThat(profiles.size(), is(0));

      queueInputLines("JBOSS_AS_MANAGED_6.X", "", "19", "10", "", "", "", "8", "");
      getShell().execute("arquillian setup");

      queueInputLines("JBOSS_AS_MANAGED_6.X", "2", "8000");
      getShell().execute("arquillian configure-container");

      ResourceFacet facet = project.getFacet(ResourceFacet.class);
      FileResource<?> arquillianXML = facet.getTestResource("arquillian.xml");

      assertThat(arquillianXML, is(notNullValue()));
      assertThat(arquillianXML.exists(), is(true));

      String content = new String(IOUtil.asByteArray(arquillianXML.getResourceInputStream()));
      Assert.assertTrue("Option should be writen to file", content.indexOf("8000") != -1);
   }

   @Test
   public void configureContainerMultipleTimes() throws Exception
   {
      Project project = initializeJavaProject();

      MavenCoreFacet coreFacet = project.getFacet(MavenCoreFacet.class);

      List<Profile> profiles = coreFacet.getPOM().getProfiles();
      for (Profile profile : profiles) {
         System.out.println(profile.getId());
      }
      assertThat(profiles.size(), is(0));

      queueInputLines("JBOSS_AS_MANAGED_6.X", "", "19", "10", "", "", "", "8", "");
      getShell().execute("arquillian setup");

      queueInputLines("JBOSS_AS_MANAGED_6.X", "2", "8000");
      getShell().execute("arquillian configure-container");

      queueInputLines("JBOSS_AS_MANAGED_6.X", "2", "8000");
      getShell().execute("arquillian configure-container");

      ResourceFacet facet = project.getFacet(ResourceFacet.class);
      FileResource<?> arquillianXML = facet.getTestResource("arquillian.xml");

      assertThat(arquillianXML, is(notNullValue()));
      assertThat(arquillianXML.exists(), is(true));

      String content = new String(IOUtil.asByteArray(arquillianXML.getResourceInputStream()));
      Assert.assertTrue("Option should be overwritten", content.indexOf("8000") == content.lastIndexOf("8000"));
   }
   
   @Test
   public void createArquillianXmlOnSetup() throws Exception {
       Project project = initializeJavaProject();

       queueInputLines("JBOSS_AS_MANAGED_6.X", "", "19", "10", "", "", "", "8", "");
       getShell().execute("arquillian setup");

       ResourceFacet facet = project.getFacet(ResourceFacet.class);
       FileResource<?> arquillianXML = facet.getTestResource("arquillian.xml");

       assertThat(arquillianXML, is(notNullValue()));
       assertThat(arquillianXML.exists(), is(true));
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
      {}
   }
}
