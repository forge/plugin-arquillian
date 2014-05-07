/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.container;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.arquillian.container.model.Container;
import org.jboss.forge.arquillian.container.model.Dependency;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ContainerInstaller
{
   @Inject
   private ProfileManager profileManager;

   public void installContainer(Container container, String version)
   {
      List<org.jboss.forge.addon.dependencies.Dependency> dependencies = new ArrayList<org.jboss.forge.addon.dependencies.Dependency>();
      
      DependencyBuilder containerDependency = container.asDependency()
                .setVersion(version);

      dependencies.add(containerDependency);
      
      if (container.getDependencies() != null)
      {
         for (Dependency dependency : container.getDependencies())
         {
            DependencyBuilder dependencyBuilder = DependencyBuilder.create()
                  .setGroupId(dependency.getGroupId())
                  .setArtifactId(dependency.getArtifactId());
            // TODO: support nested deps
            //dependencies.add(resolveVersion(dependencyBuilder));
         }
      }
      profileManager.addProfile(container, dependencies);
   }

   /*
   private org.jboss.forge.addon.dependencies.Dependency resolveVersion(DependencyBuilder containerDependency)
   {
      DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);

      List<org.jboss.forge.addon.dependencies.Coordinate> versions = dependencyFacet.resolveAvailableVersions(containerDependency);
      return shell.promptChoiceTyped(
            "What version of " + containerDependency.getCoordinate().getArtifactId() + " do you want to use?",
            versions,
            DependencyUtil.getLatestNonSnapshotVersion(versions));
   }
   */
}
