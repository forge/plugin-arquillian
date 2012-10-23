package org.jboss.forge.arquillian.container;

import org.codehaus.jackson.map.ObjectMapper;
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
public class ContainerDirectoryParser {
    private List<Container> containers;

    @Inject
    private ContainerDirectoryLocationProvider containerDirectoryLocationProvider;

    @PostConstruct
    void parse() throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            containers = objectMapper.readValue(containerDirectoryLocationProvider.getUrl(),
                    new TypeReference<List<Container>>() {
                    });
        } catch (IOException e) {
            throw e;
        }
    }

    public List<Container> getContainers() throws IOException {
        return Collections.unmodifiableList(containers);
    }
}
