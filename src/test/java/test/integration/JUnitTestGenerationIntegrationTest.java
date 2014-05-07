/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package test.integration;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
@RunWith(Arquillian.class) @Ignore
public class JUnitTestGenerationIntegrationTest
{
/*
   @Deployment
   public static JavaArchive getDeployment()
   {
      return Deployments.basicPluginInfrastructure();
   }

   @Test
   public void shouldGenerateJUnitBasedTestIfNotSpecifiedExplicitly() throws Exception
   {
      testJUnitTestGenerationUsing("arquillian setup --containerName GLASSFISH_EMBEDDED_3.1");
   }

   @Test
   public void shouldGenerateJUnitBasedTest() throws Exception
   {
      testJUnitTestGenerationUsing("arquillian setup --containerName GLASSFISH_EMBEDDED_3.1 --testFramework JUnit");
   }

   private void testJUnitTestGenerationUsing(String arquillianSetupCommand) throws Exception
   {
      final Project project = initializeJavaProject();
      final MavenCoreFacet mavenCoreFacet = project.getFacet(MavenCoreFacet.class);

      queueInputLines("", "", "", "", "", "");
      getShell().execute("java new-class --named Bean --package com.test");
      getShell().execute(arquillianSetupCommand);
      getShell().execute("arquillian create-test --class com.test.Bean");

      assertThat(mavenCoreFacet.getPOM().getDependencies(), hasItem(new DependencyMatcher("junit")));
      assertThat(mavenCoreFacet.getPOM().getDependencies(), hasItem(new DependencyMatcher("arquillian-junit-container")));

      final JavaSource<?> testClass = project.getFacet(JavaSourceFacet.class)
                                             .getTestJavaResource("com.test.BeanTest")
                                             .getJavaSource();
      assertTrue("@RunWith custom runner should be used", testClass.getAnnotation(RunWith.class).getClassValue().equals(Arquillian.class));

      getShell().execute("build --profile arquillian-glassfish-embedded-3.1");
   }

*/
}
