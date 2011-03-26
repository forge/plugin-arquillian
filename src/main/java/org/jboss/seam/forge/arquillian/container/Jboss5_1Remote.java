package org.jboss.seam.forge.arquillian.container;

import org.jboss.seam.forge.arquillian.container.Container;
import org.jboss.seam.forge.arquillian.container.ProfileBuilder;
import org.jboss.seam.forge.project.dependencies.DependencyBuilder;

import javax.inject.Inject;
import javax.inject.Named;

public class Jboss5_1Remote implements Container {
    @Inject @Named("arquillianVersion") String arquillianVersion;
    @Inject ProfileBuilder builder;

    @Override public void installDependencies() {
        DependencyBuilder dep1 = DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian.container")
                .setArtifactId("arquillian-jbossas-remote-5.1")
                .setVersion(arquillianVersion);

        DependencyBuilder dep2 = DependencyBuilder.create()
                .setGroupId("org.jboss.jbossas")
                .setArtifactId("jboss-as-client")
                .setVersion("5.1.0.GA")
                .setPackagingType("pom");
        builder.addProfile("jbossas-remote-5.1", dep1, dep2);
    }
}
