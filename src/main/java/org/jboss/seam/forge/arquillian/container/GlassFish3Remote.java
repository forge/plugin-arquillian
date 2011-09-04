package org.jboss.seam.forge.arquillian.container;

import javax.inject.Inject;

import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;

public class GlassFish3Remote implements Container
{
   private static final String GLASSFISH_VERSION = "3.1";
   @Inject
   ProfileBuilder builder;

   @Override
   public void installDependencies(String arquillianVersion)
   {
      DependencyBuilder dep1 = DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian.container")
                .setArtifactId("arquillian-glassfish-remote-3.1")
                .setVersion(arquillianVersion)
                .setScopeType(ScopeType.TEST);

      builder.addProfile("glassfish-remote-3", dep1);
   }

    @Override
    public String installContainer(String location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String installContainerToDefaultLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsContainerInstallation() {
        return false;
    }
}
