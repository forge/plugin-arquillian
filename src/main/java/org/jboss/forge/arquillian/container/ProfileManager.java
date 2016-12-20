package org.jboss.forge.arquillian.container;

import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.dependencies.MavenDependencyAdapter;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.arquillian.container.model.Container;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {

   @Inject
   private ContainerResolver containerResolver;
   
   public List<Profile> getArquillianProfiles(Project project) {
      MavenFacet mavenCoreFacet = project.getFacet(MavenFacet.class);
      List<Profile> profiles = new ArrayList<>();
      List<Profile> profileList = mavenCoreFacet.getModel().getProfiles();
      for (Profile profile : profileList)
      {
         profiles.add(profile);
      }

      profiles.sort((o1, o2) -> o1.getId().compareTo(o2.getId()));
      return profiles;
   }
   
   public void addProfile(Project project, Container container, List<Dependency> dependencies) {
      Dependency[] deps = new Dependency[dependencies.size()];
      addProfile(project, container, dependencies.toArray(deps));
   }

   public void addProfile(Project project, Container container, Dependency... dependencies) {
      MavenFacet facet = project.getFacet(MavenFacet.class);


      Profile profile = new Profile();
      profile.setId(container.getProfileId());

      /*
       * Create the surefire plugin configuration, so we call the relevant Arquillian container config
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
      surefirePlugin.setConfiguration(buildConfiguration(container.getProfileId()));
      surefirePlugin.setVersion("2.14.1");

      BuildBase buildBase = new BuildBase();
      buildBase.addPlugin(surefirePlugin);

      profile.setBuild(buildBase);

      for (Dependency dependency : dependencies)
      {
         profile.addDependency(new MavenDependencyAdapter(DependencyBuilder.create(dependency)));
      }

      Model pom = facet.getModel();
      Profile existingProfile = findProfileById(container.getProfileId(), pom);
      if (existingProfile != null) {
         // preserve existing id
         profile.setId(existingProfile.getId());
         pom.removeProfile(existingProfile);
      }
      pom.addProfile(profile);

      facet.setModel(pom);
   }
   
   public Container getContainer(Profile profile) {
      String profileId = profile.getId().replaceFirst("^arq-", "arquillian-");
      for (Container container : containerResolver.getContainers()) {
         if (container.getProfileId().equals(profileId)) {
            return container;
         }
      }
      throw new RuntimeException("Container not found for profile " + profile);
   }


   private Profile findProfileById(String profileId, Model pom) {
      for (Profile profile : pom.getProfiles()) {
         if (profileId.equalsIgnoreCase(profile.getId().replaceFirst("^arq-", "arquillian-"))) {
            return profile;
         }
      }
      return null;
   }

   private Object buildConfiguration(String profileId) {
       try {
          return Xpp3DomBuilder.build(new StringReader(
                   "<configuration>\n" +
                   "    <systemPropertyVariables>\n" +
                   "        <arquillian.launch>" + profileId + "</arquillian.launch>\n" +
                   "    </systemPropertyVariables>\n" +
                   "</configuration>"));
       }
       catch (XmlPullParserException | IOException e) {
          throw new IllegalStateException(e);
       }
   }
}
