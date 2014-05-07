/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.api;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.facets.constraints.FacetConstraints;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.MetadataFacet;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
@FacetConstraints({
   @FacetConstraint(DependencyFacet.class),
   @FacetConstraint(MetadataFacet.class),
   @FacetConstraint(ArquillianFacet.class)
})
public abstract class TestFrameworkFacet extends AbstractVersionedFacet {

   public abstract String getTemplateName();

   public abstract String getFrameworkName();

   public abstract String getVersionPropertyName();

   public abstract DependencyBuilder createFrameworkDependency();

   public abstract DependencyBuilder createArquillianDependency();

   @Override
   public boolean install() {
      if(getVersion() != null) {
         installDependencies();
         return true;
      }
      return false;
   }

   @Override
   public boolean isInstalled() {
      return hasEffectiveDependency(createFrameworkDependency())
             && hasEffectiveDependency(createArquillianDependency());
   }

   @Override
   public boolean uninstall() {
      return false;
   }
   
   @Override
   protected Coordinate getVersionedCoordinate() {
      return createFrameworkDependency().getCoordinate();
   }

   protected void installDependencies() {
      installArquillianDependency(createArquillianDependency());
      installFrameworkDependency(createFrameworkDependency());
   }

   private boolean hasEffectiveDependency(DependencyBuilder frameworkDependency)
   {
      final DependencyFacet dependencyFacet = getFaceted().getFacet(DependencyFacet.class);
      return dependencyFacet.hasEffectiveDependency(frameworkDependency);
   }

   protected void installArquillianDependency(DependencyBuilder arquillianDependency) {
      if (hasEffectiveDependency(arquillianDependency)) {
         return;
      }
      final DependencyFacet dependencyFacet = getFaceted().getFacet(DependencyFacet.class);

      if(arquillianDependency != null) {
         dependencyFacet.addDirectDependency(arquillianDependency);
      }
   }
   
   protected void installFrameworkDependency(DependencyBuilder frameworkDependency) {
      if (hasEffectiveDependency(frameworkDependency)) {
         return;
      }
      
      final DependencyFacet dependencyFacet = getFaceted().getFacet(DependencyFacet.class);
      final MetadataFacet metadataFacet = getFaceted().getFacet(MetadataFacet.class);

      metadataFacet.setDirectProperty(getVersionPropertyName(), getVersion());
      dependencyFacet.addDirectDependency(frameworkDependency.setVersion(wrap(getVersionPropertyName())));
   }

   private String wrap(String versionPropertyName)
   {
      return "${" + versionPropertyName + "}";
   }
}
