/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package test.integration;

import org.apache.maven.model.Profile;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.io.IOUtil;
import org.junit.Assert;
import org.junit.Test;
import test.integration.util.Deployments;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ConfigurationIntegrationTest extends AbstractShellTest
{
   @Deployment
   public static JavaArchive getDeployment()
   {
      return Deployments.basicPluginInfrastructure();
   }

   @Test
   public void configureContainer() throws Exception
   {
      Project project = initializeJavaProject();

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

}
