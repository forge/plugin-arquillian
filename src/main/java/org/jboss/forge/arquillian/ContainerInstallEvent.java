package org.jboss.forge.arquillian;

import org.jboss.forge.arquillian.container.Container;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ContainerInstallEvent
{
   private final Container container;

   public ContainerInstallEvent(Container container)
   {
      this.container = container;
   }

   public Container getContainer()
   {
      return container;
   }
}
