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

        //NEW URL https://gist.github.com/1324966/9228e16adf569e1657d037faa10ae2419daa6719
        //Old url "https://raw.github.com/gist/1324966/afe53313a2ed345585188a5c1f3d43fdb0c667d6/containers.json"
        //this.getClass().getClassLoader().getResource("containers.json").toURI().toURL()
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            containers = objectMapper.readValue(containerDirectoryLocationProvider.getUrl(), new TypeReference<List<Container>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
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
