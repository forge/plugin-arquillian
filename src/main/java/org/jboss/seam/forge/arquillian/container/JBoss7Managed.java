package org.jboss.seam.forge.arquillian.container;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.shell.Shell;

import javax.inject.Inject;

public class JBoss7Managed extends AbstractJBoss7Container {


    @Inject
    MavenContainerDownloadPluginInstaller pluginInstaller;

    @Inject
    public JBoss7Managed(Shell shell, Project project, ProfileBuilder builder) {
        super(shell, project, builder);
    }

    @Override
    protected Dependency getContainerDependency() {
        return DependencyBuilder.create()
                .setGroupId("org.jboss.as")
                .setArtifactId("jboss-as-arquillian-container-managed")
                .setScopeType(ScopeType.TEST);
    }

    @Override
    protected String getProfileName() {
        return "arq-jbossas-7-managed";
    }


    @Override
    public String installContainer(String location) {
        String groupId = "org.jboss.as";
        String artifactId = "jboss-as-dist";

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
