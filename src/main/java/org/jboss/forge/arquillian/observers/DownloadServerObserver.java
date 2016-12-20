/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.observers;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.input.UIPrompt;

import javax.inject.Inject;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class DownloadServerObserver {
    @Inject
    Project project;

    @Inject
    UIPrompt shell;

   /*
   public void install(@Observes ContainerInstallEvent event)
   {
      if (event.getContainer().getDownload() != null)
      {
         boolean installContainer = shell.promptBoolean("Do you want Arquillian to install the container?", false);
         if (installContainer)
         {
            installContainer(event.getContainer());
         }
      }
   }


   private void installContainer(Container container)
   {
      MavenFacet mavenCoreFacet = project.getFacet(MavenFacet.class);
      DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
      Model pom = mavenCoreFacet.getModel();
      Profile containerProfile = ProfileBuilder.findProfileById(container.getProfileId(), pom);
      if (containerProfile == null)
      {
         containerProfile = ProfileBuilder.findProfileById(container.getId(), pom);
      }
      if (containerProfile == null)
      {
         throw new RuntimeException("Container profile with id " + container.getId() + " or "
               + container.getProfileId() + " not found");
      }

      List<org.jboss.forge.addon.dependencies.Coordinate> asDependencies = dependencyFacet
            .resolveAvailableVersions(DependencyBuilder.create()
                  .setGroupId(container.getDownload().getGroupId())
                  .setArtifactId(container.getDownload().getArtifactId()));
      org.jboss.forge.addon.dependencies.Coordinate asVersion = shell.promptChoiceTyped(
            "Which version of the container do you want to install?", asDependencies,
            DependencyUtil.getLatestNonSnapshotVersion(asDependencies));

      ConfigurationBuilder configuration = ConfigurationBuilder.create();
      configuration.createConfigurationElement("artifactItems")
            .createConfigurationElement("artifactItem")
            .addChild("groupId").setText(container.getDownload().getGroupId()).getParentElement()
            .addChild("artifactId").setText(container.getDownload().getArtifactId()).getParentElement()
            .addChild("version").setText(asVersion.getVersion()).getParentElement()
            .addChild("type").setText("zip").getParentElement()
            .addChild("overWrite").setText("false").getParentElement()
            .addChild("outputDirectory")
            .setText(shell.prompt("Where do you want to install the container?", String.class, container.getId()));

      MavenPluginBuilder pluginBuilder = MavenPluginBuilder
            .create()
            .setCoordinate(DependencyBuilder.create("org.apache.maven.plugins:maven-dependency-plugin").getCoordinate())
            .addExecution(
                  ExecutionBuilder.create().setId("unpack").setPhase("process-test-classes").addGoal("unpack")
                        .setConfig(configuration));

      BuildBase build = containerProfile.getBuild();
      if (build == null)
      {
         build = new BuildBase();
      }

      build.addPlugin(new MavenPluginAdapter(pluginBuilder));
      containerProfile.setBuild(build);
      pom.removeProfile(containerProfile);
      pom.addProfile(containerProfile);

      mavenCoreFacet.setModel(pom);

   }
   */

}
