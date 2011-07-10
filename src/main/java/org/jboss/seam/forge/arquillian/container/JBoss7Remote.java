package org.jboss.seam.forge.arquillian.container;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.shell.Shell;

import javax.inject.Inject;

public class JBoss7Remote extends AbstractJBoss7Container
{
   @Inject
   protected JBoss7Remote(Shell shell, Project project, ProfileBuilder builder)
   {
      super(shell, project, builder);
   }

   @Override protected Dependency getContainerDependency()
   {
      return DependencyBuilder.create()
              .setGroupId("org.jboss.as")
              .setArtifactId("jboss-as-arquillian-container-remote")
              .setScopeType(ScopeType.TEST);
   }

   @Override protected String getProfileName()
   {
      return "arq-jbossas-7-remote";
   }
}
