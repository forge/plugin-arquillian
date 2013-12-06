/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.installobservers;

import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.jboss.forge.arquillian.ContainerInstallEvent;
import org.jboss.forge.arquillian.DependencyUtil;
import org.jboss.forge.arquillian.ProfileBuilder;
import org.jboss.forge.arquillian.container.Container;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.maven.plugins.ConfigurationBuilder;
import org.jboss.forge.maven.plugins.ExecutionBuilder;
import org.jboss.forge.maven.plugins.MavenPluginAdapter;
import org.jboss.forge.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.shell.Shell;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class DownloadServerObserver
{
   @Inject
   Project project;

   @Inject
   Shell shell;

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
      MavenCoreFacet mavenCoreFacet = project.getFacet(MavenCoreFacet.class);
      DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
      Model pom = mavenCoreFacet.getPOM();
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

      List<org.jboss.forge.project.dependencies.Dependency> asDependencies = dependencyFacet
            .resolveAvailableVersions(DependencyBuilder.create()
                  .setGroupId(container.getDownload().getGroup_id())
                  .setArtifactId(container.getDownload().getArtifact_id()));
      org.jboss.forge.project.dependencies.Dependency asVersion = shell.promptChoiceTyped(
            "Which version of the container do you want to install?", asDependencies,
            DependencyUtil.getLatestNonSnapshotVersion(asDependencies));

      ConfigurationBuilder configuration = ConfigurationBuilder.create();
      configuration.createConfigurationElement("artifactItems")
            .createConfigurationElement("artifactItem")
            .addChild("groupId").setText(container.getDownload().getGroup_id()).getParentElement()
            .addChild("artifactId").setText(container.getDownload().getArtifact_id()).getParentElement()
            .addChild("version").setText(asVersion.getVersion()).getParentElement()
            .addChild("type").setText("zip").getParentElement()
            .addChild("overWrite").setText("false").getParentElement()
            .addChild("outputDirectory")
            .setText(shell.prompt("Where do you want to install the container?", String.class, container.getId()));

      MavenPluginBuilder pluginBuilder = MavenPluginBuilder
            .create()
            .setDependency(DependencyBuilder.create("org.apache.maven.plugins:maven-dependency-plugin"))
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

      mavenCoreFacet.setPOM(pom);

   }

}
