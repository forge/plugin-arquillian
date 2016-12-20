package org.jboss.forge.arquillian.api;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.facets.constraints.FacetConstraints;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.resource.FileResource;

@FacetConstraints({
        @FacetConstraint(DependencyFacet.class),
        @FacetConstraint(MetadataFacet.class),
        @FacetConstraint(ProjectFacet.class),
        @FacetConstraint(ResourcesFacet.class)
})
public class ArquillianFacet extends AbstractVersionedFacet {

    public static final String ARQ_FILENAME_XML = "arquillian.xml";

    public static final String ARQ_CORE_VERSION_PROP_NAME = "version.arquillian_core";
    public static final String ARQ_CORE_VERSION_PROP = "${" + ARQ_CORE_VERSION_PROP_NAME + "}";


    private DependencyBuilder createBOM() {
        return DependencyBuilder.create().setGroupId("org.jboss.arquillian")
                .setArtifactId("arquillian-bom").setPackaging("pom").setScopeType("import");
    }

    @Override
    protected Coordinate getVersionedCoordinate() {
        return createBOM().getCoordinate();
    }

    @Override
    public boolean install() {
        if (getVersion() != null) {
            installArquillianBom(getVersion());
            return true;
        }
        return false;
    }

    @Override
    public boolean isInstalled() {
        return isBOMInstalled();
    }

    public ArquillianConfig getConfig() {
        FileResource<?> resource = getArquillianXMLResource();
        if (!resource.exists()) {
            return new ArquillianConfig();
        } else {
            return new ArquillianConfig(resource.getResourceInputStream());
        }
    }

    public void setConfig(ArquillianConfig xml) {
        getArquillianXMLResource().setContents(xml.toString());
    }

    private FileResource<?> getArquillianXMLResource() {
        ResourcesFacet resources = getFaceted().getFacet(ResourcesFacet.class);
        return resources.getTestResource(ARQ_FILENAME_XML);
    }

    private void installArquillianBom(String version) {
        DependencyFacet dependencyFacet = getFaceted().getFacet(DependencyFacet.class);
        MetadataFacet metadataFacet = getFaceted().getFacet(MetadataFacet.class);

        String installedVersion = metadataFacet.getDirectProperty(ARQ_CORE_VERSION_PROP_NAME);
        if (installedVersion == null || !installedVersion.equals(version)) {
            metadataFacet.setDirectProperty(ARQ_CORE_VERSION_PROP_NAME, version);
        }

        // need to set version after resolve is done, else nothing will resolve.
        if (!isBOMInstalled()) {
            dependencyFacet.addDirectManagedDependency(
                    createBOM().setVersion(ARQ_CORE_VERSION_PROP)
            );
        }
    }

    private boolean isBOMInstalled() {
        DependencyFacet dependencyFacet = getFaceted().getFacet(DependencyFacet.class);
        // need to set version after resolve is done, else nothing will resolve.
        return dependencyFacet.hasDirectManagedDependency(createBOM());
    }
}
