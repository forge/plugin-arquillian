/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package test.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import test.integration.util.DependencyMatcher;
import test.integration.util.Deployments;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
@RunWith(Arquillian.class)
public class TestNGTestGenerationIntegrationTest extends AbstractShellTest
{

   @Deployment
   public static JavaArchive getDeployment()
   {
      return Deployments.basicPluginInfrastructure();
   }

   @Test
   public void shouldGenerateTestNGBasedTest() throws Exception
   {
      final Project project = initializeJavaProject();
      final MavenCoreFacet mavenCoreFacet = project.getFacet(MavenCoreFacet.class);

      queueInputLines("", "", "", "", "", "");
      getShell().execute("java new-class --named Bean --package com.test");
      getShell().execute("arquillian setup --containerName GLASSFISH_EMBEDDED_3.1 --testFramework testng");
      getShell().execute("arquillian create-test --class com.test.Bean");

      assertThat(mavenCoreFacet.getPOM().getDependencies(), hasItem(new DependencyMatcher("testng")));
      assertThat(mavenCoreFacet.getPOM().getDependencies(), hasItem(new DependencyMatcher("arquillian-testng-container")));

      final JavaSource<?> testClass = project.getFacet(JavaSourceFacet.class)
                                             .getTestJavaResource("com.test.BeanTest")
                                             .getJavaSource();
      assertTrue("@Test TestNG should be imported", testClass.hasImport("org.testng.annotations.Test"));

      getShell().execute("build --profile arquillian-glassfish-embedded-3.1");
   }
}
