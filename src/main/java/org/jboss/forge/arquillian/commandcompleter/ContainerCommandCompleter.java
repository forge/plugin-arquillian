/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.commandcompleter;

import org.jboss.forge.arquillian.container.Container;
import org.jboss.forge.arquillian.container.ContainerDirectoryParser;
import org.jboss.forge.arquillian.container.ContainerType;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.completer.CommandCompleterState;
import org.jboss.forge.shell.completer.SimpleTokenCompleter;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ContainerCommandCompleter extends SimpleTokenCompleter
{

   public static final String OPTION_CONTAINER_TYPE = "containerType";

   public static final String OPTION_CONTAINER_NAME = "containerName";

   private static final String NOT_FOUND = "container-type-not-found";

   @Inject
   private ContainerDirectoryParser parser;

   @Inject
   private Shell shell;

   private CommandCompleterState state;

   @Override
   public void complete(CommandCompleterState state)
   {
      this.state = state;
      super.complete(state);
   }

   @Override
   public Iterable<?> getCompletionTokens()
   {
      List<Container> availableContainers = null;
      try
      {
         availableContainers = new ArrayList<Container>(parser.getContainers());
         Collections.sort(availableContainers);

         String containerTypeAsString = resolveContainerType();
         if (NOT_FOUND.equals(containerTypeAsString))
         {
            return availableContainers;
         }

         return filterByType(availableContainers, ContainerType.valueOf(containerTypeAsString));
      }
      catch (IOException e)
      {
         ShellMessages.error(shell, e.getMessage());
         return null;
      }
   }

   private Iterable<?> filterByType(List<Container> containers, ContainerType containerType)
   {
      List<Container> filtered = new ArrayList<Container>();
      for (Container container : containers)
      {
         if (containerType.equals(container.getContainerType()))
         {
            filtered.add(container);
         }
      }
      return filtered;
   }

   /**
    * Resolves the value of containerType command option if already specified
    */
   private String resolveContainerType()
   {
      final String completeCommand = state.getBuffer();
      final String[] commands = completeCommand.split("[\\s]++"); // split by one or more whitespaces
      int containerTypePosition = 0;
      for (String token : commands)
      {
         containerTypePosition++;
         if (("--" + OPTION_CONTAINER_TYPE).equals(token))
         {
            break;
         }
      }

      if (containerTypePosition == commands.length)
      {
         return NOT_FOUND;
      }

      return commands[containerTypePosition];
   }

}
