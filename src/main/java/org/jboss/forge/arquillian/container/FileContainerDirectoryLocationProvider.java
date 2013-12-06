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
      } catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      } catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }
}
