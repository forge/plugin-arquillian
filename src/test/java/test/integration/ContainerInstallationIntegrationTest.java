/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package test.integration;

import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.parser.xml.resources.XMLResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.ui.controller.WizardCommandController;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.api.ArquillianFacet;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.arquillian.command.SetupWizard;
import org.jboss.forge.arquillian.testframework.junit.JUnitFacet;
import org.jboss.forge.parser.xml.Node;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import test.integration.util.DependencyMatcher;
import test.integration.util.Deployments;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
@RunWith(Arquillian.class)
@Ignore
public class ContainerInstallationIntegrationTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.arquillian.forge:arquillian-addon"),
            @AddonDependency(name = "org.jboss.forge.addon:projects"),
            @AddonDependency(name = "org.jboss.forge.addon:maven"),
            @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness")
    })
    public static AddonArchive getDeployment() {
        return Deployments.basicPluginInfrastructure();
    }

    @Inject
    private UITestHarness testHarness;

    @Inject
    private ProjectFactory factory;


    private Project installContainer(final String container, final List<DependencyMatcher> dependencies) throws Exception {
        Project project = factory.createTempProject();
        MavenFacet metadataFacet = project.getFacet(MavenFacet.class);
        List<Profile> profiles = metadataFacet.getModel().getProfiles();
        assertThat(profiles.size(), is(0));

        WizardCommandController setup = testHarness.createWizardController(SetupWizard.class, project.getRootDirectory());
        setup.initialize();
        setup.setValueFor("arquillianVersion", "1.1.3.Final");
        setup.execute();

        assertTrue(project.hasFacet(ArquillianFacet.class));

        Assert.assertTrue(setup.canMoveToNextStep());

        WizardCommandController testFramework = setup.next();
        testFramework.initialize();
        testFramework.setValueFor("testFramework", "junit");
        testFramework.setValueFor("testFrameworkVersion", "4.11");
        testFramework.execute();

        Assert.assertTrue(setup.canMoveToNextStep());

        JUnitFacet junitFacet = new JUnitFacet();
        Model pom = metadataFacet.getModel();
        DependencyMatcher arqBom = new DependencyMatcher("arquillian-bom");

        assertThat("Verify arquillian:bom was added to DependencyManagement ",
                pom.getDependencyManagement().getDependencies(), hasItem(arqBom));

        assertNotNull("Verify that the plugin use a version property for arquillian core",
                pom.getProperties().get(ArquillianFacet.ARQ_CORE_VERSION_PROP_NAME));

        assertNotNull("Verify that the plugin use a version property for junit",
                pom.getProperties().get(junitFacet.getVersionPropertyName()));

        assertThat("Verify that junit arquillian integration was added",
                pom.getDependencies(), hasItem(
                        new DependencyMatcher(junitFacet.createArquillianDependency().getCoordinate().getArtifactId())));

        assertThat("Verify that junit was added",
                pom.getDependencies(), hasItem(
                        new DependencyMatcher(junitFacet.createFrameworkDependency().getCoordinate().getArtifactId())));

        WizardCommandController containerCommand = setup.next();
        containerCommand.initialize();
        containerCommand.setValueFor("containerAdapter", container);
        containerCommand.execute();
        Assert.assertFalse(setup.canMoveToNextStep());

        XMLResource arquillianXml = project.getRootDirectory().getChildOfType(XMLResource.class, "src/test/resources/arquillian.xml");

        assertThat(arquillianXml, is(notNullValue()));
        assertThat(arquillianXml.exists(), is(true));

        Node arquillianXmlRoot = arquillianXml.getXmlSource();
        assertThat(arquillianXmlRoot.getSingle("container@qualifier=" + container), is(notNullValue()));

        assertThat(metadataFacet.getModel().getProfiles().size(), is(1));
        Profile profile = metadataFacet.getModel().getProfiles().get(0);

        for (DependencyMatcher dependency : dependencies) {
            assertThat(profile.getDependencies(), hasItem(dependency));
        }

        return project;
    }

    @Test
    public void installOpenEJBContainer() throws Exception {
        installContainer("openejb-embedded-3.1",
                Arrays.asList(
                        new DependencyMatcher("arquillian-openejb-embedded-3.1"),
                        new DependencyMatcher("openejb-core")));
    }

    @Test
    public void installOpenWebBeansContainer() throws Exception {
        installContainer("openwebbeans-embedded-1",
                Arrays.asList(
                        new DependencyMatcher("arquillian-openwebbeans-embedded-1"),
                        new DependencyMatcher("openwebbeans-impl")));
    }

    @Test
    public void installGlassfishEmbeddedContainer() throws Exception {
        installContainer("glassfish-embedded-3.1",
                Arrays.asList(
                        new DependencyMatcher("arquillian-glassfish-embedded-3.1"),
                        new DependencyMatcher("glassfish-embedded-all")));
    }

    @Test
    public void installGlassfishManagedContainer() throws Exception {
        installContainer("glassfish-managed-3.1",
                singletonList(
                        new DependencyMatcher("arquillian-glassfish-managed-3.1")));
    }

    @Test
    public void installGlassfishRemoteContainer() throws Exception {
        installContainer("glassfish-remote-3.1",
                singletonList(
                        new DependencyMatcher("arquillian-glassfish-remote-3.1")));
    }

    @Test
    public void installJBossAS51ManagedContainer() throws Exception {
        installContainer("jbossas-managed-5.1",
                singletonList(
                        new DependencyMatcher("arquillian-jbossas-managed-5.1")));
    }

    @Test
    public void installJBossAS51RemoteContainer() throws Exception {
        installContainer("jbossas-remote-5.1",
                singletonList(
                        new DependencyMatcher("arquillian-jbossas-remote-5.1")));
    }

    @Test
    public void installJBossAS5RemoteContainer() throws Exception {
        installContainer("jbossas-remote-5",
                singletonList(
                        new DependencyMatcher("arquillian-jbossas-remote-5")));
    }

    @Test
    public void installJBossAS6EmbeddedContainer() throws Exception {
        installContainer("jbossas-embedded-6",
                singletonList(
                        new DependencyMatcher("arquillian-jbossas-embedded-6")));
    }

    @Test
    public void installJBossAS6ManagedContainer() throws Exception {
        installContainer("jbossas-managed-6",
                singletonList(
                        new DependencyMatcher("arquillian-jbossas-managed-6")));
    }

    @Test
    public void installJBossAS6RemoteContainer() throws Exception {
        installContainer("jbossas-remote-6",
                singletonList(
                        new DependencyMatcher("arquillian-jbossas-remote-6")));
    }

    @Test
    public void installJBossAS7ManagedContainer() throws Exception {
        installContainer("jbossas-managed-7",
                singletonList(
                        new DependencyMatcher("jboss-as-arquillian-container-managed")));
    }

    @Test
    public void installJBossAS7RemoteContainer() throws Exception {
        installContainer("jbossas-remote-7",
                singletonList(
                        new DependencyMatcher("jboss-as-arquillian-container-remote")));
    }

    @Test
    public void installWildFlyManagedContainer() throws Exception {
        installContainer("wildfly-managed",
                singletonList(
                        new DependencyMatcher("wildfly-arquillian-container-managed")));
    }

    @Test
    public void installWildFlyRemoteContainer() throws Exception {
        installContainer("wildfly-remote",
                singletonList(
                        new DependencyMatcher("wildfly-arquillian-container-remote")));
    }

    @Test
    public void installJetty6EmbeddedContainer() throws Exception {
        installContainer("jetty-embedded-6.1",
                Arrays.asList(
                        new DependencyMatcher("arquillian-jetty-embedded-6.1"),
                        new DependencyMatcher("jetty")));
    }

    @Test
    public void installJetty7EmbeddedContainer() throws Exception {
        installContainer("jetty-embedded-7",
                Arrays.asList(
                        new DependencyMatcher("arquillian-jetty-embedded-7"),
                        new DependencyMatcher("jetty-webapp")));
    }

    @Test
    public void installTomcat6EmbeddedContainer() throws Exception {
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
    public void installWAS7RemoteContainer() throws Exception {
        installContainer("was-remote-7",
                singletonList(
                        new DependencyMatcher("arquillian-was-remote-7")));
    }

    @Test
    @Ignore("Not in default maven repo")
    public void installWAS8EmbeddedContainer() throws Exception {
        installContainer("was-embedded-8",
                singletonList(
                        new DependencyMatcher("arquillian-was-embedded-8")));
    }

    @Test
    @Ignore("Not in default maven repo")
    public void installWAS8RemoteContainer() throws Exception {
        installContainer("was-remote-8",
                singletonList(
                        new DependencyMatcher("arquillian-was-remote-8")));
    }

    @Test
    public void installTomcat6RemoteContainer() throws Exception {
        installContainer("tomcat-remote-6",
                singletonList(
                        new DependencyMatcher("arquillian-tomcat-remote-6")));
    }

    @Test
    public void installWeldEEEmbeddedContainer() throws Exception {
        installContainer("weld-ee-embedded-1.1",
                singletonList(
                        new DependencyMatcher("arquillian-weld-ee-embedded-1.1")));
    }

    @Test
    public void installWeldSEEmbeddedContainer() throws Exception {
        installContainer("weld-se-embedded-1",
                singletonList(
                        new DependencyMatcher("arquillian-weld-se-embedded-1")));
    }

    @Test
    public void installWeldSEEmbedded1_1Container() throws Exception {
        installContainer("weld-se-embedded-1.1",
                singletonList(
                        new DependencyMatcher("arquillian-weld-se-embedded-1.1")));
    }

    @Test
    public void installWWeblogicRemoteContainer() throws Exception {
        installContainer("wls-remote-10.3",
                singletonList(
                        new DependencyMatcher("arquillian-wls-remote-10.3")));
    }
/*
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
*/
}
