/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.commandcompleter;

import org.apache.maven.model.Profile;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.shell.completer.SimpleTokenCompleter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ProfileCommandCompleter extends SimpleTokenCompleter
{

   @Inject
   private Project project;

   @Override
   public List<String> getCompletionTokens()
   {
      MavenCoreFacet mavenCoreFacet = project.getFacet(MavenCoreFacet.class);
      List<String> profiles = new ArrayList<String>();
      List<Profile> profileList = mavenCoreFacet.getPOM().getProfiles();
      for (Profile profile : profileList)
      {
         profiles.add(profile.getId());
      }

      Collections.sort(profiles);
      return profiles;
   }
}
