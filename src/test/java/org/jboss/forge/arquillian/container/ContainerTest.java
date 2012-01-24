package org.jboss.forge.arquillian.container;

import org.jboss.forge.arquillian.container.Container;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ContainerTest {
    @Test
    public void testGetId() throws Exception {
        Container container = new Container();
        container.setName("Arquillian Container Tomcat Embedded 6.x");

        assertThat(container.getId(), is("TOMCAT_EMBEDDED_6.X"));
    }
}
