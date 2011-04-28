package org.jboss.seam.forge.arquillian;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.spec.javaee.CDIFacet;

@Alias("forge.arquillian")
@RequiresFacet(CDIFacet.class)
public class ArquillianFacet extends BaseFacet
{

   @Produces
   @Named("arquillianVersion")
   String arquillianVersion = "1.0.0.Alpha5";

   @Override
   public boolean install()
   {
      if (!isInstalled())
      {

         DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);

         DependencyBuilder arquillianDependency = createArquillianDependency();
         if (!dependencyFacet.hasDependency(arquillianDependency))
         {
            dependencyFacet.addDependency(arquillianDependency);
         }
      }

      return true;
   }

   private DependencyBuilder createArquillianDependency()
   {
      DependencyBuilder dependencyBuilder = DependencyBuilder.create()
                .setGroupId("org.jboss.arquillian")
                .setArtifactId("arquillian-api")
                .setVersion(arquillianVersion)
                .setScopeType(ScopeType.TEST);
      return dependencyBuilder;
   }

   @Override
   public boolean isInstalled()
   {
      if(!project.hasFacet(DependencyFacet.class))
      {
         return false;
      }
      DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
      return dependencyFacet.hasDependency(createArquillianDependency());
   }
}
