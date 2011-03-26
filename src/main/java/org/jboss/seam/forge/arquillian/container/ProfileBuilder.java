package org.jboss.seam.forge.arquillian.container;

import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.jboss.seam.forge.project.Project;
import org.jboss.seam.forge.project.dependencies.Dependency;
import org.jboss.seam.forge.project.dependencies.DependencyBuilder;
import org.jboss.seam.forge.project.dependencies.MavenDependencyAdapter;
import org.jboss.seam.forge.project.facets.MavenCoreFacet;

import javax.inject.Inject;

public class ProfileBuilder {

    @Inject Project project;

    public void addProfile(String profileId, Dependency... dependencies) {
        MavenCoreFacet facet = project.getFacet(MavenCoreFacet.class);

        Profile profile = new Profile();
        profile.setId(profileId);

        for (Dependency dependency : dependencies) {
            profile.addDependency(new MavenDependencyAdapter(DependencyBuilder.create(dependency)));
        }

        Model pom = facet.getPOM();
        pom.addProfile(profile);
        facet.setPOM(pom);
    }
}
