package org.jboss.seam.forge.arquillian.container;

import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.shell.Shell;

import java.util.List;

public class Jboss6Managed implements Container {
    @Inject
    Project project;
    @Inject
    ProfileBuilder builder;

    @Inject
    Shell shell;

    @Inject
    MavenContainerDownloadPluginInstaller pluginInstaller;

    @Override
    public void installDependencies(String arquillianVersion) {
        DependencyBuilder dep1 = DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian.container")
                .setArtifactId("arquillian-jbossas-managed-6")
                .setScopeType(ScopeType.TEST);

        setVersion(dep1);

        DependencyBuilder dep2 = DependencyBuilder.create()
                .setGroupId("org.jboss.jbossas")
                .setArtifactId("jboss-server-manager")
                .setScopeType(ScopeType.TEST);

        setVersion(dep2);


        DependencyBuilder dep3 = DependencyBuilder.create()
                .setGroupId("org.jboss.jbossas")
                .setArtifactId("jboss-as-client")
                .setPackagingType("pom")
                .setScopeType(ScopeType.TEST);

        setVersion(dep3 );

        builder.addProfile(getProfileName(), dep1, dep2, dep3);
        if (shell.promptBoolean("Do you want to automatically download and install JBoss AS?")) {
            installContainerToDefaultLocation();
        }
    }

    private void setVersion(DependencyBuilder dependency) {
        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        List<Dependency> dep1Versions = dependencyFacet.resolveAvailableVersions(dependency);
        dependency.setVersion(shell.promptChoiceTyped("Which version of " + dependency.getGroupId() + ":" + dependency.getArtifactId() +" do you want to install?", dep1Versions).getVersion());
    }

    private String getProfileName() {
        return "jbossas-managed-6";
    }

    @Override
    public String installContainer(String location) {
        String groupId = "org.jboss.jbossas";
        String artifactId = "jboss-as-distribution";

        String asVersion = pluginInstaller.addPluginAndReturnVersion(getProfileName(), location, groupId, artifactId);

        return location + "/jboss-as-" + asVersion;

    }

    @Override
    public String installContainerToDefaultLocation() {
        return installContainer("target");
    }

    @Override
    public boolean supportsContainerInstallation() {
        return true;
    }
}
