package org.jboss.seam.forge.arquillian.container;

import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;

public class GlassFish3Embedded implements Container
{
   private static final String GLASSFISH_VERSION = "3.1";
   @Inject
   ProfileBuilder builder;


   @Override
   public void installDependencies(String arquillianVersion)
   {
      DependencyBuilder dep1 = DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian.container")
                .setArtifactId("arquillian-glassfish-embedded-3.1")
                .setVersion(arquillianVersion)
                .setScopeType(ScopeType.TEST);

      DependencyBuilder dep2 = DependencyBuilder.create()
                .setGroupId("org.glassfish.extras")
                .setArtifactId("glassfish-embedded-all")
                .setVersion(GLASSFISH_VERSION)
                .setScopeType(ScopeType.TEST);

      builder.addProfile("glassfish-embedded-3", dep1, dep2);
   }
}
