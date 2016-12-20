/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.arquillian.container.model.Container;
import org.jboss.forge.arquillian.container.model.Dependency;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ContainerInstaller
{
   @Inject
   private ProfileManager profileManager;

   public void installContainer(Project project, Container container, String version, Map<Dependency, String> dependencies)
   {
      List<org.jboss.forge.addon.dependencies.Dependency> allDependencies = new ArrayList<>();
      
      DependencyBuilder containerDependency = container.asDependency()
                .setVersion(version)
                .setScopeType("test");
      allDependencies.add(containerDependency);

      if(dependencies != null) {
         for(Map.Entry<Dependency, String> dependencyEntry: dependencies.entrySet()) {
            allDependencies.add(
                  DependencyBuilder.create(
                        dependencyEntry.getKey().asDependency()
                           .setVersion(dependencyEntry.getValue())
                           .setScopeType("test")));
         }
      }
      profileManager.addProfile(project, container, allDependencies);
   }
}
