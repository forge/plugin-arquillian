/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.testframework.testng;

import org.jboss.forge.arquillian.testframework.TestFrameworkFacet;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;

import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;

import javax.inject.Inject;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
@Alias("arq.testframework.testng")
public class TestNGFacet extends TestFrameworkFacet
{

   @Inject
   private ShellPrompt shellPrompt;

   public ShellPrompt getPrompt()
   {
      return shellPrompt;
   }

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
   protected String getVersionPropertyName()
   {
      return "version.testng";
   }


   @Override
   protected DependencyBuilder createFrameworkDependency()
   {
      return DependencyBuilder.create()
            .setGroupId("org.testng")
            .setArtifactId("testng")
            .setScopeType(ScopeType.TEST);
   }

   @Override
   protected DependencyBuilder createArquillianDependency()
   {
      return DependencyBuilder.create()
            .setGroupId("org.jboss.arquillian.testng")
            .setArtifactId("arquillian-testng-container")
            .setScopeType(ScopeType.TEST);
   }
}
