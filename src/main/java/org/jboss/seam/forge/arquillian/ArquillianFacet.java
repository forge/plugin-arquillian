package org.jboss.seam.forge.arquillian;

import org.jboss.seam.forge.project.dependencies.DependencyBuilder;
import org.jboss.seam.forge.project.facets.BaseFacet;
import org.jboss.seam.forge.project.facets.DependencyFacet;
import org.jboss.seam.forge.shell.plugins.Alias;
import org.jboss.seam.forge.shell.plugins.RequiresFacet;
import org.jboss.seam.forge.spec.cdi.CDIFacet;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

@Alias("forge.arquillian")
@RequiresFacet(CDIFacet.class)
public class ArquillianFacet extends BaseFacet {

    @Produces @Named("arquillianVersion") String arquillianVersion = "1.0.0.Alpha5";

    @Override public boolean install() {
        if (!isInstalled()) {


            DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);

            DependencyBuilder arquillianDependency = createArquillianDependency();
            if (!dependencyFacet.hasDependency(arquillianDependency)) {
                dependencyFacet.addDependency(arquillianDependency);
            }
        }

        return true;
    }

    private DependencyBuilder createArquillianDependency() {
        DependencyBuilder dependencyBuilder = DependencyBuilder.create()
            .setGroupId("org.jboss.arquillian")
            .setArtifactId("arquillian-api")
            .setVersion(arquillianVersion);
        return dependencyBuilder;
    }





    @Override public boolean isInstalled() {
        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        return dependencyFacet.hasDependency(createArquillianDependency());
    }
}
