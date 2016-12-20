/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.arquillian.container.index.ContainerDirectoryParser;
import org.jboss.forge.arquillian.container.model.Container;
import org.jboss.forge.arquillian.container.model.ContainerType;


/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ContainerResolver 
{
   @Inject
   private ContainerDirectoryParser parser;

   public Iterable<Container> getContainers() {
      return getContainers(null);
   }

   public Iterable<Container> getContainers(ContainerType type) {
      List<Container> availableContainers = null;
      try {
         availableContainers = new ArrayList<>(parser.getContainers());
         Collections.sort(availableContainers);

         if (type == null) {
            return availableContainers;
         }

         return filterByType(availableContainers, type);
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private Iterable<Container> filterByType(List<Container> containers, ContainerType containerType) {
      List<Container> filtered = new ArrayList<>();
      for (Container container : containers) {
         if (containerType.equals(container.getContainerType())) {
            filtered.add(container);
         }
      }
      return filtered;
   }
}
