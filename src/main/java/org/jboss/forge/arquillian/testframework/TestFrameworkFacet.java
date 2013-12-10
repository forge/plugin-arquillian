/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.testframework;

import org.jboss.forge.arquillian.DependencyUtil;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.RequiresFacet;

import javax.inject.Inject;
import java.util.List;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
@RequiresFacet(DependencyFacet.class)
public abstract class TestFrameworkFacet extends BaseFacet
{

   protected abstract ShellPrompt getPrompt();

   public abstract String getTemplateName();

   public abstract String getFrameworkName();

   protected abstract String getVersionPropertyName();

   protected abstract DependencyBuilder createFrameworkDependency();

   protected abstract DependencyBuilder createArquillianDependency();

   @Override
   public boolean install()
   {
      installDependencies();
      return true;
   }

   @Override
   public boolean isInstalled()
   {
      return hasEffectiveDependency(createFrameworkDependency())
             && hasEffectiveDependency(createArquillianDependency());
   }

   protected void installDependencies()
   {
      final DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
      final DependencyBuilder frameworkDependency = createFrameworkDependency();
      final DependencyBuilder arquillianDependency = createArquillianDependency();

      if (!hasEffectiveDependency(frameworkDependency))
      {
         promptToSelectAvailableVersions(frameworkDependency);
      }

      if (!hasEffectiveDependency(arquillianDependency))
      {
         dependencyFacet.addDirectDependency(arquillianDependency);
      }
   }

   private boolean hasEffectiveDependency(DependencyBuilder frameworkDependency)
   {
      final DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
      return dependencyFacet.hasEffectiveDependency(frameworkDependency);
   }

   protected void promptToSelectAvailableVersions(DependencyBuilder frameworkDependency)
   {
      final DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
      final List<Dependency> dependencies = dependencyFacet.resolveAvailableVersions(frameworkDependency);

      Dependency dependency = getPrompt().promptChoiceTyped("Which version of [" + getFrameworkName() + "] would you like to install?",
            dependencies,
            DependencyUtil.getLatestNonSnapshotVersion(dependencies));

      dependencyFacet.setProperty(getVersionPropertyName(), dependency.getVersion());
      dependencyFacet.addDirectDependency(DependencyBuilder.create(dependency).setVersion(wrap(getVersionPropertyName())));
   }

   private String wrap(String versionPropertyName)
   {
      return "${" + versionPropertyName + "}";
   }

}
