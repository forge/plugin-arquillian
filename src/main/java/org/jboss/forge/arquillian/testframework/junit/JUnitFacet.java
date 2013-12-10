/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.testframework.junit;

import org.jboss.forge.arquillian.testframework.TestFrameworkFacet;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.shell.ShellPrompt;

import javax.inject.Inject;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
public class JUnitFacet extends TestFrameworkFacet
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
      return "junit";
   }

   @Override
   public String getTemplateName()
   {
      return "JUnitTest.vtl";
   }

   @Override
   public String getVersionPropertyName()
   {
      return "version.junit";
   }

   @Override
   protected DependencyBuilder createFrameworkDependency()
   {
      return DependencyBuilder.create()
                              .setGroupId("junit")
                              .setArtifactId("junit")
                              .setScopeType(ScopeType.TEST);
   }

   @Override
   protected DependencyBuilder createArquillianDependency()
   {
      return DependencyBuilder.create()
                              .setGroupId("org.jboss.arquillian.junit")
                              .setArtifactId("arquillian-junit-container")
                              .setScopeType(ScopeType.TEST);
   }
}
