/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.container.model;

import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class Dependency
{
   private String groupId;
   private String artifactId;
   private String url;

   public String getGroupId()
   {
      return groupId;
   }

   public void setGroupId(String groupId)
   {
      this.groupId = groupId;
   }

   public String getArtifactId()
   {
      return artifactId;
   }

   public void setArtifactId(String artifactId)
   {
      this.artifactId = artifactId;
   }

   public String getUrl()
   {
      return url;
   }

   public void setUrl(String url)
   {
      this.url = url;
   }

   public DependencyBuilder asDependency() {
      return DependencyBuilder.create()
            .setGroupId(getGroupId())
            .setArtifactId(getArtifactId());
   }
}
