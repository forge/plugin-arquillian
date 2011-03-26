package org.jboss.seam.forge.arquillian.container;

import org.jboss.seam.forge.project.dependencies.DependencyBuilder;

import javax.inject.Inject;
import javax.inject.Named;

public class Jboss5Remote implements Container {
    @Inject @Named("arquillianVersion") String arquillianVersion;
    @Inject ProfileBuilder builder;

    @Override public void installDependencies() {
        DependencyBuilder dep1 = DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian.container")
                .setArtifactId("arquillian-jbossas-remote-5")
                .setVersion(arquillianVersion);

        DependencyBuilder dep2 = DependencyBuilder.create()
                .setGroupId("org.jboss.jbossas")
                .setArtifactId("jboss-as-client")
                .setVersion("5.0.1.GA")
                .setPackagingType("pom");
        builder.addProfile("jbossas-remote-5", dep1, dep2);
    }
}
