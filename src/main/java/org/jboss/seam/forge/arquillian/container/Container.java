package org.jboss.seam.forge.arquillian.container;

public interface Container {
    void installDependencies(String arquillianVersion);

    String installContainer(String location);

    String installContainerToDefaultLocation();

    boolean supportsContainerInstallation();
}
