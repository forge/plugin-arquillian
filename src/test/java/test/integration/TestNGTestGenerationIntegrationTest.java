/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package test.integration;


/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
//@RunWith(Arquillian.class)
public class TestNGTestGenerationIntegrationTest {
/*
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
   */
}
