/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.container;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.type.TypeReference;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
@Singleton
public class ContainerDirectoryParser
{
   private List<Container> containers;

   @Inject
   private ContainerDirectoryLocationProvider containerDirectoryLocationProvider;

   @PostConstruct
   void parse() throws IOException
   {
      final ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
      List<Container> parsedContainers = objectMapper.readValue(containerDirectoryLocationProvider.getUrl(),
            new TypeReference<List<Container>>() {});

      this.containers = Collections.unmodifiableList(parsedContainers);
   }

   public List<Container> getContainers() throws IOException
   {
      return containers;
   }
}
