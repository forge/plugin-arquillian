/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.testframework.junit;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.arquillian.api.TestFrameworkFacet;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
@ApplicationScoped
public class JUnitFacet extends TestFrameworkFacet
{
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
   public DependencyBuilder createFrameworkDependency()
   {
      return DependencyBuilder.create()
                              .setGroupId("junit")
                              .setArtifactId("junit")
                              .setScopeType("test");
   }

   @Override
   public DependencyBuilder createArquillianDependency()
   {
      return DependencyBuilder.create()
                              .setGroupId("org.jboss.arquillian.junit")
                              .setArtifactId("arquillian-junit-container")
                              .setScopeType("test");
   }
}
