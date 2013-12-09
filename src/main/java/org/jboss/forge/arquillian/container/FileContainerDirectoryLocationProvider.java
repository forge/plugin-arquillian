/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.container;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class FileContainerDirectoryLocationProvider implements ContainerDirectoryLocationProvider
{
   @Override
   public URL getUrl()
   {
      try
      {
         return this.getClass().getClassLoader().getResource("containers.json").toURI().toURL();
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }
}
