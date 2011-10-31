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
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.shell.Shell;

import javax.inject.Inject;
import java.util.List;

public class MavenContainerDownloadPluginInstaller {

    @Inject
    Project project;

    @Inject
    Shell shell;

    public String addPluginAndReturnVersion(String profileName, String location, String groupId, String artifactId) {
        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);

        List<Dependency> asDependencies = dependencyFacet.resolveAvailableVersions(groupId + ":" + artifactId);
        if(asDependencies == null || asDependencies.size() == 0) {
            shell.println("Couldn't find any versions for dependency " + groupId + ":" + artifactId);
        }
        Dependency asVersion = shell.promptChoiceTyped("Which version of " + artifactId + " do you want to install?", asDependencies);

        Profile containerProfile = getContainerProfile(profileName);
        MavenPluginBuilder pluginBuilder = createPlugin(location, groupId, artifactId, asVersion.getVersion());
        savePom(containerProfile, pluginBuilder);

        return asVersion.getVersion();
    }

    private MavenPluginBuilder createPlugin(String location, String groupId, String artifactId, String version) {
        ConfigurationBuilder configuration = ConfigurationBuilder.create();

        configuration.createConfigurationElement("artifactItems")
                .createConfigurationElement("artifactItem")
                .addChild("groupId").setText(groupId).getParentElement()
                .addChild("artifactId").setText(artifactId).getParentElement()
                .addChild("version").setText(version).getParentElement()
                .addChild("type").setText("zip").getParentElement()
                .addChild("overWrite").setText("false").getParentElement()
                .addChild("outputDirectory").setText(location);

        return MavenPluginBuilder.create().setDependency(DependencyBuilder.create("org.apache.maven.plugins:maven-dependency-plugin"))
                .addExecution(ExecutionBuilder.create().setId("unpack").setPhase("process-test-classes").addGoal("unpack")
                        .setConfig(configuration));
    }

    private void savePom(Profile containerProfile, MavenPluginBuilder pluginBuilder) {
        MavenCoreFacet mavenCoreFacet = project.getFacet(MavenCoreFacet.class);
        Model pom = getPom();
        BuildBase build = containerProfile.getBuild();
        if (build == null) {
            build = new BuildBase();
        }

        build.addPlugin(new MavenPluginAdapter(pluginBuilder));
        containerProfile.setBuild(build);
        pom.removeProfile(containerProfile);
        pom.addProfile(containerProfile);

        mavenCoreFacet.setPOM(pom);
    }

    private Profile getContainerProfile(String profileName) {
        Model pom = getPom();
        List<Profile> profiles = pom.getProfiles();

        Profile containerProfile = null;
        for (Profile profile : profiles) {
            if (profile.getId().equals(profileName)) {
                containerProfile = profile;
                break;
            }
        }

        if (containerProfile == null) {
            throw new RuntimeException("Container profile with id " + profileName + " not found");
        }
        return containerProfile;
    }

    private Model getPom() {
        MavenCoreFacet mavenCoreFacet = project.getFacet(MavenCoreFacet.class);
        return mavenCoreFacet.getPOM();
    }
}