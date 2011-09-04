package org.jboss.seam.forge.arquillian.container;

import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.maven.plugins.ConfigurationBuilder;
import org.jboss.forge.maven.plugins.ExecutionBuilder;
import org.jboss.forge.maven.plugins.MavenPluginAdapter;
import org.jboss.forge.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.shell.Shell;

import javax.inject.Inject;
import java.util.List;

public class JBoss7Managed extends AbstractJBoss7Container {
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
        MavenCoreFacet mavenCoreFacet = project.getFacet(MavenCoreFacet.class);
        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        Model pom = mavenCoreFacet.getPOM();
        List<Profile> profiles = pom.getProfiles();
        Profile containerProfile = null;
        for (Profile profile : profiles) {
            if (profile.getId().equals(getProfileName())) {
                containerProfile = profile;
                break;
            }
        }

        if (containerProfile == null) {
            throw new RuntimeException("Container profile with id " + getProfileName() + " not found");
        }

        List<Dependency> asDependencies = dependencyFacet.resolveAvailableVersions("org.jboss.as:jboss-as-dist");
        Dependency asVersion = shell.promptChoiceTyped("Which version of Jboss AS do you want to install?", asDependencies);

        ConfigurationBuilder configuration = ConfigurationBuilder.create();
        configuration.createConfigurationElement("artifactItems")
                .createConfigurationElement("artifactItem")
                .addChild("groupId").setText("org.jboss.as").getParentElement()
                .addChild("artifactId").setText("jboss-as-dist").getParentElement()
                .addChild("version").setText(asVersion.getVersion()).getParentElement()
                .addChild("type").setText("zip").getParentElement()
                .addChild("overWrite").setText("false").getParentElement()
                .addChild("outputDirectory").setText(location);

        MavenPluginBuilder pluginBuilder = MavenPluginBuilder.create().setDependency(DependencyBuilder.create("org.apache.maven.plugins:maven-dependency-plugin"))
                .addExecution(ExecutionBuilder.create().setId("unpack").setPhase("process-test-classes").addGoal("unpack")
                        .setConfig(configuration));

        BuildBase build = containerProfile.getBuild();
        if (build == null) {
            build = new BuildBase();
        }

        build.addPlugin(new MavenPluginAdapter(pluginBuilder));
        containerProfile.setBuild(build);
        pom.removeProfile(containerProfile);
        pom.addProfile(containerProfile);

        mavenCoreFacet.setPOM(pom);

        return location + "/jboss-as-" + asVersion.getVersion();
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
