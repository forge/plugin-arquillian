/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.container;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ContainerTest
{
   @Test
   public void testGetId() throws Exception
   {
      Container container = new Container();
      container.setName("Arquillian Container Tomcat Embedded 6.x");
      container.setArtifact_id("tomcat-embedded-6");

      assertThat(container.getId(), is("tomcat-embedded-6"));
   }

   @Test
   public void testGetProfileId() throws Exception
   {
      Container container = new Container();
      container.setName("Arquillian Container GlassFish Remote 3.1");
      container.setArtifact_id("glassfish-remote-3.1");

      assertThat(container.getProfileId(), is("arquillian-glassfish-remote-3.1"));
   }
}
