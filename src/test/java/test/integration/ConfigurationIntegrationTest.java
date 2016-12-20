/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package test.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
@RunWith(Arquillian.class) @Ignore
public class ConfigurationIntegrationTest
{
   @Deployment
   @AddonDependencies({
            @AddonDependency(name = "org.jboss.forge.addon:projects"),
            @AddonDependency(name = "org.jboss.forge.addon:maven"),
            @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness")
   })
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap
               .create(AddonArchive.class)
               .addBeansXML()
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:projects"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:maven"),
                        AddonDependencyEntry.create("org.arquillian.forge:arquillian-addon"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness")
               );

      return archive;
   }
   
   @Inject
   private UITestHarness testHarness;

   @Inject
   private ProjectFactory factory;

   /*

   @Test
   public void configureContainer() throws Exception
   {
      Project project = factory.createTempProject();

      MavenCoreFacet coreFacet = project.getFacet(MavenCoreFacet.class);

      List<Profile> profiles = coreFacet.getPOM().getProfiles();
      assertThat(profiles.size(), is(0));

      queueInputLines("JBOSS_AS_MANAGED_6", "", "", "", "", "", "", "");
      getShell().execute("arquillian setup");

      queueInputLines("arquillian-jbossas-managed-6", "2", "8000", "");
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
      assertThat(profiles.size(), is(0));

      queueInputLines("JBOSS_AS_MANAGED_6", "", "", "", "", "", "", "");
      getShell().execute("arquillian setup");

      queueInputLines("arquillian-jbossas-managed-6", "2", "8000", "");
      getShell().execute("arquillian configure-container");

      queueInputLines("arquillian-jbossas-managed-6", "2", "8000", "");
      getShell().execute("arquillian configure-container");

      ResourceFacet facet = project.getFacet(ResourceFacet.class);
      FileResource<?> arquillianXML = facet.getTestResource("arquillian.xml");

      assertThat(arquillianXML, is(notNullValue()));
      assertThat(arquillianXML.exists(), is(true));

      String content = new String(IOUtil.asByteArray(arquillianXML.getResourceInputStream()));
      Assert.assertTrue("Option should be overwritten", content.indexOf("8000") == content.lastIndexOf("8000"));
   }

   @Test
   public void createArquillianXmlOnSetup() throws Exception
   {
      Project project = initializeJavaProject();

      queueInputLines("JBOSS_AS_MANAGED_6", "", "", "", "", "", "", "");
      getShell().execute("arquillian setup");

      ResourceFacet facet = project.getFacet(ResourceFacet.class);
      FileResource<?> arquillianXML = facet.getTestResource("arquillian.xml");

      assertThat(arquillianXML, is(notNullValue()));
      assertThat(arquillianXML.exists(), is(true));
   }
*/
}
