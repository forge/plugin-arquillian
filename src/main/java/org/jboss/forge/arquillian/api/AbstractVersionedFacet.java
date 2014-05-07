package org.jboss.forge.arquillian.api;

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.arquillian.util.DependencyUtil;

abstract class AbstractVersionedFacet extends AbstractFacet<Project> implements ProjectFacet {

   @Inject
   private DependencyResolver resolver;

   private String version;
   
   public void setVersion(String version) {
      this.version = version;
   }

   public String getVersion() {
      return version;
   }

   public boolean install(String version) {
      setVersion(version);
      return install();
   }

   public String getDefaultVersion() {
      return DependencyUtil.getLatestNonSnapshotVersion(getAvailableVersions());
   }

   public List<String> getAvailableVersions() {
      return DependencyUtil.toVersionString(
            resolver.resolveVersions(
                  DependencyQueryBuilder.create(getVersionedCoordinate())));
   }

   protected abstract Coordinate getVersionedCoordinate();
}
