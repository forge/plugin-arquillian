/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.testframework.testng;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.arquillian.api.TestFrameworkFacet;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
@ApplicationScoped
public class TestNGFacet extends TestFrameworkFacet
{
   @Override
   public String getFrameworkName()
   {
      return "testng";
   }

   @Override
   public String getTemplateName()
   {
      return "TestNGTest.vtl";
   }

   @Override
   public String getVersionPropertyName()
   {
      return "version.testng";
   }


   @Override
   public DependencyBuilder createFrameworkDependency()
   {
      return DependencyBuilder.create()
            .setGroupId("org.testng")
            .setArtifactId("testng")
            .setScopeType("test");
   }

   @Override
   public DependencyBuilder createArquillianDependency()
   {
      return DependencyBuilder.create()
            .setGroupId("org.jboss.arquillian.testng")
            .setArtifactId("arquillian-testng-container")
            .setScopeType("test");
   }
}
