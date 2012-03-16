package org.jboss.forge.arquillian;

import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.maven.dependencies.MavenDependencyAdapter;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;

import java.util.List;

public class ProfileBuilder
{

   @Inject
   Project project;

    public void addProfile(String profileId, List<Dependency> dependencies) {
        Dependency[] deps = new Dependency[dependencies.size()];
        addProfile(profileId, dependencies.toArray(deps));
    }

   public void addProfile(String profileId, Dependency... dependencies)
   {
      MavenCoreFacet facet = project.getFacet(MavenCoreFacet.class);


      Profile profile = new Profile();
      profile.setId(profileId);

      for (Dependency dependency : dependencies)
      {
          profile.addDependency(new MavenDependencyAdapter(DependencyBuilder.create(dependency)));
      }

      Model pom = facet.getPOM();
      Profile existingProfile = findProfileById(profileId, pom);
      if(existingProfile != null)
      {
         pom.removeProfile(existingProfile);
      }
      pom.addProfile(profile);
       
      facet.setPOM(pom);
   }

   private Profile findProfileById(String profileId, Model pom)
   {
      for (Profile profile : pom.getProfiles())
      {
         if (profileId.equalsIgnoreCase(profile.getId()))
         {
            return profile;
         }
      }
      return null;
   }
}
