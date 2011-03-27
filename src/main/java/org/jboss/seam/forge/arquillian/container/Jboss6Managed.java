package org.jboss.seam.forge.arquillian.container;

import org.jboss.seam.forge.project.Project;
import org.jboss.seam.forge.project.dependencies.DependencyBuilder;

import javax.inject.Inject;
import javax.inject.Named;

public class Jboss6Managed implements Container {
    @Inject @Named("arquillianVersion") String arquillianVersion;
    @Inject Project project;
    @Inject ProfileBuilder builder;


    @Override public void installDependencies() {
        DependencyBuilder dep1 = DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian.container")
                .setArtifactId("arquillian-jbossas-managed-6")
                .setVersion(arquillianVersion);

        DependencyBuilder dep2 = DependencyBuilder.create()
                .setGroupId("org.jboss.jbossas")
                .setArtifactId("jboss-server-manager")
                .setVersion("1.0.3.GA");

        DependencyBuilder dep3 = DependencyBuilder.create()
                .setGroupId("org.jboss.jbossas")
                .setArtifactId("jboss-server-manager")
                .setVersion("6.0.0.Final")
                .setPackagingType("pom");
        builder.addProfile("jbossas-managed-6", dep1, dep2, dep3);
    }
}
