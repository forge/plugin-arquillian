package org.jboss.seam.forge.arquillian.container;

import org.jboss.seam.forge.project.dependencies.DependencyBuilder;
import org.jboss.seam.forge.project.dependencies.ScopeType;

import javax.inject.Inject;
import javax.inject.Named;

public class Jboss5_1Managed implements Container {
    @Inject @Named("arquillianVersion") String arquillianVersion;
    @Inject ProfileBuilder builder;

    @Override public void installDependencies() {
        DependencyBuilder dep1 = DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian.container")
                .setArtifactId("arquillian-jbossas-managed-5.1")
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
                .setVersion("5.0.1.GA")
                .setPackagingType("pom")
                .setScopeType(ScopeType.TEST);

        builder.addProfile("jbossas-managed-5.1", dep1, dep2, dep3);
    }
}
