package test.integration;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Profile;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jboss.arquillian.api.Deployment;
import org.jboss.forge.Root;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.forge.arquillian.*;
import org.jboss.forge.arquillian.container.Container;
import org.jboss.seam.render.RenderRoot;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.solder.SolderRoot;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class PluginTest extends AbstractShellTest {
    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackages(true, Root.class.getPackage())
                .addPackages(true, RenderRoot.class.getPackage())
                .addPackages(true, SolderRoot.class.getPackage())
                .addPackages(true, ArquillianPlugin.class.getPackage(), Container.class.getPackage())
                .addManifestResource(new ByteArrayAsset("<beans><alternatives><class>org.jboss.forge.arquillian.container.MockContainerDirectoryLocationProvider</class></alternatives></beans>".getBytes()), ArchivePaths.create("beans.xml"));
    }

    @Test
    public void installContainer() throws Exception {
        Project project = initializeJavaProject();

        MavenCoreFacet coreFacet = project.getFacet(MavenCoreFacet.class);

        List<Profile> profiles = coreFacet.getPOM().getProfiles();
        for (Profile profile : profiles) {
            System.out.println(profile.getId());
        }
        assertThat(profiles.size(), is(0));

        queueInputLines("TOMCAT_EMBEDDED_6.X", "", "19", "10", "8");
        getShell().execute("arquillian setup");

        assertThat(coreFacet.getPOM().getProfiles().size(), is(1));
        Profile profile = coreFacet.getPOM().getProfiles().get(0);

        assertThat(profile.getDependencies(), hasItems(
                new DependencyMatcher("arquillian-tomcat-embedded-6"),
                new DependencyMatcher("catalina"),
                new DependencyMatcher("coyote"),
                new DependencyMatcher("jasper")));
    }

    @Test
    public void installContainerWithDownload() throws Exception {
        Project project = initializeJavaProject();

        MavenCoreFacet coreFacet = project.getFacet(MavenCoreFacet.class);

        List<Profile> profiles = coreFacet.getPOM().getProfiles();
        for (Profile profile : profiles) {
            System.out.println(profile.getId());
        }
        assertThat(profiles.size(), is(0));

        queueInputLines("JBOSS_AS_MANAGED_4.2.X", "", "19", "10", "", "", "", "", "y","");
        getShell().execute("arquillian setup");

        assertThat(coreFacet.getPOM().getProfiles().size(), is(1));
        Profile profile = coreFacet.getPOM().getProfiles().get(0);

        assertThat(profile.getDependencies(), hasItems(
                new DependencyMatcher("arquillian-jbossas-managed-4.2"),
                new DependencyMatcher("jboss-server-manager"),
                new DependencyMatcher("dom4j"),
                new DependencyMatcher("jbossall-client")));

        assertThat(profile.getBuild().getPlugins().size(), is(1));
        assertThat(profile.getBuild().getPlugins().get(0).getArtifactId(), is("maven-dependency-plugin"));

    }

    @Test
    public void configureContainer() throws Exception {
        Project project = initializeJavaProject();

        MavenCoreFacet coreFacet = project.getFacet(MavenCoreFacet.class);

        List<Profile> profiles = coreFacet.getPOM().getProfiles();
        for (Profile profile : profiles) {
            System.out.println(profile.getId());
        }
        assertThat(profiles.size(), is(0));

        queueInputLines("JBOSS_AS_MANAGED_6.X", "", "19", "10", "", "", "", "8","");
        getShell().execute("arquillian setup");

        queueInputLines("JBOSS_AS_MANAGED_6.X", "2", "8000");
        getShell().execute("arquillian configure-container");

        ResourceFacet facet = project.getFacet(ResourceFacet.class);
        FileResource<?> arquillianXML = facet.getTestResource("arquillian.xml");
        
        assertThat(arquillianXML, is(notNullValue()));
        assertThat(arquillianXML.exists(), is(true));


    }

    class DependencyMatcher extends BaseMatcher<Dependency> {
        private String artifactId;

        public DependencyMatcher(String artifactId) {
            this.artifactId = artifactId;
        }

        @Override
        public boolean matches(Object o) {
            Dependency d = (Dependency) o;
            return d.getArtifactId().equals(artifactId);
        }

        @Override
        public void describeTo(Description description) {
        }
    }
}
