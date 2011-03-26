package org.jboss.seam.forge.arquillian.container;

import org.jboss.seam.forge.project.dependencies.DependencyBuilder;

import javax.inject.Inject;
import javax.inject.Named;

public class GlassFish3Embedded implements Container {
    private static final String GLASSFISH_VERSION = "3.1";
    @Inject ProfileBuilder builder;
    @Inject @Named("arquillianVersion") String arquillianVersion;

    @Override public void installDependencies() {
        DependencyBuilder dep1 = DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian.container")
                .setArtifactId("arquillian-glassfish-embedded-3.1")
                .setVersion(arquillianVersion);

        DependencyBuilder dep2 = DependencyBuilder.create()
                .setGroupId("org.glassfish.extras")
                .setArtifactId("glassfish-embedded-all")
                .setVersion(GLASSFISH_VERSION);

        builder.addProfile("glassfish-embedded-3", dep1, dep2);
    }
}
