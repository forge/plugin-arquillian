package org.jboss.seam.forge.arquillian.container;

import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;

public class Jboss6Managed implements Container
{
   @Inject
   @Named("arquillianVersion")
   String arquillianVersion;
   @Inject
   Project project;
   @Inject
   ProfileBuilder builder;

   @Override
   public void installDependencies()
   {
      DependencyBuilder dep1 = DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian.container")
                .setArtifactId("arquillian-jbossas-managed-6")
                .setVersion(arquillianVersion)
                .setScopeType(ScopeType.TEST);

      DependencyBuilder dep2 = DependencyBuilder.create()
                .setGroupId("org.jboss.jbossas")
                .setArtifactId("jboss-server-manager")
                .setVersion("1.0.3.GA")
                .setScopeType(ScopeType.TEST);

      DependencyBuilder dep3 = DependencyBuilder.create()
                .setGroupId("org.jboss.jbossas")
                .setArtifactId("jboss-as-client")
                .setVersion("6.0.0.Final")
                .setPackagingType("pom")
                .setScopeType(ScopeType.TEST);

      builder.addProfile("jbossas-managed-6", dep1, dep2, dep3);
   }
}
