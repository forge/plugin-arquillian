package org.jboss.forge.arquillian.container;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
@Singleton
public class ContainerDirectoryParser {
    private List<Container> containers;

    @Inject ContainerDirectoryLocationProvider containerDirectoryLocationProvider;

    private synchronized void parse() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            containers = objectMapper.readValue(containerDirectoryLocationProvider.getUrl(), new TypeReference<List<Container>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Container> getContainers() {
        if (containers == null) {
            synchronized (this) {
                if (containers == null) {
                    parse();
                }
            }
        }

        return Collections.unmodifiableList(containers);
    }
}
