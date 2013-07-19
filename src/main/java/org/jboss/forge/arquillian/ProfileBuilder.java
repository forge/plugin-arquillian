package org.jboss.forge.arquillian;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.arquillian.container.Container;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.maven.dependencies.MavenDependencyAdapter;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;

public class ProfileBuilder
{

   @Inject
   Project project;

    public void addProfile(Container container, List<Dependency> dependencies) {
        Dependency[] deps = new Dependency[dependencies.size()];
        addProfile(container, dependencies.toArray(deps));
    }

   public void addProfile(Container container, Dependency... dependencies)
   {
      MavenCoreFacet facet = project.getFacet(MavenCoreFacet.class);


      Profile profile = new Profile();
      profile.setId(container.getProfileId());

      /*
       * Create the surefire plugin configuration, so we call the relevant Arqullina container config
       * 
       *  <plugin>
       *      <artifactId>maven-surefire-plugin</artifactId>
       *      <configuration>
       *          <systemPropertyVariables>
       *              <arquillian.launch>${profileId}</arquillian.launch>
       *          </systemPropertyVariables>
       *      </configuration>
       * </plugin>
       * 
       */
      
      Plugin surefirePlugin = new Plugin();
      surefirePlugin.setArtifactId("maven-surefire-plugin");
      surefirePlugin.setConfiguration(buildConfiguration(container.getId()));
      surefirePlugin.setVersion("2.14.1");
      
      BuildBase buildBase = new BuildBase();
      buildBase.addPlugin(surefirePlugin);
      
      profile.setBuild(buildBase);
      
      for (Dependency dependency : dependencies)
      {
          profile.addDependency(new MavenDependencyAdapter(DependencyBuilder.create(dependency)));
      }

      Model pom = facet.getPOM();
      Profile existingProfile = findProfileById(container.getProfileId(), pom);
      if(existingProfile != null)
      {
         // preserve existing id
         profile.setId(existingProfile.getId());
         pom.removeProfile(existingProfile);
      }
      pom.addProfile(profile);
       
      facet.setPOM(pom);
   }
   
   private Object buildConfiguration(String profileId) {
       try {
        return Xpp3DomBuilder.build(new StringReader(
                   "<configuration>\n" +
                   "    <systemPropertyVariables>\n" +
                   "        <arquillian.launch>" + profileId + "</arquillian.launch>\n" +
                   "    </systemPropertyVariables>\n" +
                   "</configuration>"));
    } catch (XmlPullParserException e) {
        throw new IllegalStateException(e);
    } catch (IOException e) {
        throw new java.lang.IllegalStateException(e);
    }
   }

   public static Profile findProfileById(String profileId, Model pom)
   {
      for (Profile profile : pom.getProfiles())
      {
         if (profileId.equalsIgnoreCase(profile.getId().replaceFirst("^arq-", "arquillian-")))
         {
            return profile;
         }
      }
      return null;
   }
}
