/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package test.integration.util;

import org.apache.maven.model.Dependency;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class DependencyMatcher extends BaseMatcher<Dependency>
{
   private final String artifactId;

   public DependencyMatcher(final String artifactId)
   {
      this.artifactId = artifactId;
   }

   @Override
   public boolean matches(final Object o)
   {
      Dependency d = (Dependency) o;
      return d.getArtifactId().equals(artifactId);
   }

   @Override
   public void describeTo(final Description description)
   {
   }
}
