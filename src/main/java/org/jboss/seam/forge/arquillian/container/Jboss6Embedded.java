package org.jboss.seam.forge.arquillian.container;

import org.apache.maven.model.Plugin;
import org.jboss.seam.forge.project.Project;
import org.jboss.seam.forge.project.facets.MavenCoreFacet;

import javax.inject.Inject;

public class Jboss6Embedded implements Container{
    @Inject Project project;

    @Override public void installDependencies() {
        MavenCoreFacet facet = project.getFacet(MavenCoreFacet.class);
        Plugin plugin = new Plugin();

        facet.getPOM().getBuild().addPlugin(plugin);
    }
}
