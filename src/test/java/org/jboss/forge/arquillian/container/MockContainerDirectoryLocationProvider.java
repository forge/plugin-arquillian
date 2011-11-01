package org.jboss.forge.arquillian.container;

import javax.enterprise.inject.Alternative;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
@Alternative
public class MockContainerDirectoryLocationProvider implements ContainerDirectoryLocationProvider {
    @Override
    public URL getUrl() {
        System.out.println("Using mock ContainerDirectoryLocationProvider");

        try {
            return this.getClass().getClassLoader().getResource("containers.json").toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
