package org.jboss.forge.arquillian;

import org.jboss.forge.arquillian.container.Container;
import org.jboss.forge.arquillian.container.Dependency;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.shell.Shell;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ContainerInstaller
{
   @Inject
   ProfileBuilder profileBuilder;

   @Inject
   Shell shell;

   @Inject
   Project project;

   public void installContainer(Container container)
   {
      List<org.jboss.forge.project.dependencies.Dependency> dependencies = new ArrayList<org.jboss.forge.project.dependencies.Dependency>();

      DependencyBuilder containerDependency = DependencyBuilder.create()
            .setGroupId(container.getGroup_id())
            .setArtifactId(container.getArtifact_id());


      dependencies.add(resolveVersion(containerDependency));

      if (container.getDependencies() != null)
      {
         for (Dependency dependency : container.getDependencies())
         {
            DependencyBuilder dependencyBuilder = DependencyBuilder.create()
                  .setGroupId(dependency.getGroup_id())
                  .setArtifactId(dependency.getArtifact_id());
            dependencies.add(resolveVersion(dependencyBuilder));
         }
      }
      profileBuilder.addProfile(container, dependencies);

   }

   private org.jboss.forge.project.dependencies.Dependency resolveVersion(DependencyBuilder containerDependency)
   {
      DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);

      List<org.jboss.forge.project.dependencies.Dependency> versions = dependencyFacet.resolveAvailableVersions(containerDependency);
      return shell.promptChoiceTyped("What version of " + containerDependency.getArtifactId() + " do you want to use?", versions, DependencyUtil.getLatestNonSnapshotVersion(versions));
   }
}
